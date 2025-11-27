package com.ims;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

//  Import our "toolbox"
import static com.ims._00_Utils.*;


/**
 *  This class manages the client-side connection to the server. It handles SSL/TLS setup, socket creation, and sending/receiving messages.
 *  This is the "Messenger" or the "Only Phone Line in the Building" for the CLIENT application.

 *  Its ONLY job is to handle all communication with the server, for all kinds of CRUD operations.
 *  No other class in the client app knows how to talk over the network like this.
 *  Every controller (like _21_CtrlLogin) will be "given" (injected with) this one class to make calls.

 *  Because of the dual nature of this class (1) open the line and (2) communicate, I thought about splitting it.
 *  But chose not to, because:
 *     1. It's Not Too Big (Yet): The "_03_ClientServerLine" is doing okay right now. It's got a clear job:
 *        "be the only way the client talks to the server." While it knows the specific commands, those commands are all
 *        tightly related to its main job. It follows the "Single Responsibility Principle."
 *     2. Keeps Things Simpler: If I split it, my controllers (like _24_CtrlProd) would suddenly need 2 tools injected
 *        instead of 1: the "phone line" and the "CRUD command list." That adds a bit more wiring.
 *     3. The "API" Idea: If you think of "_03_ClientServerLine" as defining the Client's API for talking to the server,
 *        then all the available "calls" you can make are neatly listed in one place. That's very handy!
 */
public class _03_ClientServerLine {

    //  BASIC CONFIGURATION

    //  CONFIGURATION - loaded from config.properties
    private static final String HOST;    //  The server's address
    private static final int PORT;       //  The server's "loading dock"

    /**
     *  SECURITY CONFIGURATION

     *  This is the "Guest List" (TrustStore) that has the server's "ID Badge" on it, so we know we're not talking to an impostor.
     *  This is the necessary, healthy paranoia that the entire security of the internet is built on.
     *  How do you really know you're talking to your bank (server) and not a Man-in-the-Middle (MiM) hacker?
     *  What if we happily type in our credentials. The MiM hacker would copy them, and then they would log us (transfer us)
     *     to the real bank, without us noticing anything. From this point on they can log in as any time, pretending to be us.

     *  This entire complex dance (with the keystore and truststore) is designed to solve this one single problem:
     *     the client must prove to itself that the server is not an impostor.

     *  The server wants to prove it's a very important person. Its "server.keystore" is its wallet, which contains its
     *     secret private key and its official photo ID (the public SSL certificate).
     *  The Client is the Bouncer at the door of an exclusive club. His job is to make sure only legitimate VIPs get in.
     *  The "client.truststore" is the Bouncer's Guest List: This isn't the bouncer's own ID. It's a special, locked
     *     clipboard that contains a photocopy of every expected VIP's photo ID.
     *  When the VIP (Server) comes to the door, the Bouncer (Client) asks for their ID. The Bouncer then unlocks their own
     *     clipboard (client.truststore) with their own password (TRUST_STORE_PASSWORD) and compares the VIP's ID to the photocopy on the list.
     *  The Client needs its own password (TRUST_STORE_PASSWORD) simply to protect its guest list from being tampered with
     *     by other programs on the client's machine.

     *  To trust the server, our client (the Bouncer) needs a list of all the servers (VIPs) it's allowed to talk to.
     *  This list is the "client.truststore" file. It's like a locked clipboard containing a photocopy of every trusted server's ID badge.

     *  Citation: "This class extends Socket and provides secure socket using protocols such as the 'Secure Sockets Layer' (SSL)
     *     or IETF 'Transport Layer Security' (TLS) protocols." - ORACLE Java Documentation
     */
    private static final String TRUST_STORE_PATH;

    //  This is the password to unlock the Bouncer's clipboard (the truststore file itself). It's NOT the server's password.
    //  It's the password the client uses to protect its own list of trusted connections.
    //  It must match the password we used when we created the client.truststore file.
    private static final String TRUST_STORE_PASSWORD;



    //  STATIC INITIALIZER

    //  This block runs EXACTLY ONCE when the _03_ClientServerLine class is first loaded by the JVM.
    //  It's the CLIENT'S version of what _01_InventoryServer does on the server side.

    //  Why here and not in the constructor?
    //     1. These settings are the SAME for all instances of this class (if we ever made multiple ones).
    //     2. We want to fail EARLY if the config is bad, not when someone tries to connect.
    //     3. It matches the pattern we used on the server side, keeping our code consistent.

