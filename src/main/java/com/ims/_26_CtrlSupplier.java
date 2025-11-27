package com.ims;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//  Import our "toolbox" and "Lego brick" builders
import static com.ims._00_Utils.*;
import static com.ims._20_ValidBusinessRules.*;
import static com.ims._30_GUI_Builder.*;
import static com.ims._31_ViewAdd.*;

/**
 *   This is the "Supplier Department Manager" (a Controller).
 *   This is the "brain" for all supplier management actions within the client application.
 *   It does NOT talk to the database directly. It only talks to the _03_ClientServerLine's communicator.
 *   This class structure parallels _25_CtrlUser and _24_CtrlProd, managing the 'Supplier' table.

 *   Each public method typically corresponds to one action that an admin user can trigger via a button.
 *   The creation ("Add") action utilizes the generic "_31_ViewAdd" dialog factory.

 *   It is responsible for translating UI events into network requests and updating the UI based on server responses.
 *   TODO: Further customize input validation or UI appearance.
 */
public class _26_CtrlSupplier {

    //  INSTANCE VARIABLES (THE "TOOLS")

    /**   The single network communicator instance for talking to the server. Injected via constructor. */
    private final _03_ClientServerLine communicator;
    /**   The actual TableView instance from the UI that displays suppliers. Injected via constructor. */
    private final TableView<_15_ModelSupplier> supplierTable;




    /**
     *   CONSTRUCTOR

     *   Establishes Dependency Injection. This controller requires the communicator and the table it manages to function.
     *   Passing these dependencies ensures this controller uses the single, shared communicator instance and updates the correct UI table.
     *   This approach avoids issues with unmanaged instances and makes the controller testable.

     *   This controller instance would typically be created by the _23_CtrlMainSceneBuilder when constructing the Supplier management scene.
     *      @param communicator The one-and-only shared "phone line" to the server.
     *      @param supplierTable The actual Supplier TableView instance displayed on the screen.
     */
    public _26_CtrlSupplier(_03_ClientServerLine communicator, TableView<_15_ModelSupplier> supplierTable) {
        this.communicator = communicator;
        this.supplierTable = supplierTable;

        //  Configure the table to allow selecting multiple suppliers (e.g., for batch removal).
        this.supplierTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }





    //==================================================================================================================

    //  CRUD OPERATIONS

    //===================================================================================================================





    /**
     *   "C" RUD: CREATE: "ADD" A SUPPLIER

     *   Called when the "Add" button is clicked in the Supplier view.
     *   It uses the generic "_31_ViewAdd._31a_showAddDialog" factory to display a pop-up form for entering new supplier details.
     *   Handles the entire process: showing the dialog, collecting input, basic validation, sending data to the server, and refreshing the table upon success.
     */
    public void _26a_handleAddSupplier() {

        //  STEP 1: Call the generic "Pop-Up Window Factory".
        Optional<_15_ModelSupplier> newSupplierOptional = _31a_showAddDialog(

                //  Parameter 1: Popup Dialog Box Title
                "Add New Supplier",

                //  Parameter 2: The "Form Factory" lambda. Defines the fields.
                grid -> {
                    int row = 0;
                    //  Use GUI Builder to create labeled fields matching "_15_ModelSupplier" properties.
                    var companyField = _30b_TextField(grid, "Company", row++);
                    var contactField = _30b_TextField(grid, "Contact", row++);
                    var phoneField   = _30b_TextField(grid, "Phone", row++);
                    var emailField   = _30b_TextField(grid, "Email", row++);
                    var addressField = _30b_TextField(grid, "Address", row++);
                    var notesField   = _30b_TextField(grid, "Notes", row++);

                    //  EXTRACT, VALIDATE & CREATE

                    return () -> {

                        //  STEP 2: EXTRACT:
                        //String supID   = companyField.getText();
                        String company = companyField.getText();
                        String contact = contactField.getText();
                        String phone   = phoneField.getText();
                        String email   = emailField.getText();
                        String address = addressField.getText();
                        String notes   = notesField.getText();


                        //  STEP 3: VALIDATE teh extracted texts:
                        //  _20a_validAlphaNumeric(fieldType, fieldText, maxLength, description)

                        //  COMPANY
                        boolean boolCompany = _20a_validAlphaNumeric("Name", company, 2, 50);

                        //  CONTACT
                        boolean boolContact = _20a_validAlphaNumeric("Name", contact, 2, 50);

                        //  PHONE
                        boolean boolPhone = _20a_validAlphaNumeric("Phone", phone, 10, 15);

                        //  EMAIL
                        boolean boolEmail = _20a_validAlphaNumeric("Email", email, 2, 50);

                        //  ADDRESS
                        boolean boolAddress = _20a_validAlphaNumeric("Address", address, 2, 50);

                        //  NOTES
                        boolean boolNotes = _20a_validAlphaNumeric("Description", notes, 0, 10000);


                        //  CONFIRM VALIDATION:
                        if( boolCompany || boolContact || boolPhone || boolEmail || boolAddress || boolNotes ) {

                            //  STEP 4: CREATE: a new "ModelSupplier" object, if all checks pass.
                            //     Return the "Supplier<T>" for creating the updated object when "Save" is clicked.
                            return new _15_ModelSupplier(null, company, contact, phone, email, address, notes); //  supID is generated by DB
                        }

                        //  If the validation failed, it returns 'null', then  "Optional<T>_31a_showAddDialog()" will see 'null' and return Optional.empty()
                        return null;
                    }; //  End of Supplier lambda
                },

                //  Parameter 3: 'onSave' is null. The network call happens after the dialog closes successfully.
                null
        ); //  End of _31a_showAddDialog call


        //  STEP 5: This code runs after the pop-up closes, and it comes from the definition of optional on top:
        //     "Optional<_13_ModelUser> updatedUserOptional = _31_ViewAdd._31a_showAddDialog()"
        //     '.ifPresent()' runs only if the user clicked "Save" (the Optional contains a supplier).
        newSupplierOptional.ifPresent(supplier -> {

            //  STEP 5a: Send the newly created supplier object to the server via the communicator.
            String serverResponse = communicator._03p_addSupplier(supplier);

            //  STEP 5b: Process the server's response.
            if (serverResponse.startsWith("SUCCESS")) {

                //  Show success message and refresh the table to display the new supplier.
                _30h_showAlert("Success", "Supplier added successfully.");
                _26b_handleRefreshTable(); //  Refresh the supplier's table to see the changes!
            } else {
                //  Show the error message received from the server.
                _30i_showError("Failed to Add Supplier", serverResponse);
            }
        });
    }




