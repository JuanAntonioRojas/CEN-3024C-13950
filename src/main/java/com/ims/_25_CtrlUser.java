package com.ims;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//  Import our "toolbox" and "Lego brick" builders
import static com.ims._00_Utils.*;
import static com.ims._20_ValidBusinessRules.*;
import static com.ims._30_GUI_Builder.*;

/**
 *   This is the "User Department Manager" (a Controller).
 *   This is the "brain" for all user management actions.
 *   It does not talk to the database. It only talks to the _03_ClientServerLine's communicator.
 *   This class is almost identical to _24_CtrlProd, but it's in charge of the 'User' table instead.

 *   Each public method is one action that a user can trigger.
 *   Its job is specifically to manage existing users (Read, Update, Delete).
 *   The creation part cleverly reuses the dedicated sign-up controller.
 *   So basically, it's responsible for all the application's logic: connecting to the database, adding/removing users, and refreshing the table.

 *   I tried to keep the UI prompts simple and do some validation, so the program won’t crash.
 *   TODO: customize the inputs and beautify them with CSS.
 */

public class _25_CtrlUser {

    //  INSTANCE VARIABLES (THE "TOOLS")

    //  The "phone line."
    private final _03_ClientServerLine communicator;

    //  The actual 'User' TableView from the UI.
    private final TableView<_13_ModelUser> userTable;

    /**
     *   CONSTRUCTOR

     *   With this constructor we establish the dependency injection, so that the class can do its job without
     *     creating its own dependencies. Without this, the Actions class wouldn't know about the database or the table.
     *   If it created its own DatabaseManager and TableView dependencies it would lead to separate, unmanaged instances,
     *      or be unable to perform any actions at all.
     *   We pass a single instance of the communicator, from _03_ClientServerLine,to indirectly talk to the DB.
     *   We also give it access to the specific "TableView" object (a Generic User) that is displayed on the screen.

     *   This class would be "hired" by an Admin-only part of the app (e.g., when an admin clicks a "Manage Users" button).
     *      @param communicator The one-and-only "phone line."
     *      @param userTable The actual 'User' TableView to update.
     */
    public _25_CtrlUser(_03_ClientServerLine communicator, TableView<_13_ModelUser> userTable) {
        this.communicator = communicator;   // phone
        this.userTable = userTable;         // data

        //  We set up the "userTable" to allow the admin to select multiple users, in case we need to delete them at once.
        userTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }






    // =================================================================================================================

    //  CRUD OPERATIONS

    // =================================================================================================================




    /**
     *  1. "C" RUD: CREATE:  ADD NEW USER

     *   SIGN-UP form manages this:  The "_22b_showSignUpDialog" is called when the "Add" button is clicked.
     */



    /**
     *   2. C "R" UD: READ

     *   REFRESH TABLE

     *   Essential utility after every action/change. This is the most used method in this class.
     *   This method is called when the "Refresh" button is clicked, or a CRUD operation happens.
     *   It gets all the user data from the server, through the communicator, and display it in the table.

     *   The "data" list of all the users requested to and given by the server goes into an "ObservableList".
     *   It's a key part of the Model-View-Controller (MVC) design pattern used by JavaFX.
     *   It also sends automatic notifications when it "observes" that items are modified (added, removed, or updated).
     *   When a change occurs in the ObservableList, the UI component (e.g. TableView, ListView, or ComboBox)
     *       automatically updates itself without requiring to manually refresh the view.
     *   This is crucial for keeping the UI synchronized with the data and make the app so responsive and easy to work with.
     */

