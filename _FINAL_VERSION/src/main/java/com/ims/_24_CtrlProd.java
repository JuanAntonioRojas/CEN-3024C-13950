package com.ims;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

//  Import our "toolbox" and "Lego brick" builders
import static com.ims._00_Utils.*;
import static com.ims._20_ValidBusinessRules.*;
import static com.ims._30_GUI_Builder.*;









/**
 *    This Controller Class is the "Product Department Manager", the "brain" for 'all' product-related actions on the client side.
 *    Its job is to translate user clicks (e.g., "Add," "Remove") into "phone calls" into network requests to the server via the communicator.
 *    So basically, this method is responsible for all the application's logic: adding, removing, and refreshing the table.
 *    It does NOT talk to the database directly. It only talks to the '_03_ClientServerLine' communicator.
 *    PS: I tried to keep the UI prompts simple.
 */
public class _24_CtrlProd {

    //  INSTANCE VARIABLES (THE "TOOLS")

    //  The "phone line" (given by the _23_CtrlMainSceneBuilder).
    private final _03_ClientServerLine communicator;

    //  The "engine block" (the TableView on the screen).
    private final TableView<_11_ModelProd> productTable;

    //  A constant for the CSV file chooser's starting directory (the root of the project).
    private final String directoryPath = "user.dir"; //  (The project root)




    /**
     *   CONSTRUCTOR
     *   The _23_CtrlMainSceneBuilder "hires" this manager and "gives" it the tools it needs (dependencies) to do its job.
     *      @param communicator The one-and-only "phone line."
     *      @param productTable The actual TableView from the UI.
     */
    public _24_CtrlProd(_03_ClientServerLine communicator, TableView<_11_ModelProd> productTable) {
        this.communicator = communicator;
        this.productTable = productTable;

        //  Now, we're making each row clickable with a tooltip
        _24i_initRowClickForDetails();
    }



    //=================================================================================================================

    //  VALIDATION OPERATIONS

    //=================================================================================================================

    /**
     *   This private helper method will do all the validation of all product fields from the dialog box.
     *   This abstraction follows the DRY (Don't Repeat Yourself) principle
     *   If any check validation fails, it shows an error and returns null and shows a GUI error message.
     *   If all checks pass, it builds and returns a new "_11_ModelProd" object.
     *      @return A new _11_ModelProd object if valid, or null if invalid.

     *  Bug: If I import a CSV with 100 or 1,000 bad rows, the user will get 100-1,000 popup windows they have to click "OK" on.
     *  This requires a Force Quit to stop, because of the subsequent popup storm.
     *  The Fix: A "Silent Mode" boolean var before validation to identify who needs the popup. Stay quiet if don't.

     *  Mosh addresses this issue in "CLEAN CODE" on Separation of concerns (view vs controller).  He calls the repetition
     *     of the popup a "tightly coupled" logic and view. He fixes this "Flag Argument" or "Code Smell" with the boolean
     *     that changes the behavior of the validation logic. I created this "Silent Mode" boolean for that reason.

     *   YOUTUBE:
     */
    private boolean _24a_validateProduct(String sku, String brand, String name, String description, String txtQtty, String txtPrice, String imageUrl, boolean silentMode) {
        try {
            //  Here each boolean will return TRUE if it passes the validation, else false.
            //  SKU
            _20a_validAlphaNumeric("Sku", sku, 2, 16);

            //  BRAND
            _20a_validAlphaNumeric("Brand", brand, 2, 20);

            //  NAME
            _20a_validAlphaNumeric("Name", name, 2, 50);

            //  DESCRIPTION
            _20a_validAlphaNumeric("Description", description, 0, 10000);

            //  QUANTITY:
            _20b_ValidQuantity(txtQtty);

            //  PRICE
            _20c_ValidPrice(txtPrice);

            //  IMAGE
            if (imageUrl != null && !imageUrl.isBlank()) {
                _20a_validAlphaNumeric("Image", imageUrl, 1, 100);
            }

            //  SUCCESS!
            //  If we get here, validation passed!  In other words:
            //  If input is invalid → this method never reaches return true; — it throws.
            //  If input is valid → it reaches return true (“I completed without throwing”).
            return true;
        }
        catch (IllegalArgumentException ex) {
            if (!silentMode) {  //  In case we get 100+ requests to validate, we don't want 100+ error messages.
                _30i_showError("Product error", ex.getMessage());
            }
            return false;
        }
    }





    // =================================================================================================================

    //  CRUD OPERATIONS

    // =================================================================================================================




