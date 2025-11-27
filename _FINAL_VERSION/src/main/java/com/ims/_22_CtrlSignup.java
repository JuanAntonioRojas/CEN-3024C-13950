package com.ims;

import javafx.collections.FXCollections;

import java.util.Locale;
import java.util.Optional;

//  Import our "toolbox" and "Lego brick" builders

import static com.ims._00_Utils.*;
import static com.ims._20_ValidBusinessRules.*;
import static com.ims._30_GUI_Builder.*;

/**
 *  This is the other "Front Desk Clerk," in charge of Sign-Ups.
 *  This class has ONE job: to handle the creation of a new user.
 *  It creates its 'own' pop-up dialog window (using the _31_ViewAdd factory), collects the new user's information,
 *     hashes the password, then it sends that information to the server for creation, and finally reports the result.

 *  The main method orchestrates the entire process: it handles the UI interaction (_22b_showSignUpDialog),
 *     defines how to build the data (the grid -> lambda), and defines what to do with the data (the user -> consumer).
 *  This is a maintainable structure due to the "Separation of Concerns" and it's far better than repeating code and
 *     mixing the UI and data persistence logic, as it makes testing and future modifications a lot easier.

 *  The most important security feature (hashing the password) is performed after the user submits the form but before
 *     the data leaves the application's memory.

 *  By using a Functional Interface (the Supplier) to delay the object creation (new ModelUser), we ensure that the
 *     sensitive plain-text password is kept in a local variable (plainPassword) only for the brief moment (required
 *     for hashing), minimizing its exposure.
 */

public class _22_CtrlSignup {

    //  This controller's only "tool" is the "phone line" (the 'communicator' to talk to the server).
    //  It doesn't need @FXML tags because it builds its own UI (a dialog).
    private _03_ClientServerLine communicator;


    /**
     *  This is the "tool-giving" (setter) method.
     *  The _21_CtrlLogin class calls this to "inject" the one and only "phone line."
     *     @param communicator The shared communicator.
     */
    public void _22a_setCommunicator(_03_ClientServerLine communicator) {
        this.communicator = communicator;
    }



