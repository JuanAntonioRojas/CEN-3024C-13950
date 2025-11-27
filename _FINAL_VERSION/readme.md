The Story: How This Program Got Built

This project didn't start out all complicated. It's had a "v2" and now a "v3."

1: The "Old Way" (AKA The Monolith)

At first, I built this as a simple, 2-tier app. The "classic way" we learned in our first programming class.
    1. The Frontend View (JavaFX): This was the user's screen. The buttons, text fields, tables... etc.
    2. The Backend Logic and Database (MySQL): This is where the data lived, and got manipulated.
When you clicked a button (like "Login" in the old ControlLogin), the code in that same file would just reach right out, connect directly to the database, and check your password.
This was like giving every single bank teller a key to the main vault. It "worked," but I realized it was a terrible idea:
    • It was messy: My "view" code was all tangled up with "database" code.
    • It was risky: My database password was just sitting there in the app. If anyone got the app, they had the keys to the kingdom. In fact, in my very first version I had the Username and Password, hardcoded into the code. Big mistake.
    • It was a nightmare to change: If I wanted to change one little thing about the database, I had to go hunt down every single file that touched it. No thank you.
2: The "New Way" (The 3-way Split)
This is where the 3-Tier Architecture comes in. This was the "Aha!" moment for me. [Sommerville, Ch. 17, talks about 3-Tier/Layered Architecture, on “separation of concerns”].
I decided to split the project into two separate pieces that talk to each other over a "phone line."
    1. The Client (The "Storefront"): This is the JavaFX app. It's the "Presentation" tier. It's got no “brains” (logic) in it. It’s as dumb as a box of rocks, but on purpose. It doesn't know anything about the database, because of this new “separation of concerns.”
    2. The Server (The "Back Office" & "Vault"): This is a whole new, separate program. It holds the "Logic" tier and the "Data" tier. It's the only thing in the world that can grab a hold of the key to the database, now placed in a config file.
Now, when you click that "Login" button, the bank teller (_21_CtrlLogin) doesn't run to the vault. He just sends a message on his little computer terminal (the _0_ClientServerCommunicator) to the secure back office (the Server).
The server "boss" (_0_ClientHandler) gets the message, tells the "accountant" (_10_ModelUserDB) to check the password, and just sends a simple "Yep, they're good" or "Nope, they're a fraud" back to the teller.
This approach is not only a lot cleaner, but it’s safer and more professional grade.


Keeping Things Secure

Since I was building a "phone line" between the client and server, I had to make sure nobody could "listen in."
1. The Encrypted Phone Line (SSL/TLS)
In the old days this communication used to be done with Secure Socket Layers, SSL, but this was improved on TLS. I'm using what they conventionally call SSL/TLS for the connection. This is the same magic that puts the little padlock 🔒 next to a website address in the browser.
    • The Server has a keystore file. Think of this as its official, un-fakeable "Company ID Badge."
    • The Client has a truststore file. This is like a "Guest List" that only has the server's ID badge on it.
When the client "calls" the server, it checks the server's ID badge against its guest list. If it matches, it creates a secret, encrypted "tunnel" that only “they” can understand. Now, even if a hacker taps the line, all they hear is scrambled gibberish.
2. Password Scrambling (This is the cool part!)
You can never, ever store a password. Not even on the server. You have to store an encrypted "hash."
But I didn't just use any old hash. Old ways like MD5 or SHA-1 are too fast to crack. A hacker with a good computer can guess billions of passwords per second.

I'm using BCrypt (Utils.java). [W3Schools or Codecademy teach a lot on "Password Hashing" and "BCrypt"]
BCrypt is slow on purpose. It's like putting the password inside a 1,000-pound safe with a 12-hour time lock.
    • When a new user is signed up (_22_CtrlSignup), the client app hashes the password using BCrypt with Salt and Cost.
    • It sends that un-guessable hash to the server. The server never even sees the real password.
    • When you log in later, you type your plain password. The server takes it, finds the hash, and uses BCrypt's verify() function to see if they match. The server is the only one who can verify the password, and to do that, it needs two things: the password and the stored hash. 
Even if a hacker steals my entire database, all they get is a list of these super-slow-to-crack hashes. They'll probably just give up.


4. Brute Force Protection

In my _10_ModelUserDB on the server, I added a login attempt counter. If you get the password wrong, the server makes a note. If you do it 5 times... OOPS! The account gets locked… Out of luck: “Go see the Admin.”
This stops those "brute force" robots that just try to guess your password (admin, 12345, password...) a million times a second.



The Class Breakdown

Here's the rundown of the most important files and what they do. This grouping system is super clear just by looking at the first number:
    • 0X_ Classes: This is the "Plumbing" (Core, Server, Network).
    • 1X_ Classes: This is the "Data" (Models and DB classes).
    • 2X_ Classes: This is the "Brains" (Controllers).
    • 3X_ Classes: This is the "Lego Bricks" (View builders).