    //  Load config when class is first used. This runs ONCE, just like in _01_InventoryServer
    static {
        println("Loading client configuration...");

        // Load the config file (same file the server uses)
        Properties config = _05_ConfigLoader.load();

        if (config == null) {
            error("FATAL: Could not load config.properties for client.");
            error("Client will use default values, but connection may fail.");

            //  Fallback to defaults if config fails
            HOST = "localhost";
            PORT = 8888;
            TRUST_STORE_PATH = "client.truststore";
            TRUST_STORE_PASSWORD = "1m_4_c0nn3c710n_p455w0rd";  //  Leet: I'm a connection password.
        } else {
            //  Read the config file
            HOST = config.getProperty("client.host", "localhost");

            // Parse the port, with error handling
            int tempPort = 8888;  // the server's default port
            try {
                tempPort = Integer.parseInt(config.getProperty("client.port", "8888"));
                //  Read the 'client.port' property. Default to 8888 if not found or invalid.
                if (tempPort <= 0 || tempPort > 65535) { //  Standard port range check
                    error("Invalid client.port in config. Using default 8888.");
                    tempPort = 8888;
                }
            } catch (NumberFormatException e) {
                error("Could not parse client.port. Using default 8888.");
            }
            PORT = tempPort;

            TRUST_STORE_PATH = config.getProperty("client.truststore.path", "client.truststore");
            TRUST_STORE_PASSWORD = config.getProperty("client.truststore.password", "1m_4_c0nn3c710n_p455w0rd");

            println("✅ Client configuration loaded:");
            println("   Host: " + HOST);
            println("   Port: " + PORT);
            println("   Truststore: " + TRUST_STORE_PATH);
        }
    }





    //   INSTANCE VARIABLES
    //   These are the "parts" of our "phone."
    private SSLSocket socket;       //  The secure, encrypted "phone line" itself.
    private PrintWriter writer;     //  An imported tool for SENDING messages TO the phone line.
    private BufferedReader reader;  //  An imported tool for RECEIVING messages FROM the phone line.


    //  =================================================================================================================

    //  PART 1:
    //  Setting up and managing the actual connection (the _03a_connect, _03d_sendRequest, _03e_disconnect methods).
    //  This is the "phone line" itself.

    //  =================================================================================================================


