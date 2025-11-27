package com.ims;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.zaxxer.hikari.HikariConfig;     // Import HikariConfig
import com.zaxxer.hikari.HikariDataSource; // Import HikariDataSource

//  We just need to import our one "toolbox"
import static com.ims._00_Utils.*;

/**
 *   INVENTORY SERVER — the “loading dock” and “bank vault” of the system.

 *   This is the file we run to start the entire "Back End" (the Server).
 *   It's the "Boss" of the Inventory Mgmt. System. Think of it as the GM of a Warehouse (loading dock) or
 *      the manager of a bank (security and records).

 *   1. Warehouse analogy:
 *      1. The server is the boss that assigns an arrival docking bay (TCP port) and receives trucks (client sockets).
 *      2. For every truck that arrives, the boss assigns a worker (Client Handler) to process that order end-to-end.
 *      3. The server coordinates safe access to warehouse inventory (data) through well-defined operations.

 *   2. Bank analogy:
 *      1. The server is the manager that enforces security at the door (SSL/TLS setup, keystore/truststore).
 *         It starts up, prepare the resources (DB connection/pool, SSL materials), and then open the doors for business,
 *            waiting for clients to arrive, on a known TCP port.
 *         It gets all the resources ready (like the "master ledger" DB connection and the "company ID" SSL cert), and then
 *            waits for the clients to walk in or listens for the "phone" to ring (on a TCP port).
 *      2. Tellers (Client Handlers) handle each request (simple pipe-delimited string commands), from a client.
 *      3. Managers (DAO/service classes, suffixed with ~DB) talks to the DB and perform their work using PreparedStatement.
 *      4. Each client handler returns a clear, machine-friendly response.

 *   3. Responsibilities:
 *      1. Loads application configuration and security materials.
 *      2. Initializes networking primitives (SSL Client/Server socket).
 *      3. Accepts client sockets in a loop and hands each one to the thread pool.
 *      4. Provides shared resources to handlers (JDBC datasource/connections, services), or acquire them safely per request.
 *      5. Logs clearly and fail safely, so one bad client cannot crash the server.

 *   4. Concurrency model:
 *      1. The main thread "blocks" on 'accepting' a client or truck, and never performs request work.
 *      2. Each accepted client is served by an instance of the _02_ServerClientHandler class, running from a pool thread.
 *      3. A fixed-size pool applies backpressure when demand exceeds capacity. Waiting clients have to queue in line.
 */

public class _01_InventoryServer {

    //------------------------------------------------------------------------------------------------------------------
    //   STATIC FIELDS & INITIALIZATION (Application-Wide Settings)
    //------------------------------------------------------------------------------------------------------------------
    //  These fields belong to the CLASS itself and hold settings loaded once.

    //  Holds all properties loaded from config.properties. Accessible anywhere within this class.
    private static Properties config;

    //  The TCP PORT number the server will listen on. Loaded from config.properties.
    private static int PORT;

    //  Path to the server's SSL keystore file. Loaded from config.properties.
    private static String KEYSTORE_PATH;

    //  Password for the server's SSL keystore. Loaded from config.properties.
    private static String KEYSTORE_PASSWORD;



