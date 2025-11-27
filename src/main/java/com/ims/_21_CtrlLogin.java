package com.ims;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

//  Import our "toolbox" and "Lego brick" builders
import static com.ims._00_Utils.*;
import static com.ims._20_ValidBusinessRules.*;
import static com.ims._30_GUI_Builder.*;

/**
 *  This is the "Front Desk Clerk" for the login screen.
 *  This comprehensive file is the most critical for the application's login flow. This is linked to the login.fxml.
 *  Its only job is to handle the login button by talking to the _03_ClientServerLine. In other words,
 *     to handle what happens when a user clicks the 'Login' or 'Sign Up' buttons.

 *   It incorporates several critical security practices (like using BCrypt, timing attack defense, and attempt limiting).
 *   It begins with (first screen) the authentication gate (ACME), which gathers the credentials from the UI fields.

 *   This "bank teller" no longer has a key to the vault.
 *   Her computer terminal (_03_ClientServerLine) can only send messages to the secure back office (Server).
 *   She sends the customer's ID and request. A vault manager in the back office (_02_ServerClientHandler) does all the
 *     security checks and ledger updates and then sends a simple "Approved" or "Denied" message back to the teller's screen.

 *  As a 'controller', her job is to:
 *     1. Collect username and password from the user. Get the user's info from the text fields.
 *     2. Send that information to the server for verification. Give that data to the "phone" (_03_ClientServerLine).
 *     3. Get the server's response (e.g., "SUCCESS" or "FAILURE").
 *     4. Act on that response (either show the main app or display an error) ,e.g., call the _23_CtrlMainSceneBuilder to build
 *        the main app, or show a pop-up error.

 *   This controller NEVER talks to the database directly. That's the server's job now.
 */

public class _21_CtrlLogin {

    //  FXML "INJECTIONS"

    //  The '@FXML' tag is magic. It connects these 'private' variables directly to the components in the FXML blueprint.
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    //  We need a reference to the button (or any item in the scene) so we can find the 'Stage' (the main window) later.
    @FXML private Button loginButton;


    //  INSTANCE VARIABLES

    //  A reference back to the "Theater Manager" (_04_Main), the main application class.
    private _04_Main mainApp;


    //  A reference to the "phone line" (_03_ClientServerLine).
    private _03_ClientServerLine clientServerLine;


    /**
     *  This initializer method is called right after loading the FXML (by the "_0_Main" class) to give this controller
     *     the tools it needs to do its job, a reference to itself and the communicator.
     *  This is the "tool-giving" method, NOT a constructor.
     *  The _04_Main class calls this after loading the FXML to "inject" (give) this controller the tools it needs.
     *  @param mainApp A reference to the main application class.
     *  @param communicator The one and only "phone line" to the server.
     */
    public void _21a_initialize(_04_Main mainApp, _03_ClientServerLine communicator) {
        /*/  TEST LOGGING
        println("\"_21a_initialize()\" entered.");
        println("Received mainApp: " + (mainApp != null ? "OK" : "NULL!"));
        println("Received communicator: " + (communicator != null ? "OK" : "NULL!"));
        //  END TEST
        */

        //  Save the tools in our private variables.
        this.mainApp = mainApp;
        this.clientServerLine = communicator;

        println("All tools assigned successfully.");
        println("\"_21a_initialize()\" finished.");
    }