    /**
     *  We now establish a secure CONNECTION TO THE SERVER.
     *  This is the "dial" button. The _04_Main class calls this 'once' when the app starts.
     *     @throws IOException if it can't connect (e.g., server is off).

     *    Citation: "SSLSocketFactorys create SSLSockets. The default factory is initialized with the system properties
     *       javax.net.ssl.keyStore and javax.net.ssl.trustStore." - ORACLE Java Documentation
     *    Link: https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/SSLSocketFactory.html

     *    DigitalOcean - Java Socket Programming: https://www.digitalocean.com/community/tutorials/java-socket-programming-server-client
     */
    public void _03a_connect() throws IOException {
        //  STEP 1: Set up the "Guest List" (TrustStore) that contains the server's public certificate.
        //     We tell the Java Security system where to find our truststore file and what its password is.
        //     We let our security system know (setProperty) where to find the photo of the server's ID badge.
        //     We register in the System the location and password of our "truststore" file.
        System.setProperty("javax.net.ssl.trustStore", TRUST_STORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUST_STORE_PASSWORD);

        //  STEP 2: Create an SSLSocket Factory. Get the "factory" for building secure phone lines
        //     This is the special factory that knows how to create secure phone lines (SSLSockets).
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        //  STEP 3: "Dial" the server
        //     Create a connection w the SSLSocket.
        //     We create the SSLSocket, which is the secure "phone line" itself and connect it to the server's address.
        //     This is the moment the "handshake" happens, where the client checks the server's ID badge against the guest list.
        this.socket = (SSLSocket) factory.createSocket(HOST, PORT);

        //  STEP 4: Set up our "speaker" and "microphone" tools, and we attach them to the new "phone line."
        //     The auto-flush = 'true' means it sends messages immediately.
        this.writer = new PrintWriter(socket.getOutputStream(), true); //  SENDING
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));  //  RECEIVING

        //  All systems check:
        println("✅ Securely connected to Inventory Server.");
    }




    /**
     *  Sends a request to the server and returns the response.
     *  This is the "core" private method for sending 'any' message request and reading the response.
     *  All the other public methods (like _03b_attemptToLogin) will use this method to do the actual work.
     *     @param request The full command string to send (e.g., "LOGIN::...")
     *     @return The server's one-line response.
     *     @throws IOException if the "phone line" is cut.
     */
    public String _03b_sendRequest(String request) throws IOException {
        //  STEP 1: Safety check, to make sure the phone is connected.
        if (socket == null || socket.isClosed()) {
            throw new IOException("Not connected to the server.");
        }

        //  STEP 2: Log what we're about to send, for future debugging.
        println("Sending to server: " + request);

        //  STEP 3: "Speak" into the phone.
        writer.println(request);
// CODE IS STUCK HERE
        //  STEP 4: "Listen" for the reply.
        //  This .readLine() "blocks" (pauses) until the server sends a response.
        String response = reader.readLine();

        //  STEP 5: Log what we heard back.
        println("Received from server: " + response);

        //  STEP 6: Return the response to whoever asked.
        return response;
    }



    /**
     *  Closes the connection to the server.
     *  Hangs up the "phone." Called by _04_Main when the app is closing.
     */
    public void _03c_disconnect() {
        try {
            //  If the phone line exists and isn't already hung up...
            if (socket != null && !socket.isClosed()) {
                println("Disconnecting from server...");
                socket.close(); //  ...hang it up.
            }
        } catch (IOException e) {
            error("Error closing connection: " + e.getMessage());
        }
    }







    // -----------------------------------------------------------------------------------------------------------------
    //   PUBLIC METHODS
    //   These are the simple methods our controllers will call that build the command string and call _03d_sendRequest.
    // -----------------------------------------------------------------------------------------------------------------



    /**
     *  LOGIN ATTEMPT.

     *  Asks the server to verify a login.
     *  This method builds the specific "LOGIN" command string the server is expecting.
     *  It sends the user's password in PLAIN TEXT over the secure SSL connection.
     *  The SERVER is responsible for hashing this password and comparing it to the stored hash in the DB.
     *     @param email The user's plain-text email.
     *     @param plainTextPassword The user's plain-text password.
     *     @return The server's response (e.g., "SUCCESS|...")
     */
    public String _03d_attemptToLogin(String email, String plainTextPassword) {
        try {
            //  STEP 1: Build the protocol string.
            //     We format the request exactly as the server's _0_ClientHandler expects: "COMMAND::DATA"
            //     The data for a login is the email and password, separated by a pipe character.
            String request = "LOGIN::" + email + "|" + plainTextPassword;
            //  STEP 2: Send it.
            return _03b_sendRequest(request);
        } catch (IOException e) {
            //  If the network fails, return a "FAILURE" message in the same format our app expects.
            return "FAILURE: Network Error:  Could not contact the server. " + e.getMessage();
        }
    }



    /**
     *   SIGN-UP ATTEMPT.

     *  Asks the server to create a new user.
     *  This method deconstructs the ModelUser object and builds the "SIGNUP" command string.
     *  For security, the password in the ModelUser object MUST be hashed on the client 'before' this method is called.
     *     @param newUser A _10_ModelUser object with the new user's info.
     *     @return The server's response ("SUCCESS" or "FAILURE")
     */
    public String _03e_attemptToSignUp(_13_ModelUser newUser) {
        //  Canonical payload: name|email|pwdHash|phone|role   (no id)
        try {
            //  STEP 1: Build the Command (data part) of the string.
            //     The server expects the password to be already hashed at this point for security.
            String requestData = String.join("|",
                    //newUser.getId(),
                    //newUser.getRole(),
                    normalizeRole(safe(newUser.getRole())),        // "admin" or "staff"
                    safe(newUser.getName()),
                    safe(newUser.getPwdHash()), //  The 'hashed' password
                    safe(newUser.getEmail()),
                    digitsOnly(newUser.getPhone())
            );
            //  STEP 2: Build the full command.
            String request = "SIGNUP::" + requestData;
            //  STEP 3: Send it.
            return _03b_sendRequest(request);
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }


    private static String safe(String inpText) { return inpText == null ? "" : inpText.trim(); }

    private static String normalizeRole(String inpRole) {
        inpRole = (inpRole == null) ? "" : inpRole.trim().toLowerCase();
        return (inpRole.equals("admin") || inpRole.equals("staff")) ? inpRole : inpRole; // keep as-is; server will re-validate
    }

    private static String digitsOnly(String inpNumber) {
        return inpNumber.replaceAll("\\D+", "");
    }










    //=================================================================================================================

    //  PART 2: PRODUCTS

    //  Knowing the specific commands to send over the line (GET_ALL_PRODUCTS, ADD_PRODUCT, REMOVE_PRODUCT, etc.).
    //  This is the "list of things you can say on the phone" or the CRUD operations per se.

    //=================================================================================================================




    /**
     *  Asks the server for the full list of products.
     *    @return Server's response (e.g., "SUCCESS|product1;product2...")
     */
    public String _03f_getAllProducts() {
        try {
            return _03b_sendRequest("GET_ALL_PRODUCTS::");
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }



    /**
     *  Asks the server to add one new product.
     *    @param product The _11_ModelProd object to add.
     *    @return Server's response (e.g., "SUCCESS")
     */
    public String _03g_addProduct(_11_ModelProd product) {
        try {
            //  We use a helper to turn the object into a string
            String request = "ADD_PRODUCT::" + _03k_serializeProduct(product);
            return _03b_sendRequest(request);
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }



    /**
     *  Asks the server to remove one or more products.
     *    @param skusToRemove A List of SKU strings to delete.
     *    @return Server's response (e.g., "SUCCESS")
     */
    public String _03h_removeProduct(List<String> skusToRemove) {
        try {
            //  STEP 1: Join the list of SKUs with a comma (e.g., "sku1,sku2,sku3")
            String skus = String.join(",", skusToRemove);
            //  STEP 2: Build the command.
            String request = "REMOVE_PRODUCT::" + skus;
            //  STEP 3: Send it.
            return _03b_sendRequest(request);
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }



    /**
     *  Asks the server to update an existing product.
     *    @param updatedProduct The product object with the new data.
     *    @return Server's response (e.g., "SUCCESS")
     */
    public String _03i_updateProduct(_11_ModelProd updatedProduct) {
        try {
            String request = "UPDATE_PRODUCT::" + _03k_serializeProduct(updatedProduct);
            return _03b_sendRequest(request);
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }



    /**
     *  Asks the server to add a whole batch of products from a CSV.
     *    @param productsToLoad A List of _11_ModelProd objects.
     *    @return Server's response (e.g., "SUCCESS|5 products added.")
     */
    //  Field separator stays as "|" (used inside _03k_serializeProduct)
    private static final String FIELD_SEP   = "|";

    //  This is a product separator between rows
    //  ASCII Record Separator: very unlikely to appear in normal text and never typed by humans.
    private static final String RECORD_SEP  = "\u001E"; //

    public String _03j_bulkAddProducts(List<_11_ModelProd> productsToLoad) {
        try {
            //  STEP 1: Turn each product object into its string format.
            List<String> productStrings = productsToLoad.stream()
                    .map(this::_03k_serializeProduct) //  (e.g., "cat|sku|name...")
                    .collect(Collectors.toList());

            //  STEP 2: Join all those product strings with a Semicolon, (e.g., "prod1;prod2;prod3")
            //String bulkData = String.join(";", productStrings);  //  BUG!
            //  TODO: Replace, bc ";" has an issue with Descriptions that include ";" in their text. It messes the fields up.
            //  Using The U+001E Record Separator (RS) we separate logical records in data streams.
            //  RS is non-printable and typically treated like a delimiter; programs don't normally treat it as a line break.
            //  Note: It's not tied to line breaks (LB) or carriage returns (CR), which in windows both are New Line: "\n".
            String bulkData = String.join(RECORD_SEP, productStrings);

            //  STEP 3: Build the final command.
            String request = "BULK_ADD_PRODUCTS::" + bulkData;

            //  STEP 4: Send it.
            return _03b_sendRequest(request);
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }



    /**
     *  Private helper to turn a Product object into our protocol string.
     *  Format: category|sku|name|description|quantity|price
     */
    private String _03k_serializeProduct(_11_ModelProd product) {
        //  String.join is a clean way to combine text with a "|"
        return String.join("|",
                product.getSku(),
                product.getBrand(),
                product.getProdName(),
                product.getDescription(),
                String.valueOf(product.getQuantity()), //  Convert int to string
                String.valueOf(product.getPrice()),      //  Convert double to string
                product.getImageUrl()
        );
    }










    //=================================================================================================================

    //  PART 3: USER & SUPPLIER METHODS (for Admin)

    //=================================================================================================================


    /**
     *  Asks the server for the full list of users.
     *    @return Server's response (e.g., "SUCCESS|user1;user2...")
     */
    public String _03l_getAllUsers() {
        try {
            return _03b_sendRequest("GET_ALL_USERS::");
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }



    /**
     *  Asks the server to remove one or more users.
     *    @param userIdsToRemove A List of user ID strings.
     *    @return Server's response (e.g., "SUCCESS")
     */
    public String _03m_removeUsers(List<String> userIdsToRemove) {
        try {
            String ids = String.join(",", userIdsToRemove);
            String request = "REMOVE_USER::" + ids;  //  from "_02_ServerClientHandler" -> case "REMOVE_USER":
            return _03b_sendRequest(request);
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }



    /**
     *  Asks the server to update a user's info.
     *    @param user The _10_ModelUser object with the new data.
     *    @return Server's response (e.g., "SUCCESS")
     */
    public String _03n_updateUser(_13_ModelUser user) {
        try {
            //  Build the data string "id|role|name|pwdHash|..."
            String requestData = String.join("|",
                    user.getId(),
                    user.getRole(),
                    user.getName(),
                    user.getPwdHash(),
                    user.getEmail(),
                    user.getPhone(),
                    String.valueOf(user.getLoginAttempts())
            );
            String request = "UPDATE_USER::" + requestData;
            return _03b_sendRequest(request);
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }



    /**
     * Asks the server to add a new supplier.
     * @param supplier The _15_ModelSupplier object to add (ID should be null).
     * @return Server's response (e.g., "SUCCESS")
     */
    public String _03p_addSupplier(_15_ModelSupplier supplier) {
        try {
            //  Build the data string "company|contactName|email|phone|address|notes"
            //  We OMIT the ID, as the database will generate it.
            String requestData = String.join("|",
                    supplier.getCompanyName(),
                    supplier.getContactName(),
                    supplier.getPhone(),
                    supplier.getEmail(),
                    supplier.getAddress(),
                    supplier.getNotes()
            );
            //  Create the command string "ADD_SUPPLIER::data..."
            String request = "ADD_SUPPLIER::" + requestData;
            return _03b_sendRequest(request);
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }



    /**
     * Asks the server to remove one or more suppliers.
     * @param supplierIdsToRemove A List of supplier ID strings.
     * @return Server's response (e.g., "SUCCESS")
     */
    public String _03q_removeSuppliers(List<String> supplierIdsToRemove) {
        try {
            //  Join all IDs with a comma, e.g., "id1,id2,id3"
            String ids = String.join(",", supplierIdsToRemove);
            //  Create the command string "REMOVE_SUPPLIER::id1,id2,id3"
            String request = "REMOVE_SUPPLIER::" + ids;
            return _03b_sendRequest(request);
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }



    /**
     * Asks the server to update a supplier's info.
     * @param supplier The _15_ModelSupplier object with the new data.
     * @return Server's response (e.g., "SUCCESS")
     */
    public String _03r_updateSupplier(_15_ModelSupplier supplier) {
        try {
            //  Build the data string "id|company|contactName|address|email|phone"
            String requestData = String.join("|",
                    supplier.getId(),
                    supplier.getCompanyName(),
                    supplier.getContactName(),
                    supplier.getAddress(),
                    supplier.getEmail(),
                    supplier.getPhone(),
                    supplier.getNotes()
            );
            //  Create the command string "UPDATE_SUPPLIER::data..."
            String request = "UPDATE_SUPPLIER::" + requestData;
            return _03b_sendRequest(request);
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }

    /**
     *  Asks the server for the full list of suppliers.
     *    @return Server's response (e.g., "SUCCESS|supplier1;supplier2...")
     */
    public String _03o_getAllSuppliers() {
        try {
            return _03b_sendRequest("GET_ALL_SUPPLIERS::");
        } catch (IOException e) {
            return "FAILURE: Network Error: " + e.getMessage();
        }
    }

    //  (We would add _03p_addSupplier, _03q_removeSupplier, etc. here)
}