NOTE: Each and every class has methods and each and every method is prefixed with the class prefix followed by a letter. E.g. _00d_Verify(){…} or _21b_handleLogin(){…}.


0X_ (The "Plumbing & Wiring")

_00_Utils: This is the first file in the list, the "zeroth" thing for everything else to stand on. It's the foundation, a shared toolbox that contains all the handyman utilities (like the hash() and verify() password tools) that both the Client and Server need.
_01_InventoryServer: This is the big boss. You run this file first to start the server. It opens the "warehouse" (the port), hooks up the "master ledger" (database connection), and hires a "team of workers" (the ExecutorService). After that, it just waits for the phone to ring.
_02_ClientHandler: This is the server "worker" who answers the phone. The boss (_01_InventoryServer) hires a brand new one for every single client that calls. This worker's job is to listen for commands ("LOGIN::...", "GET_ALL_PRODUCTS::...") and route them to the right "department specialist" (the _1X_ DB classes).
_03_ClientServerCommunicator: This is the most important class in the client. This is the only phone in the entire building. Instead of every controller trying to make its own network connection (the "old way"), they all just use this one guy. This is a super-important concept called Dependency Injection. We "inject" (pass) this communicator to any controller that needs to talk to the server. It keeps everything clean as a whistle.
_04_Main: The "theater manager." His only job is to open the theater doors, turn on the lights, and set up the very first scene: the Login Screen. That's it. He doesn't know how to build the main app or any other scene. He just kicks things off and hands control over to the login controller.
_05_ConfigLoader: This is the server's "little black book." It's a simple helper that reads the config.properties file. This is where we keep all the secret stuff—like the database password and the keystore password—so we're not hard-coding our secrets right into the app. That's a big no-no.


1X_ (The "Data Files & Accountants")

_11_ModelProd, _13_ModelUser, _15_ModelSupplier: These are the "data buckets" (Models). They're just simple classes that hold data. They don't do anything. A _13_ModelUser object just holds one user's info (name, email, etc.).
_12_ModelProdDB, _14_ModelUserDB, _16_ModelSupplierDB: These are the "department specialists" or "accountants." They are the Data Access Objects (DAO).
    • The _14_ModelUserDB guy knows everything about the _13_ModelUser object (how to add 'em to the database, how to check their password, etc.).
    • The _12_ModelProdDB guy knows everything about _11_ModelProd.
    • The _16_ModelSupplierDB guy knows everything about _15_ModelSupplier.
These three classes are the only ones allowed to touch the database. They are the only ones who speak SQL.


2X_ (The "Brains" / Controllers)

_21_CtrlLogin: This is the "front desk" clerk. It handles the login button. It grabs the email & password and hands them to the _03_ClientServerCommunicator. When it gets a "SUCCESS" response from the server, it doesn't build the main screen itself. Instead, it just makes the call to the _23_MainSceneBuilder and says, "This fella's good. Build 'em the main screen!"
_22_CtrlSignup: Another "front desk" clerk. It handles the sign-up pop-up. It grabs all the new info, tells _00_Utils to hash the password, and hands the new _13_ModelUser object to the _03_ClientServerCommunicator, which in turn passes it to the _02_ClientHandler, which then tells the _14_ModelUserDB to stick it in the database.
_23_MainSceneBuilder: The "Master Assembler" on the factory line. The _21_CtrlLogin clerk doesn't know how to build the complicated main screen, so it delegates the job to this specialist.
    • He doesn't make the bricks; he grabs them.
    • He grabs the "car chassis" from _32_ViewFrame.
    • He grabs the "engine" from _33_ViewTables.
    • ...and most important... He grabs the "computer brain" (like _24_CtrlProd) and all the "wires" (_26_CtrlTableActions). His job is to connect the View to the Brains.
_24_CtrlProd, _25_CtrlUser, & _26_CtrlSupplier: These are the main app's "department managers." When you're logged in, these guys handle all the real work for their one department, like "Add Product," "Remove User," etc. But again, they don't do it themselves! They just package up the request (like a _11_ModelProd object) and give it to the _03_ClientServerCommunicator to send off to the server.
_27_CtrlRequisition.java: This class's only job will be to build and show a pop-up "requisition form" a real-deal business rule that separates a simple "data-in, data-out" app from a smart business tool.


3X_ (The "Lego Bricks" / View Helpers)

These are all my "View" helpers. I wrote these so I would Not Repeat Myself (DRY principle).
_31_ViewGUIBuilder: Why build a button from scratch every time? This class is a factory that makes me a styled button (_31f_Btn) or text field (_31b_TextField) every time.
_32_ViewFrame: This builds the main window's layout (the header, the left-nav bar, and the big empty space in the center for the table).
_33_ViewTables: This is a factory that builds the whole table (like the product table, all nicely formatted with its columns).
_34_ViewColumnFactory: This is a helper for my helper! It builds the individual columns for the table, and it's smart enough to right-align and format my numbers (like $1,200.50).
_35_CtrlAdd: This is my "Pop-Up Window" factory. It's a super-clever, reusable class. Instead of building a "New Product" window and a "New User" window from scratch, I just use this one class. I just tell it what fields to show, and POOF!—it builds a perfect pop-up dialog box for me. It's the king of my "Don't Repeat Yourself" strategy.



Testing Strategy (How We'll Check Our Work)

You can't just build a car and hope the brakes work, right? You gotta test it. Here's the plan. [Sommerville here on Testing, Ch. 8-9]

1. Unit Testing (Checking the Parts)
This is where we test the tiniest little pieces all by themselves to make sure they're not broken. We use a tool called JUnit for this. It's like checking the spark plugs before they go in the engine.
Example: Testing my Utils class
I would create a this new file called _50_MainTests

import org.junit.Test;
import static org.junit.Assert.*;

public class _50_MainTests {

    @Test
    public void testPasswordHashing() {
        String password = "IAmPassword123";
        String hash = Utils.hash(password);
        
        // Test 1: Is the hash not empty?
        assertNotNull(hash);
        
        // Test 2: Does the original password "verify" against the hash?
        assertTrue(Utils.verify(password, hash));
        
        // Test 3: Does a WRONG password fail?
        assertFalse(Utils.verify("WrongPassword!", hash));
    }

    @Test
    public void testEmailValidator() {
        assertTrue(Utils.isValidEmail("good@email.com"));
        assertFalse(Utils.isValidEmail("bademail.com"));
        assertFalse(Utils.isValidEmail("bad@email"));
        assertFalse(Utils.isValidEmail(null));
    }
}



2. Integration Testing (Checking How Parts Fit)

This is where we see if the pieces work together. Can the "client phone" really talk to the "server operator" and get a real answer from the "database guy"? We're testing the integration of the layers.
Example: Testing the real Login Flow:
This test would  START THE SERVER first, then run this:

@Test
public void testFullLoginIntegration() {
    // 1. Create the client's "phone"
    _0_ClientServerCommunicator communicator = new _0_ClientServerCommunicator();
    
    try {
        // 2. "Dial" the server
        communicator._0a_connect();
        
        // 3. Send a REAL login request for a user we know is in the test DB
        String response = communicator._0b_attemptToLogin("real_user@test.com", "real_pass123");
        
        // 4. Check the server's REAL response!
        assertTrue(response.startsWith("SUCCESS"));
        
        communicator._0e_disconnect();
        
    } catch (IOException e) {
        fail("Test failed due to network error: " + e.getMessage());
    }
}



3. User Interface (UI) Testing (The "Robot Test Drive")

This is the final test drive. We use a robot (a framework called TestFX) to actually launch the app, click the buttons, and type in the fields, just like a real person.
Example: Testing the Login Screen with TestFX:
This is a test that launches the full JavaFX app

public class LoginScreenTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        // Tell TestFX to launch our main app
        new _0_Main().start(stage);
    }
    
    @Test
    public void testSuccessfulLogin() {
        // 1. Robot types in the email field
        clickOn("#emailField").write("admin@test.com");
        
        // 2. Robot types in the password field
        clickOn("#passwordField").write("adminpass");
        
        // 3. Robot clicks the login button
        clickOn("#loginButton");
        
        // 4. THE TEST: Did the product table (from the main scene) show up?
        // We look for a component with the ID "#productTable"
        verifyThat("#productTable", isVisible());
    }
}