    /**
     *  SHOW SIGN-UP DIALOG BOX

     *  This is the main public method that _21_CtrlLogin calls. It builds and shows the sign-up pop-up dialog.
     *  This lets _21_CtrlLogin pre-fill the email field.
     *     @return An 'Optional<String>' which will contain the new user's email 'if' sign-up was successful, otherwise empty.
     */
    public Optional<String> _22b_showSignUpDialog() {
        //  TODO: (SECURITY/DESIGN) Use a logger instead to manage log levels (DEBUG, INFO, ERROR)
        println("Sign Up button clicked!");  //  attempt at a event logger


        //  STEP 1: Create the pop-up window.
        //     We display a generic dialog box, to gather the new user's info.
        //     The dialog should return a "user" object (ModelUser) or an Optional.empty() if the user clicks cancel.
        //     This makes the sign-up controller much cleaner and more concise. All the network logic happens "after" the dialog closes.
        //     So, we call our "Pop-Up Window Factory" (_31_ViewAdd).
        //     Its 'showAddDialog' method is a powerful, generic helper. We just have to give it "instructions" or arguments.
        Optional<_13_ModelUser> createdUser = _31_ViewAdd._31a_showAddDialog(
        //    _31a_showAddDialog is defined as:
        //    "public static <T> Optional<T> showAddDialog_2_1a(String title, Function<GridPane, Supplier<T>> formFactory, Consumer<T> onSave)"

                //  ARGUMENT 1: The window title.
                "Sign Up for a New Account",

                //  ARGUMENT 2: The GUI "Form Factory" (a Lambda function).

                //  LAMBDA 1: BUILT THE GUI: This 2nd parameter takes a GridPane, and it tells the factory what
                //     fields/labels (of type <T>) put on that grid.
                grid -> {
                    int row = 0; //  Our row counter. The grid Row number

                    //  The following 'TextField', 'PasswordField', etc., are imported from my _30_GUI_Builder helper class.

                    //  NOTE:
                    //var id  = addTextField(grid, "ID",       ++row);  // REMOVED bc "id" is AutoIncrement

                    var nameField = _30b_TextField(grid, "Name", row++);
                    var passField = _30c_PasswordField(grid, "Password", row++);
                    var emailField = _30b_TextField(grid, "Email", row++);
                    var phoneField = _30b_TextField(grid, "Phone", row++);

                    //var role  = addTextField(grid, "Role", row++);  //  switched to combo box, bc db has enum.
                    //  Use a 'ComboBox' (drop-down) for the role.
                    var roleCombo = _30e_ComboBox(
                            grid, "Role",
                            FXCollections.observableArrayList("admin", "staff"),
                            row++
                    );
                    roleCombo.getSelectionModel().select("staff"); // Default to 'staff'

                    //  NOTE: When we first build the form, the text fields are empty.
                    //  If we created the ModelUser object right away, all its fields would be blank!


                    //  LAMBDA 2: THE NEW OBJECT  "SUPPLIER"

                    //  NOTE: In a client-server application, the responsibility for validation is split.
                    //  This part builds the entity and returns a Supplier<T> (a Functional Interface) that knows how to read
                    //    those fields and build the generic T object (in this case: ModelUser) from the those field values.
                    //  The Supplier is a "delayed action." It's a little package of code that says, "I know how to read the text fields."
                    //  It only gets executed at the exact moment the user clicks the "OK" ("Save") button.
                    //  At that point, it runs, grabs the current text from all the fields, and builds the final ModelUser object
                    //    with the correct, validated and up-to-the-second data.
                    //  This is a way to capture the form's state at the very last moment.
                    //  The supplier is kinda like a "recipe" that tells the factory how to 'build the object' when the user clicks "Save."

                    return () -> {

                        //  STEP 2a: Get all the data from the fields, and do a FAIL-FAST VALIDATION CHECK
                        //     This is a design philosophy where a program is written to check for invalid conditions or errors
                        //        as early as possible and throw exceptions or halt execution if an issue is found.


                        //  Generic form:
                        //  _20a_validAlphaNumeric(fieldType, fieldText, maxLength, description)

                        //  NAME
                        String name = nameField.getText();
                        _20a_validAlphaNumeric("Name", name, 2, 50);

                        //  PASSWORD
                        String plainPassword = passField.getText();
                        _20a_validAlphaNumeric("Password", plainPassword, 4, 64);

                        //  STEP 2b: THE CRITICAL SECURITY STEP!
                        //     We HASH the password on the CLIENT-SIDE before it ever leaves the user's computer.
                        //     We HASH it using the BCrypt algo (1-way transformation) with a 22 char Salt and a high Cost (12)
                        //        before passing it to ModelUser constructor.
                        String hashedPassword = _00_Utils.hash(plainPassword);  //  from _00_Utils.hash()


                        //  EMAIL
                        String email = emailField.getText();
                        _20a_validAlphaNumeric("Email", email, 4, 100);


                        //  PHONE
                        String phone = phoneField.getText();
                        _20a_validAlphaNumeric("Phone", phone, 10, 15);


                        //  Combo Box doesn't need validation. Its values are preset.
                        //String role = roleCombo.getValue(); //  This had an error: "Data truncated for column 'role'"
                        String roleRaw = String.valueOf(roleCombo.getValue());
                        String role = roleRaw == null ? "" : roleRaw.trim().toLowerCase(Locale.ROOT);
                        println("DEBUG: Role value to insert = [" + role + "]");

                        if (!role.equals("admin") && !role.equals("staff")) {
                            throw new IllegalArgumentException("Role must be admin or staff (got: [" + roleRaw + "])");
                        }

                        // If we get here, all validations passed!


                        //  STEP 2c:  CREATE A NEW USER (The Object Supplier)
                        //     Return the finished "data bucket" object.
                        //     We send 'null' for the ID (the DB will make one) and '0' for login attempts.
                        //  NOTE: The Java Runtime Environment (JRE) allocates a block of memory on the dynamic Heap.
                        //     It calls the constructor of the ModelUser class, initializing all the fields (id, role, name, etc.).
                        //     The newly created ModelUser object exists only in the application's memory (RAM), not yet in the DB.

                        return new _13_ModelUser(
                                null,        //  "id" is AutoIncrement
                                role,           //  <-- string: exactly as DB "enum" says: "admin" or "staff"
                                name,
                                hashedPassword, //  (3) We pass a secured HASH to the DB. The DB will never know the pwd.
                                email,          //  Validated (unique) email
                                phone,
                                0 //  We pass zero (new user) for the loginAttempts property in the ModelUser.
                        );
                    };  //  END OF RETURN () ->
                },  //  END OF GRID


                //  ARGUMENT 3: The "On Save" action.
                //     We set this to 'null' because our "saving" happens over the network 'after' the dialog closes, not 'inside' it.
                //     The onSave lambda remains null as the client doesn't save to the DB. This is just the Supplier.
                null
        );



        //  THE CONSUMER

        //  The previous Supplier lambda (return () -> new ModelUser(...)) is the factory that produces the ModelUser object when "OK" is clicked.
        //  The following is the code that consumes that ModelUser object and sends it to the DB: _03e_attemptSignUp(newUser),
        //     with the help of the "communicator" phone line (_03_ClientServerLine).

        //  STEP 3: This code runs 'after' the user closes the pop-up.
        //     If the dialog returned a user, send it to the server.

        //     First, we check if the 'createdUser' box is full.
        if (createdUser.isPresent()) {
            //  STEP 3a: Get the user object from the "box."
            _13_ModelUser newUser = createdUser.get();

            //  STEP 3b: Send the new user (with the BCrypt 'hashed' password) to the server using our "phone line."
            String serverResponse = communicator._03e_attemptToSignUp(newUser);

            //  STEP 3c: Check the server's final word.
            if (serverResponse.startsWith("SUCCESS")) {
                //  Yee-pee!!!
                _30h_showAlert("Successful Sign-Up", "Account created for " + newUser.getName() + ".\nPls click \"Refresh Data\".");
                //  Return the email so the login screen can use it.
                return Optional.of(newUser.getEmail());
            } else {
                //  Show the server's error (e.g., "Email already exists").
                _30i_showError("Sign-Up Failed", serverResponse);
            }
        }

        //  STEP 4: If we get here, the user clicked "Cancel" or the sign-up failed. Return an empty box.
        return Optional.empty();
    }
    //  END OF SHOW SIGN-UP DIALOG BOX
}