package com.ims;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

import static com.ims._00_Utils.*;

//  Import our "toolbox" and "Lego brick" builder





/*  VERSION 2:
 *  This is the main show, the public face of our program. It's where all the buttons, text boxes, and tables live.
 *   It used to be that our main function would just print a menu to the screen and wait for input, but with JavaFX,
 *      we have a proper graphical user interface (GUI). The InventoryApp class is the conductor of this orchestra.
 *   This class is responsible for setting up the stage (the main window), arranging all the components (the UI), and
 *      listening for the user to do something (like clicking a button or typing in a text box).
 *   When a user does something, this class translates that action into a command for the DatabaseManager to handle.
 *   It doesn't know how the data gets added or removed from the database; it just knows to ask the DatabaseManager to do it.
 *   This clean separation of concerns (the UI from the business logic) keeps the UI code from becoming a tangled mess.
 *   We will use a Start and Stop methods to guarantee that the setup and teardown are handled in a structured, predictable way.
 */


/**   VERSION 3:
 *  This class is the entry point for the JavaFX client application.
 *  It sets up the UI, handles scene switching, and manages the application lifecycle.

 *  This is the "Theater Manager" or "Greeter at the Door."
 *  Think of this class as the person who opens the theater, turns on the lights, and pulls the curtain so the audience
 *     (our user) can see the stage (our login screen).
 *  This is the entry point ".java" file we run to start the CLIENT application.
 *  This class is the "Stage Manager" for our entire client application. It sets up the initial login scene and then
 *    hands off control to other classes.

 *  Its job is to:
 *    1. Start the JavaFX system.
 *    2. Try to connect to the server (using the communicator = _03_ClientServerLine._03a_connect).
 *    3. If successful, build and show the 'first scene' (the Login/Signup Screen).
 *          1. Finds the FXML file that describes the Login screen (login.fxml).
 *          2. Asks JavaFX to 'build' that screen from the FXML (FXMLLoader.load).
 *          3. Places the built screen on the primary window (Stage) and shows it.
 *    WHY FXML?  It lets us describe the UI (buttons, fields, layout) in XML, while the Controller class (_21_CtrlLogin)
 *       holds the "brains" that run when the user clicks buttons or types into fields.
 *    IMPORTANT: If anything goes wrong (file not found, controller error, CSS missing), we print a very clear error to
 *       the console so it is easy to diagnose.  Beginners should always get a loud, specific error.

 *    4. Controller Takes Over:
 *          This class does NOT know how to build the main app. It hands off all control to either _21_CtrlLogin or _22CtrlSignup.
 *          The user enters their credentials (email and password) and clicks "Login" or "Sign Up".
 *          The _21_CtrlLogin/_22CtrlSignup controller handles the input, and using the _03_ClientServerLine to talks the server.
 *          It does not talk to the DB. Instead, it calls (sends the credentials to) a new "_03d_attemptToLogin" method.
 *    5. Server Verifies: The "_03_ClientServerLine" sends a "LOGIN" request to the server.
 *          The _02_ServerClientHandler on the server receives this, calls ModelUserDB to check the credentials against the database,
 *             and hashes the provided password to compare it.
 *    6. Server Responds: The server sends back a simple string: "SUCCESS" or "FAILURE".
 *    7. Client Reacts:
 *          7.1. If the response is "FAILURE", the _21_CtrlLogin controller shows an error alert.
 *          7.2. If the response is "SUCCESS", the _21_CtrlLogin controller tells the _0_Main class to switch scenes,
 *               hiding the login screen and showing the main inventory table view.
 *    8. It also handles the "Stop" button (e.g., closing the window).

 *  YOUTUBE: Cave of Programming – JavaFX Playlist (https://www.youtube.com/playlist?list=PLTjeZIbc45NJcBCqbMNoDkobzr2GdjI0C)
 *  These videos walk through building a JavaFX app from scratch: main class, Stage, Scene, FXML, controllers, CSS.
 *  It lines up almost one-to-one with this class.

 *  YOUTUBE: Telusko – JavaFx Tutorial For Beginners 1 – Introduction to JavaFx (https://www.youtube.com/watch?v=9YrmON6nlEw)

 *  WEBSITE: Jakob Jenkov – JavaFX Tutorial (https://jenkov.com/tutorials/javafx/index.html)
 *  *  Covers the JavaFX Application lifecycle, Application.launch(), start(Stage primaryStage), and how Stage vs Scene vs nodes work.
 */
public class _04_Main extends Application {
    /**
     * App entry point.
     * - Starts JavaFX.
     * - Loads login.fxml from the classpath (src/main/resources).
     * - Shows the Login window.
     */


    //   GLOBAL SETTINGS

    //   These are public "static" so other classes can see them.
    //   TODO: If I ever want to read these from a config, they might be better off safe in one place.
    public static int wdScene = 1200, htScene = 740;
    public static String topTitle = "Inventory Management System";

