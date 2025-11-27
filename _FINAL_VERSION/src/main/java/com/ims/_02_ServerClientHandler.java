package com.ims;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//  Import our "toolbox"
import static com.ims._00_Utils.*;




/**
 *   This "Client Handler" is the "Warehouse Worker" or "Server Operator" for one customer order (a client Socket).
 *   The listening server (the “loading dock”) accepts a new customer (Socket), and hires a worker (new ClientHandler).
 *   I.e., _01_InventoryServer creates 'one new instance' of this class for every single client that connects (on a Socket).
 *   Each time a client connects, the server hands this worker (ClientHandler) to the floor via the ExecutorService (thread pool).

 *   How it’s created:
 *      1. The server (_01_InventoryServer) calls ServerSocket.accept().
 *      2. For each accepted Socket, the server constructs a new ClientHandler and submits it to the pool:
 *            executor.submit(new ClientHandler(socket)).  We configured Executors.newFixedThreadPool(10), that’s a pool of
 *            10 workers. A fixed-size pool applies backpressure: excess clients will queue until a thread is free.
 *         I chose not to use newCachedThreadPool(), where the pool would be unbounded and grow/shrink as needed.

 *   Lifecycle:
 *      1. Created by _01_InventoryServer for each accepted Socket.
 *      2. Submitted to an ExecutorService (thread pool), which invokes run() on a worker thread.
 *      3. Owns the Socket until it finishes: read → process → write → close.

 *   What it does (per connection):
 *      1. Listens for requests ("commands"), typically one line at a time, a single connected client, in UTF-8.
 *      2. Figures out what that command means (the "brain") and processes requests. It parses the command
 *            (e.g., LOGIN | READ | INSERT | UPDATE | DELETE, etc.).
 *      3. Talks to the "department specialists" (DAO services) to get the work done. It interacts with the DB manager
 *          models (the classes I ended on ~DB), and asks the specialists to fetch info (DB ops).
 *      4. Sends a clear, machine-friendly parsable responses (like "SUCCESS" or "FAILURE") back to the client,
 *            and delivers the package (the data requested) after the pipe "|".
 *      5. Closes the ticket and cleans up (streams/socket) when the client disconnects or on error (w try-with-resources & finally).

 *   Concurrency & scaling:
 *      1. With Executors.newFixedThreadPool(N), at most N clients are processed concurrently; additional clients queue.
 *      2. With newCachedThreadPool(), the pool expands/shrinks as needed (we need to be mindful of system limits).
 *      3. Any shared objects must be thread-safe (need to avoid mutable shared state where possible).

 *   Notes:
 *      1. One worker per customer connection.
 *      2. This class doesn't accept DB connections (that’s the server’s job) and it doesn't block the server’s accept loop.
 *      3. Implements Runnable: run() is the worker’s shift. A Runnable contract in the class declaration, is promise to
 *            have a small, clear, and exception-safe public `run()` method, which is like the worker's "SOP" (instruction manual).
 *            It handles exceptions inside run() so a bad client cannot crash the worker thread.
 *      4. The server runs this 'worker' on a separate thread, by a manager (the ExecutorService) so it doesn't block
 *            the main server from accepting new clients.
 *      5. Shared state (caches, singletons) must be thread-safe.

 *   CITATION: Oracle Java Tutorials "Defining and Starting a Thread": "The Runnable interface should be implemented by any
 *      class whose instances are intended to be executed by a thread. The class must define a method of no arguments called run."
 *   This separates the task from the thread that runs it.
 *   (Source: https://docs.oracle.com/javase/tutorial/essential/concurrency/runthread.html)

 *   YOUTUBE: Jakob Jenkov – Java ExecutorService Tutorial (https://jenkov.com/tutorials/java-util-concurrent/executorservice.html)
 *   Great for understanding how the handler pool is created, how tasks run, and how to correctly shut it down.

 *   YOUTUBE: GeeksforGeeks - Runnable Interface in Java: https://www.geeksforgeeks.org/runnable-interface-in-java/
 *   This is a clear example of how to implement Runnable and start threads.
 */