    public void _25a_handleRefreshTable() {
        println("Attempting to refresh user table..."); //  Log the action

        //  STEP 1: Make the "phone call" to get all users, on the Client-Server phone line (_03_).
        String serverResponse = communicator._03l_getAllUsers();  //  We're using a "_03b_sendRequest" from the Client-Server-Line

        //  STEP 2: Parse the SERVER RESPONSE
        //     Parse the initial status part of the server's response (e.g., "SUCCESS|data..." or "FAILURE|message...").
        String[] parts = serverResponse.split("\\|", 2);
        String status = parts[0];

        if ("SUCCESS".equals(status)) {  //  this is null-safe. It returns false if status is null.
            //  The other way around: if (status.equals("SUCCESS")), throws a "NullPointerException" if status == null.

            //  STEP 3: Now we get the 'data' part/payload (maybe empty if no suppliers exist).
            String data = (parts.length > 1) ? parts[1] : "";

            //  STEP 4: Give the raw data to our 'parser' helper method to parse the data string into a list of User objects.
            ObservableList<_13_ModelUser> users = _25b_parseUserData(data);  //  <-- KEY

            //  STEP 5: Put the list of users into the table. Update the TableView. JavaFX automatically redraws the table when its items list is set.
            userTable.setItems(users);
            println(users.size() + " users loaded into the table.");

        } else { //  FAILURE
            //  STEP 6: Show the error message from the server response.
            String errorMessage = (parts.length > 1) ? parts[1] : "Unknown server error.";
            //  Display the error message in a pop-up dialog.
            _30i_showError("Failed to Load Users", errorMessage);
        }
    }





    /**
     *   This is an essential "helper" tool.
     *   Its only job is to take the raw data string from the server and "parse" it into a temporary List of User objects.
     *   Handles the specific protocol format used (user1;user2;...) where each user is "id|role|...").
     *      @param data The raw string from the server (e.g., "user1;user2")
     *      @return An 'ObservableList' containing _13_ModelUser objects, suitable for the TableView that can be displayed.
     *              Returns an empty list if data is null, empty, or malformed.
     */
    private ObservableList<_13_ModelUser> _25b_parseUserData(String data) {

        List<_13_ModelUser> userList = new ArrayList<>();

        //  Handle null or empty data string gracefully.
        if (data == null || data.isEmpty()) {
            return FXCollections.observableArrayList(userList); //  Return empty observable list
        }

        //  STEP 1: Split the data into individual users.
        String[] userStrings = data.split(";");

        //  STEP 2: Loop through each user 'string'.
        for (String uString : userStrings) {
            //  STEP 2a: Split the user 'string' into its 7 fields based on the pipe delimiter.
            String[] fields = uString.split("\\|", 7); //  Use "\\|" to escape the pipe character

            //  EXTRACT

            if (fields.length == 7) {
                try {
                    //  STEP 2b: Parse the fields...
                    String id      = fields[0];
                    String role    = fields[1];
                    String name    = fields[2];
                    String pwdHash = fields[3];
                    String email   = fields[4];
                    String phone   = fields[5];
                    int loginAttempts = Integer.parseInt(fields[6]);

                    //  STEP 2c: ...and add the new object to our temporary list.
                    userList.add(new _13_ModelUser(id, role, name, pwdHash, email, phone, loginAttempts));

                } catch (NumberFormatException e) {
                    error("Skipping malformed user data from server: " + uString);
                }
            } else {
                error(" (Expected 6 fields, found " + fields.length + ")");
            }
        } //  End of loop through user strings

        //  STEP 3: Turn our normal list into a JavaFX 'ObservableList'.
        //     Convert the standard ArrayList into a JavaFX ObservableList before returning.
        //     This allows the TableView to automatically observe changes to the list.
        return FXCollections.observableArrayList(userList);
    }