    /**
     *   C "R" UD: READ - REFRESH TABLE

     *   Essential method called after CRUD operations or when the "Refresh" button is clicked.
     *   Fetches the complete, up-to-date list of suppliers from the server and updates the TableView.
     *   Handles parsing the server's response string into Supplier objects.
     */
    public void _26b_handleRefreshTable() {
        println("Attempting to refresh supplier table..."); //  Log the action

        //  STEP 1: Request all suppliers from the server using the communicator (_03_).
        String serverResponse = communicator._03o_getAllSuppliers();  //  We're using a "_03b_sendRequest" from the Client-Server-Line

        //  STEP 2: Parse the SERVER RESPONSE
        //     Parse the initial status part of the server's response (e.g., "SUCCESS|data..." or "FAILURE|message...").
        String[] parts = serverResponse.split("\\|", 2);
        String status = parts[0];

        if ("SUCCESS".equals(status)) {  //  this is null-safe. It returns false if status is null.
            //  The other way around: if (status.equals("SUCCESS")), throws a "NullPointerException" if status == null.

            //  STEP 3 (Success): Extract the data part/payload (maybe empty if no suppliers exist).
            String data = (parts.length > 1) ? parts[1] : "";

            //  STEP 4: Use the private helper method to parse the data string into a list of Supplier objects.
            ObservableList<_15_ModelSupplier> suppliers = _26c_parseSupplierData(data);

            //  STEP 5: Update the TableView. JavaFX automatically redraws the table when its items list is set.
            supplierTable.setItems(suppliers);
            println(suppliers.size() + " suppliers loaded into the table."); //  Log success

        } else { //  FAILURE
            //  STEP 6: Extract the error message from the server response.
            String errorMessage = (parts.length > 1) ? parts[1] : "Unknown server error.";
            //  Display the error message in a pop-up dialog.
            _30i_showError("Failed to Load Suppliers", errorMessage);
        }
    }