Project Management  (How We're Managing This Whole Thing)

A project this big did get real messy, real fast. You can't just... start, as I naively did in the beginning. You need a plan.
We're using an Agile method [Sommerville on Agile, Ch. 3] because “things change” all the time.
Instead of one giant, scary deadline, we break the work into small "sprints" (like 1- or 2-week mini-projects).
    1. Sprint 1: "Just get a window to open. Oh, and get the database to connect."
    2. Sprint 2: "Build the server and client. Get them to talk. Get the login to work."
    3. Sprint 3: "Build all the 'Product' stuff. Add, remove, update, refresh."
    4. Sprint 4: "Build all the 'User' stuff for the admin to manage."
    5. Sprint 5: "Testing, writing this README, and cleaning up the mess."
We're tracking all this work in a tool like Jira (or Trello, or Asana, whatever). All our "chores" (tasks, bugs, new ideas) go into a "Product Backlog." For each sprint, we just grab a few chores, put 'em in the "Sprint Backlog," and get 'em done.
Easy as pie.



How to Run This Thing

1. Run the Server (The "Back Office")
    1. We made sure we had a MySQL database set up.
    2. Next, we went into the server-side code and find/create config.properties. I filled this in with the database URL, username, and password.
    3. I also needed to create the server.keystore (the server's ID badge) and the client.truststore (the client's guest list) files.
    4. Finally we run the main method in _0_InventoryServer. We should see a message saying it's "Waiting for client connections..."
    
2. Run the Client (The "Storefront")
    1. We made sure the client.truststore file is in the client's main folder so it can find it.
    2. Then we run the main method in _0_Main.
    3. The login screen should pop up.
    4. Finally we try to log in (or sign up for a new account).