    /**
     *   This is our "team of workers" (a Thread Pool) to handle MULTIPLE CLIENTS CONCURRENTLY.
     *   We retain a team of 10 workers ready to go.
     *   Instead of hiring a single new worker (Thread) every time someone calls (which is slow), we retain a team of 10 workers
     *      ready to go, at a moment's notice, and handle multiple client requests at the same time, without getting bogged down.
     *   This lets us handle 10 client calls at the same time.

     *   THREAD POOL CONFIGURATION:
     *      1. We process clients on a bounded pool (10 threads) to protect the server under load, using:
     *            Executors.newFixedThreadPool(10) where at most 10 clients are served concurrently.
     *            It's like having a fixed crew of 10 workers instead of hiring and firing folks every time a truck shows up.
     *            It keeps things predictable.
     *      2. Additional clients queue until a worker thread becomes free.
     *      3. Prefer a fixed pool for predictable memory and CPU usage.

     *   FROM: "Effective Java" (3rd Ed) by Joshua Bloch, Item 80: "Prefer executors, tasks, and streams to threads."
     *   Bloch explains that creating a new thread for every task is expensive and can overwhelm the system.
     *   Using a Thread Pool (ExecutorService) separates the "unit of work" from the "execution mechanism."

     *   CITATION: Oracle Java Tutorial "The Executor Interfaces": "If you want to guard against this [resource exhaustion],
     *   you should consider using a fixed thread pool." (Source: https://docs.oracle.com/javase/tutorial/essential/concurrency/pools.html)

     *   YOUTUBE: Defog Tech – Java ExecutorService – Part 1 – Introduction (https://www.youtube.com/watch?v=6Oo-9Can3H8)
     *   Explains ExecutorService, newFixedThreadPool, task submission, and why we don’t manually create threads all over the place.

     *   YOUTUBE: Cave of Programming  (John Purcell) – Java Multithreading Playlist (https://www.youtube.com/playlist?list=PLBB24CFB073F1048E)
     *   Covers threads, thread pools, and how to shut things down cleanly — ties directly to how your server stops accepting
     *      new client tasks and terminates safely.
    */

    private static final ExecutorService clientPool = Executors.newFixedThreadPool(10);

    /**
     *   STATIC INITIALIZER BLOCK
     *   This special block of code runs automatically EXACTLY ONCE when the `_01_InventoryServer` class is first loaded
     *      by the Java Virtual Machine (JVM), even BEFORE the `main()` method is called.
     *   Its purpose is to perform one-time setup tasks for the class, like loading configuration.

     *   How it works:
     *   1. Loads the config.properties file using our _05_ConfigLoader helper.
     *   2. Validates that the file was found. If not, prints a fatal error and exits the application.
     *   3. Reads the 'server.port', 'ssl.keystore.path', and 'ssl.keystore.password' properties.
     *   4. Validates the port number and required SSL settings. Exits if critical SSL info is missing.
     *   5. Sets the necessary Java System Properties (`javax.net.ssl.*`) required by the SSLServerSocketFactory.
     *   This MUST be done 'before' the factory is used later in the `main` method.

     *   By doing this here, we ensure that the configuration is loaded and validated early, and the SSL system properties
     *      are set correctly before any networking code runs.
     */


    /**
     *  DATABASE CONNECTION POOL
     *  Sharing a single Connection object across multiple threads is indeed dangerous.
     *     "java.sql.Connection" is not thread-safe. If two clients try to run a query at the same time, they will fight
     *     over that single connection, leading to race conditions, crashes, or data corruption.
     *  Using a connection pool like HikariCP is the industry standard.
     *  It solves the problem by giving each thread its own connection when it needs one, and then recycling it.
     *  Instead of a single 'Connection', we hold the 'DataSource' (the pool itself).

     *  FROM: Oracle Java Documentation under "Connection Pooling" it states: "Opening a new database connection for every user
     *      can be costly and time-consuming... A connection pool is a cache of database connections maintained so that the
     *      connections can be reused when future requests to the database are required."
     *      (Source: https://docs.oracle.com/cd/E13222_01/wls/docs81/ConsoleHelp/jdbc_connection_pools.html)
     *      *
     *  CITATION: Baeldung "Introduction to HikariCP" suggests HikariCP because it is "very lightweight and extremely fast."
     *      (Source: https://www.baeldung.com/hikaricp)
     */
    private static HikariDataSource dataSource;

    //  This is how we make the server read the config file:

    static {
        println("Server class loading... Initializing static configuration from config.properties...");
        config = _05_ConfigLoader.load(); // Load the config file

        //  FATAL CHECK: Make darn sure the Config File exists
        if (config == null) {
            error("FATAL: \"config.properties\" file not found or empty. Server cannot start.");
            //  In a static block, we can't just 'return'. Exit the application forcefully.
            exit(1); //  Use a non-zero exit code to indicate an error
        }
        println("✅ config.properties loaded successfully.");

        //  Load PORT from config
        try {
            //  Read the 'server.port' property. Default to 8888 if not found or invalid.
            PORT = Integer.parseInt(config.getProperty("server.port", "8888"));
            if (PORT <= 0 || PORT > 65535) { //  Standard port range check
                throw new NumberFormatException("Port number must be between 1 and 65535.");
            }
            println("Server Port configured to: " + PORT);
        } catch (NumberFormatException e) {
            //  Handle cases where 'server.port' is missing, not a number, or out of range.
            error("WARNING: Invalid or missing 'server.port' in config.properties. Using default 8888. Error: " + e.getMessage());
            PORT = 8888; //  Fallback to a default port
            //  You might choose to exit here if the port is absolutely critical and has no sensible default:
            //  error("FATAL: Invalid 'server.port'. Server cannot start without a valid port.");
            //  System.exit(1);
        }

        //  Load SSL Keystore settings
        KEYSTORE_PATH = config.getProperty("ssl.keystore.path");
        KEYSTORE_PASSWORD = config.getProperty("ssl.keystore.password");

        //  FATAL CHECK: Validate SSL settings
        if (KEYSTORE_PATH == null || KEYSTORE_PATH.isBlank() || KEYSTORE_PASSWORD == null || KEYSTORE_PASSWORD.isBlank()) {
            //  These are critical for security. The server cannot run securely without them.
            error("FATAL: 'ssl.keystore.path' or 'ssl.keystore.password' is missing or blank in config.properties. Server cannot start securely.");
            exit(1);
        }

        println("SSL Keystore Path: " + KEYSTORE_PATH);
        //  Security Note: don't print passwords to logs, even during startup. People read logs.
        println("SSL Keystore Password loaded (not shown).");

        //  Set the SYSTEM PROPERTIES required for Java's SSL implementation
        //  To prove to clients that we are who we say we are, we need an official ID (the SSL certificate) stored in
        //     a secure vault (the ".keystore" file).

        //  We need to configure the Key Store for SSL/TLS. (Sec. Soc. Layer and Transport Layer Security protocol)
        //  So, we set up our Server's "Official ID" (the secure socket layer Keystore): We tell the Java JVM Security system
        //     the path 'where' to find our "server.keystore" file (our ID badge) and what the password is to unlock it.

        //  This MUST be done before `SSLServerSocketFactory.getDefault()` is called later.
        //  For now, we're just 'registering' them; they aren't used... yet.
        //  These lines tell our security system where to find the vault and what the password is, to open it.
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_PATH);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);
        //     This is all internal. We'll never see an if statement in our own code that compares the keystore password,
        //     With a System.setProperty() call we're registering the security credentials in the JVM before we ask it to do anything secure.
        //     Translation: "Hey Java Security System, when the time comes, the password you'll need for my keystore is 'changeit'."
        //  NOTE: 'changeit' is the default password used by the Java keytool command when we create a new keystore or truststore.
        //     I changed it to 1m_4_c0nn3c710n_p455w0rd ("I'm a connection password" in Leet) in the config.properties

        println("SSL System Properties ('javax.net.ssl.*') set successfully.");

        println("✅ Static configuration and SSL properties initialized.");



        //  Initialize the Connection Pool
        try {
            println("Initializing HikariCP Database Connection Pool...");
            println("By a quirk of Java logging, INFO logs often print in red text (even though they aren't errors).");
            HikariConfig hikariConfig = new HikariConfig();

            // Load DB settings from your config.properties
            hikariConfig.setJdbcUrl(config.getProperty("db.url"));
            hikariConfig.setUsername(config.getProperty("db.user"));
            hikariConfig.setPassword(config.getProperty("db.password"));

            //  POOL SETTINGS
            //  It has to match to the clientPool size (10) so every worker can have a connection (a wrench so to speak).
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);      //  This keeps 2 connections ready at all times
            hikariConfig.setConnectionTimeout(30000); //  We wait 30 secs for a connection before failing

            // Create the pool
            dataSource = new HikariDataSource(hikariConfig);
            println("✅ Database connection pool initialized.");

        } catch (Exception e) {
            error("FATAL: Could not initialize DB pool: " + e.getMessage());
            exit(1);
        }
    } //   END OF STATIC INITIALIZER





    //------------------------------------------------------------------------------------------------------------------
    //   THE MAIN METHOD (Server Entry Point)
    //------------------------------------------------------------------------------------------------------------------

    /**
     *   This is the main() method. It's the "front door" of the server app.
     *   The Java Virtual Machine (JVM) calls this method first AFTER the static initializer block has run.
     *   Its primary job now is to:
     *      1. Establish the database connection.
     *      2. Initialize the Data Access Objects (DAOs from the classes suffixed by ~DB).
     *      3. Create the secure server socket using the pre-configured PORT and SSL settings.
     *      4. Enter the main loop to accept client connections.
     *
     *       @param args Command-line arguments (we don't use any).
     */

    public static void main(String[] args) {

        //  STEP 1: SETUP PHASE (the config is mostly done already statically)
        println("Server main() method starting...");
        //  NOTE: 'config', 'PORT', 'KEYSTORE_PATH', 'KEYSTORE_PASSWORD' are already loaded and validated, and the
        //         SSL System Properties ('javax.net.ssl.*') are already set.



        //  STEP 2: EXECUTION PHASE - Now that setup is done, we can open the doors for business.
        //     We use a 'try' block to catch fatal errors during startup or connection handling (like when the DB is
        //        down/not available).  It helps clean up too.
        try {
            //  STEP 2.1: Connect to the Database (the "master ledger").
            //    We will get this one, single connection and share it with all our warehouse workers (ClientHandlers).
            //    We have to 'first' call our "Master Key" class to get the connection.
            //    I.e., use its own config loading (_10_ModelConnectToDB reads db.* properties)
            //    We save the key (the 'Connection' object) in our new 'dbConnection' variable.
            //  OLD WAY (Dangerous):
            //Connection connectToTheDB = _10_ModelConnectToDB.getConnection_10a();
            //println("✅ Database connection established.");//   This is already shown in _10_ModelConnectToDB, line 61
            //     If getConnection_10a fails, it throws SQLException, caught below.

            //  NEW WAY (Safe):
            //  We already have 'dataSource' ready from the static block!
            //  We don't need to "get a connection" here. We pass the POOL to the workers.


            //  STEP 2.2: Initialize our 3 "Department Specialists" (DAOs).
            //  We pass the 'dataSource' (the toolbox) instead of a single connection key to the specialists:
            //  Users
            final _14_ModelUserDB userDB = new _14_ModelUserDB(dataSource);
            //  Products
            final _12_ModelProdDB prodDB = new _12_ModelProdDB(dataSource);
            //  Suppliers
            final _16_ModelSupplierDB supplierDB = new _16_ModelSupplierDB(dataSource);
            println("✅ Database specialists (DAOs) are ready.");



            //  THE SHUTDOWN HOOK
            //  This code adds a "listener" to the JVM.
            //  If the JVM receives a shutdown signal (like Ctrl+C), this new Thread will run *before* the app exits.
            registerShutdownHook();

            //   TODO: Exit done Partially: I added an "Exit" button to the client GUI.


            //  STEP 2.3: SSL CONFIGURATION (Bank security layer)

            //  YOUTUBE: FreeCodeCamp – Java Full Course / Networking & Server Sections (SOCKETS)
            //  Main course page: 👉 https://www.freecodecamp.org/news/object-oriented-programming-in-java/

            //  FROM: OWASP Transport Layer Protection Cheat Sheet: "Applications that transmit sensitive data... must encrypt that data in transit."
            //  Using SSLServerSocket ensures that data (like passwords) is encrypted on the wire.
            //  (Source: https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Protection_Cheat_Sheet.html)

            //  Get the "factory" for building secure, encrypted "loading docks" (sockets).
            //  We use a special "factory" to create a "Secure Socket Layer Server Socket".
            //  This factory implicitly uses the 'javax.net.ssl.*' System Properties set in the static block.
            //  If the keystore path/password properties were wrong, this step might throw an IOException, and the server
            //     crashes. If it's correct, the code continues silently.

            //  Internally, it now performs these steps:
            //     1. Loads the keystore path registered in config.properties and set  javax.net.ssl.* system properties,
            //           or build an SSLContext explicitly.
            //     2. Create an SSLServerSocket from SSLServerSocketFactory.
            //     3. Require client authentication if the policy demands mutual TLS.
            //     4. If any SSL material is missing or unreadable, stop with a clear, actionable error.
            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();  //  svrScktFactory
            println("SSLServerSocketFactory obtained.");
            //  Translation: "Hey Java Security System, I need the factory that builds secure server sockets."
            //  The Magic Moment: When SSLServerSocketFactory.getDefault() is called, the Java Security System says:
            //  "Okay, to create a secure factory, I must first load the user's official identity from their keystore."


            //  STEP 2.4: Open the secure "loading dock" (Server Socket).
            //     It is at this moment, deep inside the compiled, internal Java code, that the comparison happens.
            //     This 'try-with-resources' block is special. It guarantees the 'serverSocket' will be closed, no matter what.
            //     It uses the static 'PORT' field loaded from config.properties.
            try (SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(PORT)) {
                //  Server socket is successfully created and listening.
                println("✅ Secure Inventory Server is open on port " + PORT);
                println("Waiting for client connections...");


                //  STEP 2.5: The "Forever Loop" - Accept incoming client connections indefinitely.
                //     This is the main server loop. It will run forever, until shutdown, waiting for new clients to call.
                while (true) {
                    //  STEP 2.5a: Wait for the "phone to ring" (4 a client to connect).
                    //     It's like waiting for a new client truck to arrive at the loading dock.
                    //     Then accept a new client connection. This is a blocking call.
                    //     The .accept() method 'pauses' the program (BLOCKS it) right here, pausing the main thread
                    //        until a new client connects (phone answered, truck docked on the bay).
                    Socket clientSocket = serverSocket.accept(); //  Returns a regular Socket for the specific client
                    println("📞 Client connected from: 'S' " + clientSocket.getInetAddress().getHostAddress());
                    //  Note: 'clientSocket' will be an SSLSocket because it came from an SSLServerSocket.


                    //  STEP 2.5b: Assign a "worker" (_02_ServerClientHandler) to the connected client and submit it to the thread pool.
                    //     We create a new "Client Handler" instance (our worker, to handle the truck) and give it all the tools it needs:
                    //        1. The client's personal "phone line" (clientSocket).
                    //        2. The three "department specialists" (shared DAOs for user, prod and supplier) who hold the
                    //           connection and key to the database
                    //     We ubmit this handler task to our thread pool (`clientPool`) for execution.
                    //     Then we tell our team of workers (`clientPool`) to handle a new job.
                    //     This lets the main loop (the main thread) go back immediately to wait for and accept the next
                    //        client truck, which allows the server to handle multiple clients at once.
                    clientPool.execute(new _02_ServerClientHandler(clientSocket, prodDB, userDB, supplierDB));
                }
            } //  End of try-with-resources (serverSocket is automatically closed here if loop exits)

        } catch (IOException e) {
            //  Catch errors related to network setup (createServerSocket, accept) or SSL issues (bad keystore/password).
            error("FATAL: Could not start or run server socket on port " + PORT + ": " + e.getMessage());
            error("Check if port is already in use or if SSL Keystore details are correct.");
            //  Print stack trace for detailed diagnosis, especially useful for SSL errors.
            e.printStackTrace();
        } catch (Exception e) {
            //  Catch any other unexpected errors during startup.
            error("FATAL: An unexpected error occurred during server startup: " + e.getMessage());
            e.printStackTrace();
        } finally {
            //  Proper shutdown cleanup
            if (dataSource != null) {
                dataSource.close(); // Close the pool when server stops
            }
        }

    //  TODO: Add this line to the config.properties file: server.port=8888         Done!

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            println("Shutdown signal received. Closing resources...");

            // Stop accepting new clients
            clientPool.shutdown();

            try {
                // Wait for existing tasks to finish (30 seconds max)
                if (!clientPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    // Force shutdown if they won't finish
                    clientPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                clientPool.shutdownNow();
            }

            // Close database connection pool
            if (dataSource != null) {
                dataSource.close();
            }

            println("Server shutdown complete.");
        }));

    } //  End of MAIN method



    //  Note on Shutdown:
    //  This simple server runs forever in the while(true) loop. It ends because of the "try-with-resources."
    //  A production server would typically have a shutdown hook (e.g., using Runtime.getRuntime().addShutdownHook())
    //     or a 'finally' block to close the dbConnection and shutdown the pool or an external signal mechanism to:
    //     1. Close the serverSocket gracefully.
    //     2. Shut down the clientPool (clientPool.shutdown(); clientPool.awaitTermination(...)).
    //     3. Close the database connection (dbConnection.close()).
    //     4. Shutdown the server.

    /**
     *   TODO: SHUTDOWN: stop all, close the dock cleanly. (DONE!)

     *  Registers a shutdown hook to finish ongoing work, or gracefully close the connection pool and thread pool, before shutting down.CC
     *   1. Stop accepting new clients and close the server socket.
     *   2. Shut down the ExecutorService:
     *      1. call shutdown() to stop new tasks,
     *      2. call await termination for a bounded time, to wait for running tasks
     *      3. call shutdownNow() if tasks refuse to finish or don't finish in time
     *   3. Close database resources or return them to the pool.
     *   4. Log a final message confirming shutdown.

     *  CITATION: GeeksforGeeks "Runtime addShutdownHook() method in Java": "The shutdown hook can be used to perform
     *    cleanup resources or save the state when the JVM shuts down normally or abruptly."
     *    (Source: https://www.geeksforgeeks.org/runtime-addshutdownhook-method-in-java/)

     *  CITATION: Stack Overflow "How to gracefully shutdown a Java ExecutorService":
     *     (Source: https://stackoverflow.com/questions/3973018/awaittermination-vs-shutdown)
     *
     *  Amigoscode – HikariCP (Connection Pooling with Java / Spring Boot)  https://www.youtube.com/watch?v=CJjHdchLY9Y
     *  This walks through configuring HikariCP, understanding why pools are used, and how connections are handed out
     *     and returned — very close to what your server is doing when clients hit the DB.
     */

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //  This registers a new thread with the Java Runtime. This thread will be executed when the JVM begins its shutdown sequence.
            //  Using a separate thread is crucial because shutdown hooks run concurrently with other shutdown processes.
            println("\nShutdown signal received. Closing resources...");

            // 1. Stop accepting new tasks from the client pool
            println("Shutting down client thread pool...");

            //  This "clientPool.shutdown" initiates an orderly shutdown of the clientPool (ExecutorService).
            //  It stops the acceptance of new tasks but allows existing tasks (those already running or in the queue) to complete.
            clientPool.shutdown(); // Disable new tasks
            try {
                //  Wait a grace period for existing tasks to finish.
                //  This line causes the current thread (the shutdown hook thread) to block and wait for up to 30 seconds for the clientPool to fully terminate.
                //  This gives the currently running tasks a chance to finish their work gracefully.
                //  It returns true if the pool terminated successfully within the timeout, and false otherwise.
                if (!clientPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    //  If the pool doesn't terminate within the 30-second grace period (i.e., awaitTermination returns false),
                    //     or if the waiting thread is interrupted (InterruptedException), this method is called.
                    //  "shutdownNow()" attempts to stop all actively executing tasks (typically by interrupting them)
                    //     and halts the processing of waiting tasks. It's a more forceful shutdown.
                    println("Thread pool did not terminate in 30s. Forcing shutdown...");
                    clientPool.shutdownNow(); // Force-stop stubborn tasks
                } else {
                    println("Client pool shut down gracefully.");
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if interrupted
                clientPool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }

            // 2. Close database connection pool
            if (dataSource != null) {
                println("Closing database connection pool...");

                //  This closes all connections, including the HikariCP connection pool.
                //  It ensures that all physical database connections associated with the pool are closed properly,
                //     releasing resources on both the application side and the database server side.
                dataSource.close();

                //  The println statements provide helpful feedback in the logs, allowing you to track the shutdown process
                //     and identify if it completed successfully or if forced termination was necessary.
                println("Database pool closed.");
            }

            println("Server shutdown complete.");
        }));
        println("✅ Graceful shutdown hook registered.");
    }

}