    /**
     *   Private Helper: Parses the raw data string received from the server into a temporary list of Supplier objects.
     *   Handles the specific protocol format used (supplier1;supplier2;...) where each supplier is "id|company|...").

     *      @param data The raw string data payload from the server's SUCCESS response (e.g., "1|CompA|...;2|CompB|...").
     *      @return An 'ObservableList' containing _15_ModelSupplier objects, suitable for the TableView.
     *              Returns an empty list if data is null, empty, or malformed.
     */
    private ObservableList<_15_ModelSupplier> _26c_parseSupplierData(String data) {

        List<_15_ModelSupplier> supplierList = new ArrayList<>(); //  Start with a standard ArrayList

        //  Handle null or empty data string gracefully.
        if (data == null || data.isEmpty()) {
            return FXCollections.observableArrayList(supplierList); //  Return empty observable list
        }

        //  STEP 1: Split the main data string into individual supplier strings based on the semicolon delimiter.
        String[] supplierStrings = data.split(";");

        //  STEP 2: Iterate through each individual supplier string.
        for (String sString : supplierStrings) {
            //  STEP 2a: Split the supplier string into its component fields based on the pipe delimiter.
            //  Expecting 6 fields according to the _15_ModelSupplier structure and server serialization.
            String[] fields = sString.split("\\|", 7); //  Use "\\|" to escape the pipe character

            //  EXTRACT

            //  STEP 2b: Validate that the correct number of fields was found.
            if (fields.length == 7) {

                try {
                    //  STEP 2b: Parse the fields...
                    String id      = fields[0];
                    String company = fields[1];
                    String contact = fields[2];
                    String phone   = fields[3];
                    String email   = fields[4];
                    String address = fields[5];
                    String notes   = fields[6];

                    //  CREATE

                    //  STEP 2c: ...and add the new object to our temporary list.
                    //  No number parsing needed for Supplier model as defined.
                    supplierList.add(new _15_ModelSupplier(id, company, contact, phone, email, address, notes));
                } catch (NumberFormatException e) {
                    //  Log an error if a supplier string doesn't have the expected number of fields.
                    error("Skipping malformed supplier data from server: " + sString);
                }
            } else {
                error(" (Expected 6 fields, found " + fields.length + ")"); //  debugging
            }
        } //  End of loop through supplier strings

        //  STEP 3: Turn our normal list into a JavaFX 'ObservableList'.
        //     Convert the standard ArrayList into a JavaFX ObservableList before returning.
        //     This allows the TableView to automatically observe changes to the list.
        return FXCollections.observableArrayList(supplierList);
    }