public class _02_ServerClientHandler implements Runnable {

    //   INSTANCE VARIABLES

    //  This is the "personal phone line" to this one specific client.
    //  This holds the specific, unique connection to each client this worker is responsible for.
    private final Socket clientSocket;

    //  These are the 3 "department specialists" this worker (_02_ServerClientHandler) can talk to,
    //  They're the data access objects (DAOs) for each entity, each DB table.
    private final _12_ModelProdDB prodDBManager;
    private final _14_ModelUserDB userDBManager;
    private final _16_ModelSupplierDB supplierDBManager;


    //  This is a product separator between rows
    //  ASCII Record Separator: very unlikely to appear in normal text and never typed by humans.
    private static final String RECORD_SEP  = "\u001E"; //


    /**
     *  CONSTRUCTOR
     *  To “hire” a warehouse worker for a customer (a Socket) we call "this" to create that new worker.
     *  The Big Boss, the server (_01_InventoryServer) accepts a client and constructs a new handler, supplying the client’s
     *     Socket and references to the DB “specialists”.
     *  This handler/worker OWNS the Socket (it will do read/write ops and then close it).
     *    @param socket The client's personal "phone line."
     *    @param userDB The "User Specialist."
     *    @param prodDB The "Product Specialist."
     *    @param supplierDB The "Supplier Specialist."

     *   YOUTUBE: FreeCodeCamp – Java Full Course / Networking & Server Sections (SOCKETS)
     *   Main course page: 👉 https://www.freecodecamp.org/news/object-oriented-programming-in-java/
     */
    public _02_ServerClientHandler(Socket socket, _12_ModelProdDB prodDB, _14_ModelUserDB userDB, _16_ModelSupplierDB supplierDB) {
        this.clientSocket = socket;
        this.prodDBManager = prodDB;
        this.userDBManager = userDB;
        this.supplierDBManager = supplierDB;
    }