    /**
     *  3. CR "U" D: UPDATE

     *   This method is called when the user selects a single product in the table and clicks the "View / Update" button.
     *   It orchestrates the entire process of editing an existing record in a secure, client-server architecture.

     *   THE FLOW:
     *   1. GET SELECTION: It first ensures that one, and only one, product has been selected from the table.

     *   2. BUILD DIALOG: It then uses our powerful and reusable "_34_CtrlAdd" dialog factory to create a pop-up window.

     *   3. POPULATE FORM: Within the dialog's form factory, it creates all the necessary text fields for a product.
     *      This is the critical step where it PRE-FILLS each field with the data from the 'selectedProduct' object.

     *   4. USER EDITS: The user can now edit the information in the pop-up.
     *      The SKU field is locked, as it's the primary key and should not be changed.

     *   5. CREATE NEW OBJECT: When the user clicks "Save", the "Supplier" lambda runs.
     *      It reads all the current (potentially modified) values from the fields and constructs a brand new '_10_ModelProd'
     *         object that represents the updated state.

     *   6. SEND TO SERVER: This new, updated product object is then handed off to the network communicator, which sends
     *         it to the server with an "UPDATE_PRODUCT" command.

     *   7. REFRESH UI: After the server confirms the update was successful, this method calls 'handleRefreshTable' to
     *         fetch the latest data and show the changes in the UI.
     */
    public void _25c_handleUpdateUser() {
        //  STEP 1: Get the SINGLE selected user from: "private final TableView<_13_ModelUser> userTable;"
        //     From the entire table provided by userTable, we only select the ONE ROW that is highlighted
        _13_ModelUser selectedUser = userTable.getSelectionModel().getSelectedItem();  //  singular

        //  STEP 2: "Fail-Fast" check. Ensure exactly one user is selected. Fail if no row is highlighted.
        if (selectedUser == null) {
            _30i_showError("No Selection", "Please select a single user to update.");
            return; //  Stop execution if nothing is selected
        }

        //  STEP 3: Call the generic "Pop-Up Window Factory" with that user's info to show the update form.
        Optional<_13_ModelUser> updatedUserOptional = _31_ViewAdd._31a_showAddDialog(

                //  Param 1: Popup Dialog Box Title
                "Update User",

                //  Param 2: The "Form Factory" lambda. It tells us what fields to add.
                grid -> {
                    int row = 0;
                    //  Use GUI Builder to create labeled fields matching "_13_ModelUser" properties.
                    var idField    = _30b_TextField(grid, "ID", row++);
                    var roleCombo  = _30e_ComboBox(grid, "Role", FXCollections.observableArrayList("admin", "staff"), row++);
                    var nameField  = _30b_TextField(grid, "Name", row++);
                    var emailField = _30b_TextField(grid, "Email", row++);
                    var phoneField = _30b_TextField(grid, "Phone", row++);


                    //  OLD line (the simple password field):
                    // var passField = _30c_PasswordField(grid, "New Password", row++);

                    //  NEW line (using a new toggle component): _30c1_TogglePasswordField
                    var passField = _30c1_TogglePasswordField(grid, "New Password", row++);



                    //  STEP 3a: PRE-FILL to populate the fields, for that one "selected" user from the userTable
                    idField.setText(    selectedUser.getId());
                    idField.setEditable(false); //  Lock the ID!
                    roleCombo.setValue( selectedUser.getRole());
                    nameField.setText(  selectedUser.getName());
                    emailField.setText( selectedUser.getEmail());
                    phoneField.setText( selectedUser.getPhone());
                    passField.setPromptText("Leave blank to keep old password");

                    //  EXTRACT, VALIDATE & CREATE

                    //  Return the Supplier.
                    return () -> {

                        //  STEP 3b: EXTRACT:
                        String id    = idField.getText();
                        String role  = roleCombo.getValue();
                        String name  = nameField.getText();
                        String email = emailField.getText();
                        String phone = phoneField.getText();

                        //  Handle the PASSWORD change.
                        String pass  = passField.getText();
                        String passlHash;

                        if (pass.isBlank()) {  //  If the field is blank, the admin doesn't want to change the password. So, we use the old hash.
                            passlHash = selectedUser.getPwdHash();
                        } else {  //  If the field is not blank, we must hash the new password.
                            passlHash = hash(pass);
                        }

                        //  We need to keep track of the number of logged in attempts:
                        int attempts = selectedUser.getLoginAttempts(); //  Keep the old attempts

                        //  STEP 3c: VALIDATE:
                        //  _20a_validAlphaNumeric(fieldType, fieldText, maxLength, description)

                        //  NAME
                        boolean boolName = _20a_validAlphaNumeric("Name", name, 2, 50);

                        //  EMAIL
                        boolean boolEmail = _20a_validAlphaNumeric("Email", email, 2, 50);

                        //  PHONE
                        boolean boolPhone = _20a_validAlphaNumeric("Phone", phone, 2, 50);

                        //  PASSWORD
                        boolean boolPass = _20a_validAlphaNumeric("Password", pass, 2, 50);


                        //  CONFIRM VALIDATION:
                        if( boolName || boolEmail || boolPhone || boolPass ) {

                            //  STEP 3d: CREATE: a new updated "ModelUser" object, if all checks pass.
                            //     Return the "User<T>" for creating the updated object when "Save" is clicked.
                            return new _13_ModelUser(id, role, name, email, phone, passlHash, attempts);
                        }
                        return null;
                    };
                },
                null //  'onSave' is null.
        );


        //  STEP 4: This code runs after the pop-up closes, and it comes from the definition of optional on top:
        //     "Optional<_13_ModelUser> updatedUserOptional = _31_ViewAdd._31a_showAddDialog()"
        //     '.ifPresent()' runs only if the user clicked "Save".
        updatedUserOptional.ifPresent(updatedUser -> {

            //  STEP 4a: Send the updated user object to the server.
            String serverResponse = communicator._03n_updateUser(updatedUser);

            //  STEP 4b: Process the server's response.
            if (serverResponse.startsWith("SUCCESS")) {

                //  Show success message and refresh the table to display the new supplier.
                _30h_showAlert("Success", "User updated successfully.");
                _25a_handleRefreshTable(); //  Refresh the user's table to see the changes!
            } else {
                _30i_showError("Failed to Update User", serverResponse);
            }
        });
    }