    /**
     *  "C" RUD: CREATE: "ADD" A PRODUCT

     *   Called when the "Add" button is clicked.
     *   It uses our "_31_ViewAdd" factory to create/display a pop-up, with all the empty fields.
     */
    public void _24b_handleAddProduct() {

        //  STEP 1: Call the "Pop-Up Window Factory."
        //     This factory uses the following parameter:
        //        1. String title,
        //        2. Function< GridPane, Supplier<T> > formFactory,
        //        3. Consumer<T> onSave
        Optional<_11_ModelProd> newProductOptional = _31_ViewAdd._31a_showAddDialog(

            //  Parameter 1: Popup Dialog Box Title
            "Add New Product",

            //  Parameter 2: The "Form Factory" (add the fields we need).
            grid -> {
                int row = 0;

                //  We use our _30_GUI_Builder to make the fields, where we type the input:
                var skuField   = _30b_TextField(grid, "SKU", row++);
                var brandField = _30b_TextField(grid, "Brand", row++);
                var nameField  = _30b_TextField(grid, "Name", row++);
                var descrField = _30b_TextField(grid, "Description", row++);
                var qtyField   = _30d_NumberField(grid, "Quantity", row++);
                var priceField = _30d_NumberField(grid, "Price", row++);
                //var imageField = _30b_TextField(grid, "ImageUrl", row++);// deprecated for a button+field instead.


                //  IMAGE FINDER BUTTON & FIELD  (text field + Browse button)
                //     In this section we create a Browse Button that searches for the img in the "resources\img" folder

                int imageRow   = row;
                var imageField = _30b_TextField(grid, "Image", row++);

                //  STEP 1: We have to create the Browse button using the GUI builder from _31_ViewAdd:
                Button browseBtn = _31f_Btn("Browse...", 140, new Tooltip("Select this product's image"));

                //  STEP 1.1: We need to put it in column 2 of the same row as the Image field
                grid.add(browseBtn, 2, imageRow);

                //  STEP 2: Now, we prepare a FileChooser to go get the images
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Product Image");

                //  STEP 2.1: It has to only show "image" files to choose from
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
                );

                //  STEP 2.2: We start looking in "src/main/resources/img" (starting from the project root)
                File initialDir = new File("src/main/resources/img");
                if (initialDir.exists() && initialDir.isDirectory()) {
                    fileChooser.setInitialDirectory(initialDir);
                }

                //  STEP 3: We wire the button to its action
                browseBtn.setOnAction(e -> {
                    // Owner window is the popup stage that contains this grid
                    Stage stage = (Stage) grid.getScene().getWindow();
                    File chosen = fileChooser.showOpenDialog(stage);
                    if (chosen != null) {
                        // Store relative path as a classpath-style resource
                        String relativePath = "/img/" + chosen.getName();
                        imageField.setText(relativePath);
                    }
                });





                //  EXTRACT, VALIDATE & CREATE

                return () -> {

                    //  EXTRACT
                    String sku      = skuField.getText();
                    String brand    = brandField.getText();
                    String name     = nameField.getText();
                    String descript = descrField.getText();
                    String txtQtty  = qtyField.getText();
                    String txtPrice = priceField.getText();
                    String imageUrl = imageField.getText();

                    //  VALIDATE:
                    if( _24a_validateProduct(sku, brand, name, descript, txtQtty, txtPrice, imageUrl, false) ) {
                        int qtty = Integer.parseInt(txtQtty);
                        double price = Double.parseDouble(txtPrice);

                    //  CREATE: a new "ModelProd" object, if all checks pass.
                        return new _11_ModelProd(sku, brand, name, descript, qtty, price, imageUrl);
                    }

                    //  If the validation failed, it returns 'null', then  "Optional<T>_31a_showAddDialog()" will see 'null' and return Optional.empty()
                    return null;
                };
            },  //  end of the pop-up grid creator

            //  Parameter 3: 'onSave' is null, cause "new product" got created on validator
            null
        );  //  end of Optional<_11_ModelProd> newProductOptional: OK = "new product" or Cancel = "empty"