    /**
     *  This is the worker's "To-Do List."
     *  When the "clientPool" in the Server, using an ExecutorService (the team of workers) starts this worker thread,
     *    this 'run()' method is the first and only thing it does. This method is called automatically, and it's given this ClientHandler.

     *  WEBSITE: Baeldung - A Guide to the Java ExecutorService: https://www.baeldung.com/java-executor-service-tutorial
     *  They give an excellent guide on thread pools, submitting tasks, and shutting down executors.
     */
    @Override
    public void run() {

        //  STEP 1: VERIFY THE SECURE CONNECTION
        //     This 'if' block is just for our peace of mind.
        //     It checks the "phone line" to make sure it's 'really' an encrypted SSLSocket.
        //     It says: "Is the connection I was given a standard, insecure phone line (Socket) or
        //        is it a special, encrypted, secure one (SSLSocket)?"
        //     This is like the worker checking the 'security seal' on the connection.
        //     This code gives 100% confidence that the SSL/TLS handshake worked, and your connection is truly encrypted.
        if (clientSocket instanceof SSLSocket) {
            //  If it is, we log the details to the console, proving our encryption is working.
            SSLSession session = ((SSLSocket) clientSocket).getSession();

            //  Log in the SSL/TLS connection details. This part is a logging, diagnostic and debugging tool, but
            //     in production, we don't want to log this for every connection.
            //  This then prints the details of that secure agreement (session) to the server's console.
            //  The protocol confirms we're using a modern, secure Transport Layer Security protocol (TLSv1.3).
            //  Cipher Suite: This prints a long, technical name like TLS_AES_128_GCM_SHA256.
            //  This is the exact combination of encryption algorithms being used to scramble the data.
            //  It's like confirming the exact make and model of the encryption machine.
            //  Think of it as the warehouse worker, upon starting their shift, making a quick note in their logbook.
            println("SSL Session Info: Protocol=" + session.getProtocol() + ", Cipher=" + session.getCipherSuite());
            //  if the program crashes here, it tells that something went wrong with the keystore or truststore setup.
        }




        //   STEP 2: OPEN COMMUNICATION TOOLS
        /**
         *  The next try block is a safe way to handle 'resources' that will automatically close our tools
         *     (our  'reader' and 'writer' network streams) later, when we're done; even if an error happens.
         *  How it works:
         *     1. We declare our 'reader' and 'writer' tools inside the parentheses `()`.
         *     2. The `reader` lets us "hear" (read) messages from the client.
         *     3. The `writer` lets us "talk" (write) messages back to the client.
         *     4. Because they are in the `try(...)`, Java guarantees that `reader.close()` and `writer.close()`
         *        will be called automatically when this block is finished. This happens even if an error occurs!
         *     5. This prevents 'resource leaks' which is a common bug where connections are left open, eventually crashing the server.

         *  CITATION: Oracle Java Tutorials "The try-with-resources Statement": "The try-with-resources statement is a
         *     try statement that declares one or more resources. A resource is an object that must be closed after the
         *     program is finished with it."
         *  This ensures that the BufferedReader and PrintWriter are closed automatically, preventing memory leaks.
         *  (Source: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResource.html)
         */
        try (
                //  Tool 1: The client's "Speaker" (we listen to):
                //     1. Get the "raw byte stream" from the client.
                //     2. Wrap it in an InputStreamReader to turn those bytes into characters.
                //     3. Wrap it in a BufferedReader to read a whole line of text at once.
                BufferedReader buffReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                //  Tool 2: Our "Microphone" (for talking back to the client).
                //  Get the "raw byte" output stream and wrap it in a PrintWriter to send text lines immediately.
                //  The 'true' in printWriter means "auto-flush."
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            //  This variable will hold the client's single line of message.
            String lineBufferedIn;

            //   STEP 3: THE "CONVERSATION LOOP"

            //  3.
            /**
             *  This 'while' loop is the heart of the worker's life, the main "conversation loop."
             *  It will loop forever, listening for messages. It will run as long as the client stays connected and sends messages.
             *  The magic is in 'buffReader.readLine()'.
             *  How it works:
             *    1. It 'pauses' the thread right here (on buffReader.readLine()), "blocking" and waiting for the client
             *       to send a full line of text (ending with a newline).
             *    2. When a line comes in, it's put into 'lineBufferedIn'. The line is received is assigned to the `request` variable.
             *    3. If the client hangs up, readLine() returns 'null', which makes the `while` loop 'FALSE' and ends the
             *       conversation (the client has disconnected or closed the app).
             */
            while ((lineBufferedIn = buffReader.readLine()) != null) {

                //  Step 3.1: We got a message! Log it to the server console.
                println("Received from client: " + lineBufferedIn);

                //  Step 3.2: Send the message to our "brain" (the _02b_processRequest method) to get a response and
                //     figure out what to do. Place result inside "processedResponse."
                String processedResponse = _02b_processRequest(lineBufferedIn);

                //  Step 3.3: Send the resulting response back to the client.
                println("Sending to client: " + processedResponse);
                writer.println(processedResponse);  //  for testing
                //  I commented out these previous lines because the println statement is purely for debugging purposes,
                //     allowing us to see exactly what the server is sending back.
                //  In a production environment, once we're confident the data is correct, we don't need such verbose logging.
                //println("Sending to client: a processed response.");  //  simpler
            }

        } catch (IOException e) {
            //  We `catch` an Input/Output error if the client disconnects abruptly (e.g., network cable unplugged, app crash,
            //     the client closes the app or loses Wi-Fi), while we were trying to read or write, an 'IOException' is thrown.
            //  We just log it quietly.
            error("Client handler I/O error: " + e.getMessage());
        } finally {
            //   STEP 4: CLEANUP
            //  This 'finally' block 'always' runs, whether the loop finishes or an error happens, no matter what.
            //  Its job is to do the final cleanup.
            //  It runs if the `while` loop exits normally OR if an `IOException` was caught (abrupt disconnect).
            //  We must close the main `clientSocket` to free up the system resources (like a port) associated with the client.
            //  Note: The `try-with-resources` block only closed the `reader` and `writer`, not the `socket` itself.
            try {
                //  We MUST close the client's "personal phone line" to free up resources on the server.
                clientSocket.close();
                println("Client disconnected. Socket closed.");
            } catch (IOException e) {
                error("Error closing client socket: " + e.getMessage());
            }
        }
    }