    /**
     *   CR "U" D: UPDATE SUPPLIER

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
    public void _26d_handleUpdateSupplier() {
        //  STEP 1: Get the SINGLE selected supplier from: "private final TableView<_13_ModelSupplier> supplierTable;"
        //     From the entire table provided by userTable, we only select the ONE ROW that is highlighted.
        _15_ModelSupplier selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();  //  singular

        //  STEP 2: "Fail-Fast" validation: Ensure exactly one supplier is selected. Fail if no row is highlighted.
        if (selectedSupplier == null) {
            _30i_showError("No Selection", "Please select a single supplier to update.");
            return; //  Stop execution if nothing is selected
        }

        //  STEP 3: Call the generic "Pop-Up Window Factory" with that supplier's info to show the update form.
        Optional<_15_ModelSupplier> updatedSupplierOptional = _31a_showAddDialog(

                //  Param 1: Popup Dialog Box Title
                "Update Supplier",

                //  Param 2: The "Form Factory" lambda. It tells us what fields to add.
                grid -> {
                    int row = 0;
                    //  Create fields matching _15_ModelSupplier properties.
                    var idField      = _30b_TextField(grid, "ID", row++); //  ID field
                    var companyField = _30b_TextField(grid, "Company", row++);
                    var contactField = _30b_TextField(grid, "Contact", row++);
                    var phoneField   = _30b_TextField(grid, "Phone", row++);
                    var emailField   = _30b_TextField(grid, "Email", row++);
                    var addressField = _30b_TextField(grid, "Address", row++);
                    var notesField   = _30b_TextField(grid, "Notes", row++);

                    //  STEP 3a: PRE-FILL to populate the form fields with the data from the selected supplier.
                    idField.setText(     selectedSupplier.getId());
                    idField.setEditable(false); //  IMPORTANT: Make the ID field non-editable. Primary keys should not be changed.
                    companyField.setText(selectedSupplier.getCompanyName());
                    contactField.setText(selectedSupplier.getContactName());
                    addressField.setText(selectedSupplier.getAddress());
                    emailField.setText(  selectedSupplier.getEmail());
                    phoneField.setText(  selectedSupplier.getPhone());
                    notesField.setText(  selectedSupplier.getNotes());

                    //  EXTRACT, VALIDATE & CREATE

                    //  Return the Supplier.
                    return () -> {

                        //  STEP 3b: EXTRACT:
                        String id      = idField.getText();
                        String company = companyField.getText();
                        String contact = contactField.getText();
                        String address = addressField.getText();
                        String email   = emailField.getText();
                        String phone   = phoneField.getText();
                        String notes   = notesField.getText();

                        //  STEP 3c: VALIDATE:
                        //  _20a_validAlphaNumeric(fieldType, fieldText, maxLength, description)

                        //  COMPANY
                        boolean boolCompany = _20a_validAlphaNumeric("Company", company, 2, 50);

                        //  CONTACT
                        boolean boolContact = _20a_validAlphaNumeric("Contact", contact, 2, 50);

                        //  PHONE
                        boolean boolPhone = _20a_validAlphaNumeric("Phone", phone, 10, 15);

                        //  EMAIL
                        boolean boolEmail = _20a_validAlphaNumeric("Email", email, 2, 50);

                        //  ADDRESS
                        boolean boolAddress = _20a_validAlphaNumeric("Address", address, 2, 50);

                        //  NOTES
                        boolean boolNotes = _20a_validAlphaNumeric("Description", notes, 0, 10000);


                        //  CONFIRM VALIDATION:
                        if( boolCompany || boolContact || boolPhone || boolEmail || boolAddress || boolNotes ) {

                            //  STEP 3d: CREATE: a new updated "ModelSupplier" object, if all checks pass.
                            //     Return the "Supplier<T>" for creating the updated object when "Save" is clicked.
                            return new _15_ModelSupplier(id, company, contact, phone, email, address, notes);
                        }
                        return null;
                    }; //  End of Supplier lambda
                },
                //  Param 3: 'onSave' is null. Network call after dialog closes.
                null
        ); //  End of _31a_showAddDialog call


        //  STEP 4: This code executes 'after' the update dialog is closed, and it comes from the definition of optional on top:
        //     "Optional<_13_ModelUser> updatedUserOptional = _31_ViewAdd._31a_showAddDialog()"
        //     '.ifPresent()' runs only if the user clicked "Save".
        updatedSupplierOptional.ifPresent(updatedSupplier -> {

            //  STEP 4a: Send the updated supplier object to the server.
            String serverResponse = communicator._03r_updateSupplier(updatedSupplier);

            //  STEP 4b: Process the server's response.
            if (serverResponse.startsWith("SUCCESS")) {
                _30h_showAlert("Success", "Supplier updated successfully.");
                _26b_handleRefreshTable(); //  Refresh table to show changes
            } else {
                _30i_showError("Failed to Update Supplier", serverResponse);
            }
        });
    }





    /**
     *   CRU "D": DELETE - REMOVE SUPPLIER(S)

     *   Called when the "Remove Selected" button is clicked.
     *   Handles the deletion of one or more suppliers selected in the table.
     *   Gets the list of selected suppliers, extracts their IDs, confirms with the user,
     *   sends a single batch-delete request to the server, and refreshes the table.
     */
    public void _26e_handleRemoveSupplier() {
        //  STEP 1: Get the list of all currently selected suppliers from the TableView.
        //  The table is configured for MULTIPLE selection mode in the constructor.
        ObservableList<_15_ModelSupplier> selectedSuppliers = supplierTable.getSelectionModel().getSelectedItems();

        //  STEP 2: "Fail-Fast" validation: Check if any suppliers were actually selected.
        if (selectedSuppliers == null || selectedSuppliers.isEmpty()) {
            _30i_showError("No Selection", "Please select one or more suppliers to remove.");
            return; //  Stop if nothing is selected
        }

        //  STEP 3: Show a confirmation dialog to prevent accidental deletion.
        Alert confirmDelete = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to remove " + selectedSuppliers.size() + " selected supplier(s)?",
                ButtonType.YES, ButtonType.NO //  Provide clear Yes/No options
        );

        //  STEP 4: Display the confirmation dialog and wait for the user to click "Yes" or "No" response.
        //     '.ifPresent()' executes the lambda only if the user clicks a button (doesn't close the dialog window).
        confirmDelete.showAndWait().ifPresent(response -> {
            //  STEP 5: Proceed only if the user explicitly clicked "YES".
            if (response == ButtonType.YES) {
                //  STEP 5a: Extract only the IDs from the selected Supplier objects.
                //  Uses Java Streams API for concise processing: map gets the ID, collect puts them in a List.
                List<String> idsToRemove = selectedSuppliers.stream()
                        .map(_15_ModelSupplier::getId) //  Method reference to getId()
                        .collect(Collectors.toList());

                //  STEP 5b: Send the list of IDs to the server for batch deletion via the communicator.
                String serverResponse = communicator._03q_removeSuppliers(idsToRemove);

                //  STEP 5c: Process the server's response.
                if (serverResponse.startsWith("SUCCESS")) {
                    _30h_showAlert("Success", idsToRemove.size() + " supplier(s) removed.");
                    _26b_handleRefreshTable(); //  Refresh the table to reflect deletions
                } else {
                    _30i_showError("Failed to Remove Suppliers", serverResponse);
                }
            }  //  If response is NO or the dialog is closed, do nothing.
        });
    }
}