    //  File paths for our "blueprints" in the "resources" folder.
    public static final String LOGIN_FXML = "/login.fxml";
    public static final String CSS_STYLE = "/styles.css";
    //  YOUTUBE: Telusko – Styling with CSS in JavaFX (https://www.youtube.com/watch?v=lL1HHWTBZm4)
    //  He explains how to hook up a CSS file to a Scene (like your sceneLogin.getStylesheets().add(...))



    //  INSTANCE VARIABLES

    //  The reference to the main application window (the "theater stage").
    private Stage primaryStage;

    //  The one and only "phone line" (network communicator) for the entire application.
    private _03_ClientServerLine clientServerCommunicator;

    /**
     *  The main() method. This is the official "front door" for a Java program, but in JavaFX, all it does is call launch().
     *     @param args Command-line arguments.
     */
    public static void main(String[] args) {
        //  Step 1: This is the JavaFX bootstrap command to start the whole system.
        //     It will automatically create a _04_Main object and then call its 'start()' method.
        launch(args);
    }

    /**
     *  This is the 'real' start of our app.
     *  The JavaFX runtime calls this method 'after' launch() has been called above.
     *     @param primaryStage The main "stage" (window) that JavaFX builds for us, for our application.
     */
    @Override
    public void start(Stage primaryStage) {
        //  We have to save a copy of (a reference to) the main stage, so we can use it later (e.g., to set the title).
        this.primaryStage = primaryStage;

        try {
            println("Client app starting...");  //   Just a heads-up

            // ==================================================================================
            //  STEP 1: PREPARE THE NETWORK CONNECTION
            // ==================================================================================
            //  Before we show any UI, we must establish a connection to our server.
            //  We create a new instance of our dedicated network manager.
            //  Step 2.1: Create our one and only "phone line."
            println("Attempting to create communicator...");
            this.clientServerCommunicator = new _03_ClientServerLine();

            //  Step 2.2: "Dial" the server.
            //  This 'try' block is critical.
            //  If this _03a_connect() method fails, it will throw an 'IOException', and our 'catch' block will handle it.
            println("Attempting to connect to server (SSL Handshake happens here)...");
            clientServerCommunicator._03a_connect();
            println("\"_03a_connect()\" connection completed without throwing IOException.");



            // =================================================================================
            //  STEP 2: BUILD THE INITIAL LOGIN SCREEN:  Theater play
            // =================================================================================
            //  If the connection was successful, we proceed to show the user the login screen.
            //  We call our own helper method to keep this 'start' method clean and easy to read.
            //  The logic for this is complex, so I moved it to its own separate method.
            println("Attempting to show login screen...");
            _04a_showLoginScreen();
            println("\"_04a_showLoginScreen()\" method finished.");


        } /* Normal:
        catch (IOException e) {
            //  !! EMERGENCY PLAN !!
            //  This 'catch' block only runs if _03a_connect() failed: if we can't connect to the server at all.
            error("Failed to connect to the server.");

            //  Step E.1: Show a pop-up alert to the user.
            _30i_showError(
               "Connection Failed", "Could not connect to the server.\nPlease ensure the server application is running."
            );

            //  Step E.2: Close the app, because it's useless (it cannot function) without a server connection.
            Platform.exit();
        }*/
        //  Testing:
        catch (IOException e) { // Catch network/SSL specific errors
            // ---> ADD THESE LOGS <---
            error("CAUGHT IOException in start().");
            error("This likely means the SSL t@g.connection failed (TrustStore issue?).");
            error("Error message: " + e.getMessage());
            e.printStackTrace(); // Prints detailed error trace

            println("Showing error dialog...");
            // ---> END OF ADDED LOGS <---

            _30_GUI_Builder._30i_showError( // Make sure your GUI Builder class name is correct
                    "Connection Failed",
                    "Could not connect to the server.\n" +
                            "Please ensure the server application is running and the truststore is correct.\n" +
                            "Error: " + e.getMessage() // Show error in dialog too
            );

            // ---> ADD THIS LOG <---
            println("Calling Platform.exit()...");
            Platform.exit();

        } catch (Exception e) { // Catch ANY other unexpected error during startup
            // ---> ADD THESE LOGS <---
            error("CAUGHT UNEXPECTED Exception in start().");
            error("This could be FXML loading, CSS, or something else.");
            error("Error message: " + e.getMessage());
            e.printStackTrace(); // Print detailed error trace
            // ---> END OF ADDED LOGS <---

            // Show a generic error if something else went wrong
            _30_GUI_Builder._30i_showError("Application Startup Error",
                    "A critical error occurred: " + e.getMessage());

            Platform.exit();
        }
    }