    /**
     *  This method is the "Brain" of our worker.
     *    It parses the client's request and decides what action to take: routes it to the correct "department specialist"
     *       (DAO) data access method.
     *    @param request The raw request string message from the client (e.g., "GET_ALL_PRODUCTS:", "LOGIN::foo@bar.com|12345").
     *    @return A response string for the client (e.g., "SUCCESS|..."). It's often JSON to be sent back to the client.
     */
    private String _02b_processRequest(String request) {
        //  This defines the application's protocol (the language we speak with the client): "COMMAND:DATA".
        //  This simple "protocol" is for example: "ADD_PRODUCT:{\"name\":\"Laptop\",\"price\":1200}"
        //  A more robust protocol would use JSON for both requests and responses.


        //  Validate the request format first
        if (request == null || request.isBlank()) return "FAILURE: Invalid request: empty";

        //  STEP 1: Split the string at the first "::" (double colon)
        String[] parts = request.split("::", 2);
        if (parts.length < 1)  return "FAILURE: Invalid request format";

        String COMMAND = parts[0];  //  all caps, bc the commands are in all caps.
        //  The  "DATA" part is everything 'after' the "::"
        String data = parts.length > 1 ? parts[1] : "";

        //  We use a 'try' block here to catch 'any' error (like a database error or bad data) and turn it into a clean "FAILURE" message for the client.
        try {
            //  Step 2: Use a 'switch' to act as the "router."
            //     We are "switching" based on the value of the `command` string, to jump to the `case` the matching case.
            switch (COMMAND) {

                //------------------------------------------------------------------------------------------------------
                //   USER COMMANDS
                //------------------------------------------------------------------------------------------------------

                case "LOGIN":
                    //  The 'data' will be "email|plainTextPassword"
                    String[] loginParts = data.split("\\|", 2);
                    //  We pass the work to the specialist.
                    return userDBManager._14a_svrHandleLogin(loginParts[0], loginParts[1]);


                case "SIGNUP":
                    //  The 'data' will be "name|email|hashedPassword|phone|role"
                    //String[] signupParts = data.split("\\|", 5);  //  No bueno. Caca.

                    // Accepts current client log: id|role|name|pwdHash|email|phone)

                    String[] signupParts = data.split("\\|", -1); // keep empties

                    _13_ModelUser newUser;

                    if (signupParts.length == 5) {
                        // Format A: role|name|email|hashedPassword|phone
                        String role    = signupParts[0];
                        String name    = signupParts[1];
                        String pwdHash = signupParts[2];
                        String email   = signupParts[3];
                        String phone   = signupParts[4];

                        //  testing:
                        System.out.printf("[HANDLER] parsed -> role=[%s], name=[%s], hash=[%s], email=[%s], phone=[%s]%n",
                                role, name, pwdHash, email, phone);


                        //  We build a 'ModelUser' object from the data
                        newUser = new _13_ModelUser(null, role, name, pwdHash, email, phone, 0);

                        //  testing:
                        System.out.printf("[HANDLER] model -> role=[%s], name=[%s], hash=[%s], email=[%s], phone=[%s]%n",
                                newUser.getRole(), newUser.getName(), newUser.getPwdHash(), newUser.getEmail(), newUser.getPhone());

                    } else {
                        return "FAILURE: Malformed SIGNUP payload";
                    }

                    //  Normalizing role
                    String roleNorm = newUser.getRole() == null ? "" : newUser.getRole().trim().toLowerCase();
                    if (!roleNorm.equals("admin") && !roleNorm.equals("staff")) {
                        return "FAILURE: Role must be 'admin' or 'staff' (got: " + newUser.getRole() + ")";
                    }

                    //  Pass it to the specialist.
                    return userDBManager._14b_addNewUser(newUser);


                case "GET_ALL_USERS":
                    //  This command needs no data. Just call the specialist.
                    return userDBManager._14c_getAllUsersAsString();


                case "REMOVE_USER":
                    //  The 'data' will be "id1,id2,id3"
                    List<String> userIds = Arrays.asList(data.split(","));
                    userDBManager._14e_removeUsersById(userIds);
                    return "SUCCESS";


                case "UPDATE_USER":
                    //  The 'data' is "id|role|name|pwdHash|email|phone|loginAttempts"
                    String[] userParts = data.split("\\|", 7);
                    _13_ModelUser updatedUser = new _13_ModelUser(userParts[0], userParts[1], userParts[2], userParts[3], userParts[4], userParts[5], Integer.parseInt(userParts[6]));
                    userDBManager._14d_updateUser(updatedUser);
                    return "SUCCESS";





                //------------------------------------------------------------------------------------------------------
                //   PRODUCT COMMANDS
                //------------------------------------------------------------------------------------------------------

                case "GET_ALL_PRODUCTS":
                    //  In a real implementation (in a real app) we would get:
                    //     ObservableList<_10_ModelProd> products = modelProdDB.getAllProducts();
                    //     return convertProductListToJson(products);
                    // For this example, we return mock(hard-coded) JSON data string.
                    return prodDBManager._12c_getAllProductsAsString();


                case "ADD_PRODUCT":
                    //  The 'data' is "category|sku|name|description|quantity|price|imageUrl"
                    String[] prodParts = data.split("\\|", 7);
                    _11_ModelProd newProd = new _11_ModelProd(
                            prodParts[0],
                            prodParts[1],
                            prodParts[2],
                            prodParts[3],
                            Integer.parseInt(prodParts[4]),
                            Double.parseDouble(prodParts[5]),
                            prodParts[6]);
                    prodDBManager._12a_addProduct(newProd);
                    return "SUCCESS";


                case "REMOVE_PRODUCT":
                    //  The 'data' is "sku1,sku2,sku3"
                    List<String> skus = Arrays.asList(data.split(","));
                    prodDBManager._12e_removeProductsBySku(skus);
                    return "SUCCESS";


                case "UPDATE_PRODUCT":
                    //  The 'data' is "category|sku|name|description|quantity|price|imageUrl"
                    String[] updateParts = data.split("\\|", 7);
                    _11_ModelProd updatedProd = new _11_ModelProd(
                            updateParts[0],
                            updateParts[1],
                            updateParts[2],
                            updateParts[3],
                            Integer.parseInt(updateParts[4]),
                            Double.parseDouble(updateParts[5]),
                            updateParts[6]);
                    prodDBManager._12d_updateProduct(updatedProd);
                    return "SUCCESS";


                case "BULK_ADD_PRODUCTS":
                    //  The 'data' is "product1;product2;product3"
                    //String[] productStrings = data.split(";");  //  BUG

                    //  This new separator is bug free:
                    String[] productStrings = data.split(RECORD_SEP);  //  "\u001E" or U+001E Record Separator (RS)

                    List<_11_ModelProd> products = new ArrayList<>();
                    //  Loop through each product string and parse it
                    for (String pString : productStrings) {
                        String[] proParts = pString.split("\\|", 7);
                        products.add(new _11_ModelProd(
                                proParts[0],
                                proParts[1],
                                proParts[2],
                                proParts[3],
                                Integer.parseInt(proParts[4]),
                                Double.parseDouble(proParts[5]),
                                proParts[6]));
                    }
                    //  Pass the whole list to the specialist
                    int addedCount = prodDBManager._12b_bulkAddProducts(products);
                    return "SUCCESS|" + addedCount + " products imported.";





                //------------------------------------------------------------------------------------------------------
                //   SUPPLIER COMMANDS
                //------------------------------------------------------------------------------------------------------

                case "GET_ALL_SUPPLIERS":
                    return supplierDBManager._16c_getAllSuppliersAsString();


                case "ADD_SUPPLIER":
                    // Data format: "company|contactName|email|phone|address|notes"
                    String[] addSupParts = data.split("\\|", 6);  //  split this string into at most 6 parts
                    // Create a supplier object (ID is null, DB generates it)
                    _15_ModelSupplier newSupplier = new _15_ModelSupplier(
                            null,               // ID
                            addSupParts[0],     // Company
                            addSupParts[1],     // Contact Name
                            addSupParts[2],     // Phone
                            addSupParts[3],     // Email
                            addSupParts[4],     // Address
                            addSupParts[5]      // Notes
                    );
                    //  Pass it to the supplier specialist (DAO)
                    supplierDBManager._16b_addSupplier(newSupplier); //  method is in _16_ModelSupplierDB
                    return "SUCCESS";


                case "REMOVE_SUPPLIER":
                    //  Data format from _03q_removeSuppliers: "id1,id2,id3"
                    //  1st: Check if data is empty or null before splitting
                    if (data == null || data.isBlank()) {
                        //  For now, I'll assume success if no IDs are provided.
                        return "SUCCESS";
                        //  Or: return "FAILURE|No supplier IDs provided for removal.";
                    }
                    //  Need to split the comma-separated IDs into a List<String>
                    List<String> supplierIdsToRemove = Arrays.asList(data.split(","));
                    //  and then pass the list of IDs directly to the DAO method
                    supplierDBManager._16e_removeSuppliersById(supplierIdsToRemove);
                    return "SUCCESS";


                case "UPDATE_SUPPLIER":
                    //  Data format from _03r_updateSupplier: "id|company|contactName|address|email|phone"
                    String[] updateSupParts = data.split("\\|", 7); //  Expecting 6 parts now, including ID
                    //  Create a supplier object with all the data, including the ID
                    _15_ModelSupplier updatedSupplier = new _15_ModelSupplier(
                            updateSupParts[0],     //  ID
                            updateSupParts[1],     //  Company
                            updateSupParts[2],     //  Contact Name
                            updateSupParts[3],     //  Phone
                            updateSupParts[4],     //  Email
                            updateSupParts[5],     //  Address
                            updateSupParts[6]      //  Notes
                    );
                    //  Pass the complete supplier object to the DAO method
                    supplierDBManager._16d_updateSupplier(updatedSupplier);
                    return "SUCCESS";


                //   FALLBACK
                default:
                    //  If the command isn't in our 'switch', we send an error.
                    //  The "default" case is like an "else" block. It catches any command we don't recognize.
                    //  If the client sends "DELETE_EVERYTHING", it will hit this default block, and we'll send back an error.
                    return "FAILURE: Unknown command: " + COMMAND;
            }
        }
        //  This is our "safety net." If 'anything' goes wrong (a database error, bad data format, etc.),
        //     we'll catch it and send a clean "FAILURE" message.
        catch (NullPointerException e) {
            error("Null pointer in command '" + COMMAND + "': " + e.getMessage());
            //  We send the error message to the server, so it can be decrypted.
            return "FAILURE: Server encountered an unexpected error. Please contact sys-op.";
        } catch (SQLException e) {
            e.printStackTrace();
            //  For DB errors related to duplicate records (same Sku number)
            String msg = e.getMessage();
            if (msg != null && msg.contains("Duplicate entry")) {
                return "FAILURE: Duplicate SKU found. " +
                        "At least one SKU in the file already exists in the database.\n\n" +
                        "Details: " + msg;
            }
        } catch (Exception e) {
            error("Unexpected error in command '" + COMMAND + "': " + e.getMessage());
            e.printStackTrace(); // For the server logs
            return "FAILURE: An error occurred. Please try again or contact support.";
        }
        return COMMAND;
    }
}