    /**
     *  THE "BANK TELLER" - CLIENT SIDE

     *  This fella works at the front desk (the UI). It is triggered when the user clicks the "Login" button.
     *  His only job is to:
     *     1. Take your ID (email) and Secret Word (password) from the form.
     *     2. Use the special phone (_03_ClientServerLine) to call the secure back office.
     *        He tells the operator, "Hey, this guy 'email' wants to log in with this word 'password'."
     *     3. Wait for the operator to say "Yep, he's good (SUCCESS)" or "Nope! (FAILURE)" or "He's locked out! (LOCKED)".
     *     4. Based on the answer, either call the Set Designer (_23_CtrlMainSceneBuilder) to show the main app, or show
     *        a pop-up error message.

     *  What it DOESN'T do: This teller never sees the vault, never checks the master list, never compares secret words. He just takes your info and makes the call.

     *  The '@FXML' tag in the .fxml file links the button's 'onAction' event directly to this method name.
     */
    @FXML
    private void _21b_handleLogin() {

        String email;
        String plainTextPassword;

       /* if (!validAlphaNumeric("Email", email, 100)) {
            _30i_showError("Email error:",   please + "email format.");
            return;  //  Stop right here.
        }

        //  1.2. PASSWORD
        if (!validAlphaNumeric("Password", plainTextPassword, 20)) {
            _30i_showError("Password error:", please + "password. It's required.");
            return;  //  Stop right here.
        }*/

        try {
            //  STEP 1: Get and validate the text fields: Get the text the user typed in the text fields in "login.fxml".
            //  "Fail-Fast" (FF) validation. Check for empty fields. Make sure the user actually typed something.
            //  "validAlphaNumeric" will THROW an exception if it fails, which will be caught by the 'catch' block below.

            // 1.1. EMAIL
            email = emailField.getText();
            _20a_validAlphaNumeric("Email", email, 2, 100); // This will throw on failure

            // 1.2. PASSWORD
            plainTextPassword = passwordField.getText();
            // Note: I'm changing '20' to '64' to match your regex in _20_ValidBusinessRules: PASSWD_REGEX
            _20a_validAlphaNumeric("Password", plainTextPassword, 4, 64); // This will throw on failure
            //  It never returns "false" it just throws an IllegalArgumentException and breaks or stops and waits for the user.

            // If we get here, validation passed!
            // All your OTHER code (Steps 2-4) now goes INSIDE this try block.


            //  STEP 2: "Delegate" the work.
            //     Send the login request to the server through the communicator.
            //     We send the plainTextPassword over the secure line. The 'communicator' class will handle the network stuff (suffix 0).
            //     We tell our "phone" to make the call send the credentials and wait for the server's official response.
            String serverResponse = clientServerLine._03d_attemptToLogin(email, plainTextPassword);

            /*/  DEBUG:
            println("CLIENT: Received raw response: [" + serverResponse + "]");
            if (serverResponse == null || serverResponse.isBlank()) {
                error("!!! CLIENT: Received NULL or BLANK response from server!");
                _30i_showError("Login Error", "Received empty response from server.");
                return; // Stop if response is bad
            }
            // END DEBUGGING
            */


            //  STEP 3: Process the server's response.
            //     The server sends back a simple protocol string like "STATUS|data1|data2".
            //     We need to parse it to understand what happened. We 'split()' it at the '|' to get the parts.
            //     The "\\" is needed because "|" is a special character in the split command.
            String[] parts = serverResponse.split("\\|");
            String status = parts[0].trim();  //  The first part is always the status.

            /*/ MORE DEBUGGING
            println("CLIENT: Parsed Status (trimmed): [" + status + "]");
            println("CLIENT: Number of parts: " + parts.length);
            // END DEBUGGING
            */


            //  STEP 4: Use a 'switch' to act according to the 'status' response.
            switch (status) {
                case "SUCCESS":
                    //  If login was good, the server sends "SUCCESS|UserName|UserRole"
                    if (parts.length == 3) {  //  Safety check
                        String userName = parts[1];
                        String userRole = parts[2];
                        println("Login in: " + userName + ", as: " + userRole);

                        //  STEP 5a: Get the "Stage" (the main window) from our button.
                        Stage primaryStage = (Stage) loginButton.getScene().getWindow();

                        //  STEP 5b: Call the "Master Assembler"!
                        //     We tell _23_CtrlMainSceneBuilder to build the main app screen, passing it all the tools 'it' will need.
                        //     We tell the main application to switch from the login screen to the main inventory view,
                        //        passing along the user's info.
                        _23_CtrlMainSceneBuilder._23a_show(mainApp, primaryStage, clientServerLine, userName, userRole);

                    } else {
                        //  This means the server sent a weird "SUCCESS" message.
                        _30i_showError("Protocol Error", "Received an invalid success response from the server.");
                    }
                    break;


                //  If the login fails or is locked, the server sends back "STATUS|Message for user" that says either:
                //     1. "FAILURE|Invalid password. You have 2 attempts left." or
                //     2. "LOCKED|Account locked. Please contact your admin."
                case "FAILURE":  //  status = parts[0]. On FAILURE, we go straight to LOCKED (get two birds with one stone)
                case "LOCKED":
                    if (parts.length >= 2) {  //  just in case the server sends more than 2 messages.
                        String message = parts[1];  //  "Invalid password. You have 2 attempts left."
                        String title = status.equals("LOCKED") ? "Account Locked" : "Login Failed";  // parts[0] goes in Win Title
                        //  Show the server's error message directly to the user.
                        _30h_showAlert(title, message);
                    } else {
                        _30i_showError("Protocol Error", "Received an invalid error response from the server.");
                        println("Received an invalid success response from the server.");
                    }
                    break;

                default:
                    //  This happens if the server sends something we don't understand, like "BANANAS".
                    _30i_showError("Error", "An unknown response was received: " + serverResponse);
                    println("An unknown response was received from the server: " + clientServerLine);
                    break;
            }
        } catch (IllegalArgumentException e) {
            // This CATCH block will now grab the *specific* error message from _00_Utils
            //    (e.g., "Password error: Please enter 8–64 chars...") and display it in a friendly popup!
            _30i_showError("Input Error", e.getMessage());
        }
    }


    /**
     *  This method is triggered when the user clicks the "Sign Up" button.
     *  It's "decoupled" from the login logic, because "login.fxml" can only access one "action" (this one: _21_CtrlLogin).
     *  Its only job is to launch the other "front desk clerk" (_22_CtrlSignup).
     */
    @FXML
    private void _21c_handleSignUp() {
        //  STEP 1: Create a 'new' instance of the dedicated sign-up controller, from the class _22_CtrlSignup
        _22_CtrlSignup signupController = new _22_CtrlSignup();


        //  STEP 2: "Inject" the "phone line" (the necessary instance dependency into the signup controller).
        //     Give it the tool it needs: the network communicator. The sign-up controller also needs to talk to the server.
        signupController._22a_setCommunicator(this.clientServerLine);
        //  This field was a public static, and could not access instance fields of the class.
        //  The logic for launching the dialog (controlSignup) should not be static, as it needs to run
        //     validation checks (dbMgrForUser) and potentially update fields on a specific instance


        //  STEP 3: Launch the pop-up dialog and get the result.
        //     Tell the sign-up controller to show its dialog and do its job. The showSignUpDialog() method will handle everything else.
        //     It returns an Optional, which will contain the new user's email if successful.
        //     Optional<ModelUser> createdUser = signupController_2.controlSignup();  //  Non-static.
        //     This method will pause here until the user finishes.
        Optional<String> newUserEmail = signupController._22b_showSignUpDialog();


        //  STEP 4: This is a nice "user experience" touch.
        //     If 'newUserEmail' is 'present' (meaning sign-up was a success), we pre-fill the email field for them,
        //        so they can log in right away.
        newUserEmail.ifPresent(email -> {
            emailField.setText(email);
            passwordField.clear();       //  Clear the password for security
            passwordField.requestFocus(); //  Put the cursor in the password box
        });
        //  Let user know all is good.
        //_30h_showAlert("Sign Up", "You have been successfully signed up.");
        //  commented bc it's redundant.
    }
}