    /**
     *   CRU "D": DELETE

     *   REMOVE USER

     *   This method is called when the "REMOVE SELECTED" button is clicked.
     *   It gets all selected products from the table, asks for confirmation, and then sends a single request to the
     *     server to delete all of them.
     *   Because the user can select multiple products, this method uses an ObservableList<_23_CtrlProd>
     *     to get a list of selected products.
     *   It then iterates through that list to remove each item.
     *   This is the logic for deleting one or more products from the table and the database.
     *   It works by first getting all the products the user has selected.
     *   Then it asks for confirmation before looping through the list to delete each selected product individually
     *      from the database.
     *   Finally, it refreshes the table to show the changes.
     */

    public void _25d_handleRemoveUser() {
        //  STEP 1: Get the List of all selected users.
        //     The "userTable" is allowed to select "MULTIPLE" users, in case we need to delete many at once.
        ObservableList<_13_ModelUser> selectedUsers = userTable.getSelectionModel().getSelectedItems(); // plural

        //  STEP 2: "Fail-Fast" check.
        if (selectedUsers == null || selectedUsers.isEmpty()) {  // isEmpty applies to a list
            _30i_showError("No Selection", "Please select one or more users to remove.");
            return;
        }

        //  STEP 3: Show the "Are you sure?" pop-up.
        Alert confirmDelete = new Alert(
                Alert.AlertType.CONFIRMATION,  //  a javaFX "alert" type
                "Are you sure you want to remove " + selectedUsers.size() + " selected user(s)?",
                ButtonType.YES, ButtonType.NO
        );

        //  STEP 4: Display the confirmation dialog and wait for the user to click "Yes" or "No" response.
        //     '.ifPresent()' executes the lambda only if the user clicks a button (doesn't close the dialog window).
        confirmDelete.showAndWait().ifPresent(response -> {

            if (response == ButtonType.YES) {
                //  STEP 5a: Get a second List of just the 'IDs' from the selected users ObservableList.
                List<String> idsToRemove = selectedUsers.stream()
                        .map(_13_ModelUser::getId)
                        .collect(Collectors.toList());

                //  STEP 5b: Send that one list of IDs to the server to be batch-deleted.
                //     from "_03_ClientServerLine": String request = "REMOVE_USER::" + ids;  //  from "_02_ServerClientHandler" -> case "REMOVE_USER".
                String serverResponse = communicator._03m_removeUsers(idsToRemove);

                //  STEP 5c: Process the server's response.
                if (serverResponse.startsWith("SUCCESS")) {
                    _30h_showAlert("Success", idsToRemove.size() + " user(s) removed.");
                    _25a_handleRefreshTable(); //  Refresh the table!
                } else {
                    _30i_showError("Failed to Remove Users", serverResponse);
                }
            }  //  If response is NO or the dialog is closed, do nothing.
        });
    }
}