        //  STEP 2: If the user clicked "OK" and a product was created, we send it to the server, to add to DB.
        //     This code runs 'after' the pop-up closes. 'ifPresent' means: "If the user clicked 'Save'..."
        newProductOptional.ifPresent(product -> {
            //  STEP 2a: take the 'product' object from the server and send it through the communicator (_03_) to the DB.
            String serverResponse = communicator._03g_addProduct(product);

            //  STEP 2b: Check the server's reply.
            if (serverResponse.startsWith("SUCCESS")) {
                _30h_showAlert("Success", "Product added successfully.");
                _24d_handleRefreshTable(); //  Refresh the table!
            } else {
                _30i_showError("Failed to Add Product", serverResponse);
            }
        } );
    }





    /**
     *   "Load From CSV"
     *   Called when the "Load from CSV" button is clicked.
     *   This all happens on the *client-side*. The client reads the file, turns it into a *List*, and sends that 'one big list' to the server.
     */
    public void _24c_handleLoadFromCSV() {
        //  STEP 1: Open a "File Chooser" dialog box.
        //     This is standard JavaFX code to ask the user "Which file do you want to open?"
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open a Product List File. Tab Separated Value (TSV)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TSV Files", "*.tsv"));
        fileChooser.setInitialDirectory(new File(System.getProperty(directoryPath)));

        //  STEP 2: Show the dialog and wait for the user to pick a file.
        //     The code 'pauses' here until the user selects a file or clicks "Cancel".
        File file = fileChooser.showOpenDialog(new Stage());

        //  STEP 3: If the user 'did' pick a file... (if 'file' is not null)
        if (file != null) {
            //  We'll keep track of how many rows are good (loaded) and bad (rejected).
            int loadedCount = 0;
            int rejectCount = 0;
            //  This is the list that will hold all the 'good' products we find.
            List<_11_ModelProd> productsToLoad = new ArrayList<>();

            //  STEP 4: Use a 'try-with-resources' to safely read the file, line by line.
            //     This 'try' block makes sure the file is automatically closed, even if an error happens.
            try (BufferedReader bufrReadr = new BufferedReader(new FileReader(file))) {

                //  We read the first line *before* the loop, to skip the header row (e.g., "SKU,Brand,Name...").
                bufrReadr.readLine(); //  Skip the header row!

                String line;
                //  This 'while' loop reads the file one line at a time until it reaches the end (null).
                while ((line = bufrReadr.readLine()) != null) {
                    //  STEP 4a: Split the line into an array of strings. We expect 6 columns.
                    String[] fields = line.split("\\t", 7);

                    //  First check: Does this row even have 6 columns?
                    if (fields.length == 7) {
                        //  If it does, we 'try' to parse it.
                        //  We use a 'try/catch' block because parsing numbers can fail (e.g., "abc").
                        try {
                            //  STEP 4b: Parse and EXTRACT each field from the line split:
                            String sku = fields[0];
                            String brand = fields[1];
                            String name = fields[2];
                            String description = fields[3];
                            String txtQty = fields[4];
                            String txtPrice = fields[5];
                            String imageUrl = fields[6];

                            /* This is not needed anymore
                            //  Here we use our '_20a_validAlphaNumeric' helper from _00_Utils, that looks like:
                            //  _20a_validAlphaNumeric(fieldType, fieldText, maxLength, description)

                            //  SKU
                            boolean boolSku = _20a_validAlphaNumeric("Sku", sku, 20);
                            if (!boolSku) { _30i_showError("Sku error:", please + "sku (2–20)" + chars); }

                            //  CATEGORY
                            boolean boolCat = _20a_validAlphaNumeric("Brand", brand, 50);
                            if (!boolCat) { _30i_showError("Brand error:", please + "brand (2–50)" + chars); }

                            //  NAME
                            boolean boolName = _20a_validAlphaNumeric("Name", name, 50);
                            if (!boolName) { _30i_showError("Name error:", please + "name (2–50)" + chars); }

                            //  DESCRIPTION
                            boolean boolDescr = _20a_validAlphaNumeric("Description", description, 100);
                            if (!boolDescr) { _30i_showError("Description error:", please + "description (2–100)" + chars); }

                            //  STEP 4d: NUMBER VALIDATIONS
                            //  Now we use our 'validQuantity' and 'validPrice' helpers.
                            //  These will 'throw an exception' if the text isn't a valid number
                            //  OR if it fails our logic (e.g., quantity = -5).
                            int quantity = validQuantity(txtQty);
                            double price = validPrice(txtPrice);
                            */

                            //__________________________________________________________________________________________

                            //  STEP 4c: VALIDATIONS
                            //__________________________________________________________________________________________

                            boolean validProduct = _24a_validateProduct(sku, brand, name, description, txtQty, txtPrice, imageUrl, true);
                            //  Bug: If I import a CSV with 100 or 1,000 bad rows, the user will get 100 popup windows they have to click "OK" on.
                            //  This requires a Force Quit to stop.
                            //  The Fix: A "Silent Mode" boolean var before validation to identify who needs the popup.

                            //  If any of these alphanumeric fields are invalid (too short, too long, invalid characters, etc.)...
                            if (!validProduct) {
                                rejectCount++;  //  ...then we count it as a reject.
                                continue;   //  'continue' tells the loop: "Stop processing this row and jump to the next line."
                            } else {
                                //  STEP 4e: SUCCESS!
                                //  If the code reaches this point, the row is 100% valid!
                                //  1st, got to convert the text input, which validated OK, into numbers, so we can create the new product:
                                int qty = Integer.parseInt(txtQty);
                                double price = Double.parseDouble(txtPrice);

                                //  We now create a new product, and add it to our 'in-memory' array list that came from top:
                                //     "List<_11_ModelProd> productsToLoad = new ArrayList<>();"
                                productsToLoad.add(new _11_ModelProd(sku, brand, name, description, qty, price, imageUrl));
                                loadedCount++;
                            }

                        } catch (Exception e) {
                            //  This 'catch' block will "catch" any error from Step 4d.
                            //  (e.g., NumberFormatException, or our custom error for a negative price).
                            rejectCount++; //  Bad data (e.g., "abc" for quantity)
                        }
                    } else {
                        //  This 'else' block catches rows that didn't have 7 columns from Step 4a.
                        rejectCount++; //  Wrong number of columns
                    }
                }
            } catch (IOException e) {

                //  This 'catch' block is for a *major* error, like the file being unreadable.
                //  Here, we *do* show a pop-up because the whole import failed.
                _30i_showError("File Read Error", "Could not read the file: " + e.getMessage());
                return; //  Exit the method.
            }

            //  STEP 5: After the loop is finished, check if our 'good' list has anything in it (if the list isn't empty).
            if (!productsToLoad.isEmpty()) {
                //  STEP 5a: ...send the entire batch to the server in 'one' phone call.
                String serverResponse = communicator._03j_bulkAddProducts(productsToLoad);

                //  STEP 5b: Show the final summary pop-up!
                if (serverResponse.startsWith("SUCCESS")) {
                    _30h_showAlert("Import Successful", serverResponse + "\nRejected rows: " + rejectCount);
                    _24d_handleRefreshTable(); //  Refresh the table! (Using the correct method name)
                } else {
                    _30i_showError("Import Failed", serverResponse);
                }
            } else {
                //  If the list is empty (e.g., all rows were bad, or the file was empty)
                _30h_showAlert("Import Notice", "No valid products found in the file. But " + rejectCount + " were rejected.");
            }
        }
        //  NOTE: If 'file' was null (user clicked "Cancel"), we do nothing. The method just ends.
    }






    /**
     *   C "R" UD: READ

     *   REFRESH TABLE

     *   Essential utility after every action. This is the most important method in this class.
     *   This method is called when the "Refresh" button is clicked, or a CRUD operation happens.
     *   It sends a "GET_ALL_PRODUCTS" request to the server, receives a formatted string of data, parses that data, and
     *      updates the product table in the UI.

     *   An "ObservableList" is a special type of List in JavaFX that notifies listeners whenever it's modified.
     *   It's a key part of the Model-View-Controller (MVC) design pattern used by JavaFX.
     *   It automatically sends notifications (events) when it "observes" that items are added, removed, or updated.
     *   When a change occurs in the ObservableList, the UI component (e.g. TableView, ListView, or ComboBox)
     *       automatically updates itself without requiring to manually refresh the view.
     *   This is crucial for keeping the UI synchronized with the data and make the app so responsive and easy to work with.
     */

    public void _24d_handleRefreshTable() {
        println("Attempting to refresh product table...");

        //  STEP 1: Make the "phone call" to the server.
        String serverResponse = communicator._03f_getAllProducts();

        //  STEP 2: Parse the server's response (e.g., "SUCCESS|data...").
        String[] parts = serverResponse.split("\\|", 2);
        String status = parts[0];

        if ("SUCCESS".equals(status)) {
            //  STEP 3: If it was a success, get the 'data' part. (It might be empty if the DB is empty).
            String data = (parts.length > 1) ? parts[1] : "";

            //  STEP 4: Give the raw data string to our 'parser' helper to turn it into a 'List' of objects.
            //  The data will be a long string of products separated by semicolons.
            ObservableList<_11_ModelProd> products = _24h_parseProductData(data);

            //  STEP 5: This is the magic. We tell the JavaFX 'productTable' to display this new list. The screen updates automatically.
            productTable.setItems(products);
            println(products.size() + " products loaded into the table.");

        } else {
            //  STEP 3 (Failure): If the server sent "FAILURE|Message", we show that message in a pop-up.
            String errorMessage = (parts.length > 1) ? parts[1] : "Unknown error.";
            _30i_showError("Failed to Load Products", errorMessage);
        }
    }





    /**
     *  CR "U" D: UPDATE

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
    public void _24e_handleUpdateProduct() {
        //  STEP 1: Get the single item the user selected from the table.
        _11_ModelProd selectedProduct = productTable.getSelectionModel().getSelectedItem();

        //  STEP 2: "Fail-Fast." Check if they selected zero or more than one item.
        //  This is a "guard clause" that stops the method early if nothing is selected.
        if (selectedProduct == null) {
            _30i_showError("No Selection", "Please select a 'single' product to update.");
            return; //  Exit the method immediately.
        }

        //  STEP 3: Call the "Pop-Up Window Factory" again.
        //  This is the exact same dialog factory we used in _24a_handleAddProduct.
        Optional<_11_ModelProd> updatedProductOptional = _31_ViewAdd._31a_showAddDialog(

                //  Parameter 1: The title.
                "Update Product",

                //  Parameter 2: The "Form Factory."
                //  This is the set of instructions for building the form.
                grid -> {
                    int row = 0;
                    //  Create all the fields...
                    var skuField   = _30b_TextField(grid, "SKU", row++);
                    var brandField = _30b_TextField(grid, "Brand", row++);
                    var nameField  = _30b_TextField(grid, "Name", row++);
                    var descrField = _30b_TextField(grid, "Description", row++);
                    var qtyField   = _30d_NumberField(grid, "Quantity", row++);
                    var priceField = _30d_NumberField(grid, "Price", row++);
                    var imageField = _30b_TextField(grid, "ImageUrl", row++);

                    //  STEP 3a: POPULATE THE FIELDS:
                    //  Pre-fill the fields with the data from the 'selectedProduct'. (This is what makes "Update" different from "Add").
                    skuField.setText(  selectedProduct.getSku());
                    brandField.setText(  selectedProduct.getBrand());
                    nameField.setText( selectedProduct.getProdName());
                    descrField.setText(selectedProduct.getDescription());
                    qtyField.setText(String.valueOf(    selectedProduct.getQuantity()));
                    priceField.setText(String.valueOf(  selectedProduct.getPrice()));
                    imageField.setText(selectedProduct.getImageUrl());

                    //  STEP 3b: Lock the SKU field. The SKU is the 'Primary Key' (ID) and should never be changed during an update.
                    skuField.setEditable(false);





                    //  EXTRACT, VALIDATE & CREATE

                    //  Return the Supplier. This is the code that runs when the user clicks "Save".
                    return () -> {// Call the single, centralized validation method that will return a new product object.

                        //  EXTRACT
                        String sku      = skuField.getText();
                        String brand    = brandField.getText();
                        String name     = nameField.getText();
                        String descript = descrField.getText();
                        String txtQtty  = qtyField.getText();
                        String txtPrice = priceField.getText();
                        String imageUrl = imageField.getText();

                        //  VALIDATE:
                        //  We just make one single call to our powerful VALIDATION method, passing the values from the text fields.
                        if( _24a_validateProduct(sku, brand, name, descript, txtQtty, txtPrice, imageUrl, false) ) {
                            int qtty = Integer.parseInt(txtQtty);
                            double price = Double.parseDouble(txtPrice);

                        //  CREATE: a new "ModelProd" object, if all checks pass.
                        //     If validation passes, it returns a valid "_11_ModelProd" object.
                            return new _11_ModelProd(sku, brand, name, descript, qtty, price, imageUrl);
                        }

                        //  If validation fails, _24a_validateAndBuildProduct will show an error pop-up and return 'null'.
                        return null;
                    };
                },
                null //  Parameter 3: 'onSave' is null.
        );


        //  STEP 4: This code runs after the pop-up closes, (if the user clicked "Save").
        //     'updatedProductOptional.ifPresent' means: "If the 'Recipe' (from Step 3) was successful and returned a valid
        //     product (not null)..." and it comes from top:  "Optional<_11_ModelProd> updatedProductOptional = _31_ViewAdd._31a_showAddDialog()"
        updatedProductOptional.ifPresent(updatedProduct -> {

            //  STEP 4a: ...send the updated product object to the server.
            String serverResponse = communicator._03i_updateProduct(updatedProduct);

            //  STEP 4b: Check the server's reply and show a final confirmation.
            if (serverResponse.startsWith("SUCCESS")) {
                _30h_showAlert("Success", "Product updated successfully.");
                //  Finally, refresh the table to show the new data!
                _24d_handleRefreshTable(); //  Refresh the table!
            } else {
                _30i_showError("Failed to Update Product", serverResponse);
            }
        });
    }








    /**
     *   CRU "D": DELETE

     *   REMOVE PRODUCT

     *   This method is called when the "REMOVE" button is clicked.
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
    public void _24f_handleRemoveProduct() {
        //  STEP 1: Get a 'List' of all items the user has highlighted (selected) in the table.
        ObservableList<_11_ModelProd> selectedProducts = productTable.getSelectionModel().getSelectedItems();

        //  STEP 2: "Fail-Fast." Check if they forgot to select anything.
        if (selectedProducts == null || selectedProducts.isEmpty()) {
            _30i_showError("No Selection", "Please select one or more products to remove.");
            return;
        }

        //  STEP 3: Show a "Are you sure?" confirmation box.
        Alert confirmDelete = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to remove " + selectedProducts.size() + " selected product(s)?",
                ButtonType.YES, ButtonType.NO
        );

        //  STEP 4: This 'showAndWait' pauses the code and waits for the user to click "Yes" or "No."
        confirmDelete.showAndWait().ifPresent(response -> {
            //  STEP 5: If the user clicked "Yes"...
            if (response == ButtonType.YES) {
                //  STEP 5a: Get a List of just the SKUs from the selected product objects.
                List<String> skusToRemove = selectedProducts.stream()
                        .map(_11_ModelProd::getSku)
                        .collect(Collectors.toList());

                //  STEP 5b: Send that one list of SKUs to the server to be batch-deleted.
                String serverResponse = communicator._03h_removeProduct(skusToRemove);

                if (serverResponse.startsWith("SUCCESS")) {
                    _30h_showAlert("Success", skusToRemove.size() + " product(s) removed.");
                    _24d_handleRefreshTable(); //  Refresh the table!
                } else {
                    _30i_showError("Failed to Remove Products", serverResponse);
                }
            }
            //  ...if they click "No," we do nothing.
        });
    }






    /**
     *    PRIVATE HELPER METHOD: PARSE PRODUCT DATA
     *    This is a private "helper" tool, whose only job is to take the raw data string from the server and "parse" it
     *       into a usable List of Product objects.
     *    Think of this method as a "translator" that converts raw server text into Java objects.
     *    THE PROBLEM: The server sends us one long string like this:
     *        "SKU001|Electronics|Laptop|Fast computer|10|999.99;SKU002|Books|Java Guide|Learn Java|5|29.99"
     *    THE GOAL: Turn that string into a JavaFX-friendly list of Product objects that the table can display.
     *    ANALOGY: Imagine you received a grocery list written as one long sentence:
     *         "apples-red-5-dollars;bananas-yellow-3-dollars;milk-white-4-dollars"
     *    This method would separate it into individual items you can put in your shopping cart.

     *      @param data The raw string from the server (products separated by semicolons, fields separated by pipes)
     *      @return An 'ObservableList' (a special JavaFX list) that automatically updates the UI when changed
     */
    private ObservableList<_11_ModelProd> _24h_parseProductData(String data) {
        //  STEP 1: Create an empty "shopping cart" (ArrayList) to hold our Product objects.
        //     Think of this as an empty basket where we'll put each item as we process it.
        List<_11_ModelProd> productList = new ArrayList<>();

        //  STEP 2: GUARD CLAUSE - Check if we got any data at all.
        //     If the server sent us nothing (null) or an empty string (""), there's nothing to parse.
        //     It's like opening an envelope and finding it empty - we just return an empty list.
        if (data == null || data.isEmpty()) {
            return FXCollections.observableArrayList(productList);
        }

        //  STEP 3: Split the data into individual products
        //     The server separates each product with a semicolon (;).
        String[] productStrings = data.split(";");

        //  STEP 4: Loop through each product string
        //     Now we have an array of strings, where each string represents ONE product.
        for (String pString : productStrings) {
            //  STEP 4a: Split this product's string into its 6 individual fields
            //     Each product has 6 pieces of information separated by pipe symbols (|):
            //     SKU|Brand|Name|Description|Quantity|Price
            String[] fields = pString.split("\\|", 7);
            //  STEP 4b: VALIDATE - Make sure we got exactly 7 fields.
            //     If we got more or fewer, this row is malformed (corrupted/incomplete) and we skip it.
            if (fields.length == 7) {
                //  STEP 4c: WE PARSE THE FIELDS (with error protection)
                try {
                    //  EXTRACT: Pull out each field from the array and give it a meaningful name.
                    String sku = fields[0];
                    String brand = fields[1];
                    String name = fields[2];
                    String description = fields[3];
                    //  CONVERT: The last two fields are numbers stored as text.
                    int quantity = Integer.parseInt(fields[4]);
                    double price = Double.parseDouble(fields[5]);
                    String imageUrl = fields[6];

                    //  STEP 4d: CREATE A NEW PRODUCT OBJECT
                    //     Now that we have all the data in the correct format, we create a new Product object.
                    //     Then we add this new product to our "shopping cart" (productList).
                    productList.add(new _11_ModelProd(sku, brand, name, description, quantity, price, imageUrl));
                }
                //  STEP 4e: ERROR HANDLING - Something went wrong during parsing.
                //     This happens if quantity or price isn't a valid number.
                catch (NumberFormatException e) {
                    //  ANALOGY: If one apple in the bag is rotten, throw it out and keep the good ones.
                    error("Skipping malformed product data from server: " + pString);
                    //  The loop continues automatically after this catch block.
                }
            }
        }

        //  STEP 4: Turn our normal 'ArrayList' into the special 'ObservableList' that JavaFX needs.
        return FXCollections.observableArrayList(productList);
    }






    /**
     *   PRIVATE HELPER METHOD: INITIALIZE ROW CLICK FOR DETAILS

     *   This method sets up "click behavior" for the product table.
     *   It makes each row interactive - hovering shows a tooltip, and clicking shows full details.
     *   This class will wire the product table so that hovering shows a tooltip and clicking a row shows details.
     *   Think of this as "training" the table to respond to mouse actions.

     *   WHEN IT RUNS: This is called once in the constructor when this controller is created.
     *   WHAT IT DOES:
     *      1. When you hover over a row → A tooltip appears saying "Click to view product details"
     *      2. When you click a row → A pop-up shows all the product's information
     *   ANALOGY: It's like setting up a product catalog where hovering over an item shows a preview,
     *      and clicking on it opens the full product page.
     */

    private void _24i_initRowClickForDetails() {
        //  STEP 1: Set a "row factory" for the table
        //     A "row factory" is a special function that tells JavaFX: "Here's how I want to build each row."
        //     Every time the table needs to display a row, it calls this factory to create it.
        productTable.setRowFactory(tv -> {
            //  STEP 2: Create a new table row
            //     This is the "blueprint" for one row in the table.
            TableRow<_11_ModelProd> row = new TableRow<>();

            //  STEP 3: Create a tooltip
            //    This is a small pop-up text box that appears when you hover the mouse over something.
            Tooltip tip = new Tooltip("Click to view product details");
            //    Tooltip.install() is like attaching or putting a sticky note on the row.
            Tooltip.install(row, tip);

            //  STEP 5: Set up a mouse click listener
            //     This tells Java: "When someone clicks (event) on this row, run the code inside these curly braces { }."
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    //  STEP 7: Get the product data from this row
                    _11_ModelProd clicked = row.getItem();
                    //  STEP 8: Show the details pop-up
                    _24j_showProductDetails(clicked);
                }
            });
            //
            //  STEP 9: Return the configured row
            return row;
        });
    }







    /**
     *   PRIVATE HELPER METHOD: SHOW PRODUCT DETAILS

     *   This method creates and displays a pop-up window (Alert dialog) that shows ALL information about a single
     *      product in an easy-to-read format. It's called when the user clicks on a row in the table.
     *   This class will show a popup with all details for a single product.

     *   ANALOGY: Think of this as opening a "product information card" in an online store.
     *      You click on a product thumbnail, and a detailed view pops up with all the specs.

     *   @param prod The product object to display (can be null if the row was empty)
     */

    private void _24j_showProductDetails(_11_ModelProd prod) {
        //  STEP 1: GUARD CLAUSE - Check if we actually received a product.
        //     If 'prod' is null, there's nothing to show, so we exit immediately.
        if (prod == null) return;


        /*/  STEP 2: Now we show the details in a pop-up alert dialog box
        //     This is an informational message (not an error or warning)  */
        Alert infoPopup = new Alert(Alert.AlertType.INFORMATION);
        infoPopup.setTitle("Product Details");
        infoPopup.setHeaderText(prod.getBrand() + " (" + prod.getProdName() + ")");

        //  This is to make sure the stylesheet is applied to THIS dialog
        infoPopup.getDialogPane().getStylesheets().add(
                Objects.requireNonNull( getClass().getResource("/styles.css") ).toExternalForm()
        );

        //  This is a specific class for the entire alert box
        infoPopup.getDialogPane().getStyleClass().add("product-alert");

        /* Deprecated: this places the text into the TextArea. I want it to the left of the image.
        //  STEP 3: Build the display text node for multiline content (with formatting)

        //  THE TEMPLATE SYNTAX:
        //    %s  = Insert a String (text)
        //    %d  = Insert an integer (whole number)
        //    %.2f = Insert a decimal number with exactly 2 digits after the decimal point
        //    %n  = Insert a line break (newline) - works on any operating system
        String text = String.format(
                //  This is the template (the text with placeholders):
                "SKU: %s%n" +                // %s = product SKU
                        "Brand: %s%n" +              // %s = product brand    | %n = newline
                        "Name: %s%n" +               // %s = product name
                        "Quantity: %d%n" +           // %d = quantity (integer)
                        "Price: $%.2f%n%n" +         // %.2f = price (2 decimal places) | %n%n = double newline (blank line)
                        "Description:%n%s",          // %s = description (might be multi-line)

                //  These are the actual values (in the same order as the placeholders above)
                prod.getSku(),
                prod.getBrand(),
                prod.getProdName(),
                prod.getQuantity(),
                prod.getPrice(),
                prod.getDescription()
        );

        TextArea txtArea = new TextArea(text);
        txtArea.setEditable(false);
        txtArea.setWrapText(true);
        txtArea.setPrefRowCount(10);
        */


        // STEP 3: ---------- TOP-LEFT: basic info (NOT in TextArea) ----------
        Label skuLabel   = new Label("SKU #:   "  + prod.getSku());
        Label brandLabel = new Label("Brand:   "  + prod.getBrand());
        Label nameLabel  = new Label("Name:    "  + prod.getProdName());
        Label qtyLabel   = new Label("Qtty:    "  + prod.getQuantity());
        Label priceLabel = new Label(String.format("Price: $ %.2f", prod.getPrice()));

        VBox txtArea = new VBox(4, skuLabel, brandLabel, nameLabel, qtyLabel, priceLabel);
        txtArea.setAlignment(Pos.TOP_LEFT);


        // STEP 4: ---------- TOP-RIGHT: product image ----------
        ImageView imgView = null;
        String imgUrl = prod.getImageUrl();   // e.g. "/img/1.jpg"

        if (imgUrl != null && !imgUrl.isBlank()) {
            try {
                Image img = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(imgUrl), "Image not found on classpath: " + imgUrl)
                );
                //  STEP 4.1: We need to prepare the loaded img as a graphic
                //Image img = new Image(imgUrl, 200, 200, true, true);//  No bueno
                imgView = new ImageView(img);
                imgView.setFitWidth(200);  //  reasonable dimensions
                imgView.setPreserveRatio(true); //  I set the imgs at 700x700 px, but this here helps
            }
            catch (Exception ex) {  println("Could not load product image: " + ex.getMessage());  }
        }

        //  We could wrap the image in a StackPane, so we can style it
        StackPane imageHolder = new StackPane();
        if (imgView != null) {
            imageHolder.getChildren().add(imgView);
        }
        imageHolder.getStyleClass().add("product-image");



        //  STEP 5: ---------- TOP ROW: info (left) + image (right) ----------
        //  Build the content layout (image + text). Put image and text into a HBox
        HBox topRow = new HBox(20, txtArea, imageHolder);
        topRow.setAlignment(Pos.TOP_LEFT);


        //  STEP 6: ---------- BOTTOM ROW: Description inside TextArea ----------
        Label descLabel = new Label("Description:");
        TextArea descArea = new TextArea(prod.getDescription());
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(5);

        // ---------- MAIN CONTENT LAYOUT ----------
        VBox content = new VBox(15, topRow, descLabel, descArea);
        content.setAlignment(Pos.TOP_LEFT);


        // Attach VBox to the dialog
        infoPopup.getDialogPane().setContent(content);


        //  This is a tiny icon in the header
        Image smallImg = new Image( Objects.requireNonNull( getClass().getResourceAsStream("/img/smBox.png") ) );
        ImageView smallIconView = new ImageView(smallImg);
        smallIconView.setFitWidth(70);
        smallIconView.setFitHeight(70);
        smallIconView.setPreserveRatio(true);


        infoPopup.setGraphic(smallIconView);   // this is the header icon


        //  STEP 6: We need to display the image as a graphic in the popup.
        infoPopup.showAndWait();
    }
}