    /**
     *  This helper method builds the Login Scene from the FXML "blueprint" and shows it on the stage.
     *  It handles all the steps for loading that screen and displaying it in the main window.
     *     @throws IOException If it can't find the .fxml file.

     *   YOUTUBE: FreeCodeCamp – Java Full Course / Networking & Server Sections (SOCKETS)
     *   Main course page: 👉 https://www.freecodecamp.org/news/object-oriented-programming-in-java/
     */
    public void _04a_showLoginScreen() throws IOException {

        //  ==================================================================================
        //  STEP 3: Build the USER INTERFACE's Login Screen from the blueprint.
        //  ==================================================================================
        //  PRIMARY STAGE (the physical stage that the theater offers): USE THE FXML BLUEPRINT
        //     1. We tell JavaFX where to find our designs for the login "screen" ("src/main/resources/").
        //     2. We instantiate the FXML LOADER with the "login.fxml" prototype.
        //     3. The FXMLLoader is the "construction crew" and we give them the blueprint for the login UI.
        //        They know how to read blueprints.
        FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_FXML));

        //  STEP 3.1: "BUILD" THE SCENE
        //     To finish the construction of the UI-screen, we inject it to the 'root' object in our finished UI,
        //        with all the buttons and text fields. The complete model or prototype.
        //     We tell the "crew" to .load() the blueprint.
        //     This creates all the buttons, text fields, etc., and puts them inside a "Parent" container called 'root'.
        Parent root = loader.load();

        //  STEP 3.2: GET THE "DIRECTOR" (CONTROLLER). The 'most important' part.
        //     We ask the "crew" (loader) for the controller it just created, and call it controller.
        //     From the new GUI-screen we get a reference to its "brain" a new Controller (an instance of _21_CtrlLogin).
        //     This controller is a special "director" class that we use to manage the buttons (actions) of our login screen.
        //     When we click a button, the controller will know that to do (as an event handler).
        _21_CtrlLogin controller = loader.getController();


        //  =======================================================================================
        //  STEP 4: "INJECT" THE TOOLS
        //  =======================================================================================
        //  DEPENDENCY INJECTION
        //     That new controller is "empty." It doesn't have its own "phone."
        //     We must 'give' it the tools it needs to do its job.
        //     We prepare the tools needed for the Login Screen to work. We prepare the data parts.
        //     We need to talk to the database to check if a user's login info is valid.
        //     So, we open (initialize) a single connection to our database as part of "this" object.
        //     We give the director the tools it needs: a way to talk to the Stage Manager (_04_Main) and a phone line
        //        (_03_clientServerLine) to call the server.
        controller._21a_initialize(this, this.clientServerCommunicator);


        //  ================================================================================================
        //  STEP 5:  PUT THE SCENE ON STAGE
        //  ================================================================================================
        //  STEP 5.1: Create the "Scene" (the set design) and give it the 'root' container.
        //     Set the set for Act 1 (the login screen). Put the UI on the stage and show it to the client user.
        //     In JavaFX, the 'Stage' is the main window itself (like a theater stage).
        //     The 'Scene' is the content we put inside the window (like the set for a play).
        //     We take our fully built UI (the "/login.fxml" we had placed on the 'root') and bundle it up into a Scene.
        //     Put the finished set piece prototype into a "Scene" and set the stage.
        Scene sceneLogin = new Scene(root);

        //  STEP 5.2: Attach our CSS "style guide" to the scene.
        //     To make it look good, we attach our CSS stylesheet to the scene. We control all the colors, fonts, and layout styles.
        sceneLogin.getStylesheets().add(getClass().getResource(CSS_STYLE).toExternalForm());

        //  STEP 5.3: Set the window title.
        //     The stage (main window) gets a TITLE: This is like putting the play's name on the marquee.
        primaryStage.setTitle("ACME Inventory Login");

        //  STEP 5.4: Act 1 - Scene 1
        //     Put the new scene onto the main window.
        //     Now we tell the main window (primaryStage) to display our login scene.
        //     Showing the stage is like opening the curtains for the audience.
        primaryStage.setScene(sceneLogin);

        //  STEP 5.5: "CURTAINS UP!"
        //     Make the window visible to the user.
        //     Curtains up, it's showtime!
        //     This command makes the window visible to the user: DISPLAY THE STAGE
        primaryStage.show();
    }



    /**
     *  Our last chance to do cleanup.
     *  This "cleanup" method is called by JavaFX runtime 'automatically' just before the application (the main window) closes.
     *  It's our last chance to release any resources cleanly: It's designed for cleanup tasks.
     *  This is where we close the DB connection, ensuring that all resources are properly released before termination.
     */
    @Override
    public void stop() {
        println("Application is closing...");
        //  Step 1: Check if our "phone" is connected.
        if (clientServerCommunicator != null) {
            //  Step 2: If it is, "hang up" politely.
            clientServerCommunicator._03c_disconnect();
        }
        println("Cleanup complete. Goodbye.");
    }
}


//  Supplier test:      Acme Inc.       Joe Doe     123 Any St      joe@acme.com        123-456-7890