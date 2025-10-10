package com.example.ims;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;

import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 *   This class is like my button click brain. Each public method is one action that a user can trigger
 *      (add, remove, update, refresh). So basically, it's responsible for all the application's logic:
 *      connecting to the database, adding/removing products, and refreshing the table.
 *   I tried to keep the UI prompts simple and do some validation, so the program won’t crash.
 *   TODO: customize the inputs and beautify them with CSS.
**/

public class Actions {

    //
    private final DatabaseManager dbManager;
    private final TableView<Product> productTable;

    //  Setting the initial directory to the root of the project
    private final String directoryPath = "user.dir";


    /**
     *  With this constructor we establish the dependency injection, so that the class can do its job without
     *    creating its own dependencies. Without this, the Actions class wouldn't know about the database or the table.
     *  If it created its own DatabaseManager and TableView dependencies it would lead to separate, unmanaged instances,
     *     or be unable to perform any actions at all.
     *  We pass the Actions class a single instance of the "DatabaseManager", to connect to & interact with the DB.
     *  We also give it access to the specific "TableView" object (a Generic Product) that is displayed on the screen.
     *  When a user adds or removes a product, the Actions class needs a reference to this product to refresh its contents.
     *  By passing these 2 arguments to the constructor, we ensure that Actions is working with the same components that
     *     were created by the main InventoryApp and displayed by the Frame class, preventing disconnected functions
     *     and resource leaks... for a safe and secure society, but without infringing on liberty.
     **/

    public Actions(DatabaseManager dbManager, TableView<Product> productTable) {

        this.dbManager = dbManager;
        this.productTable = productTable;

        // This way we enable multiple selection on the table. I like selecting multiple rows, so I can delete a few at once
        productTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }




    //  REFRESH TABLE
    //  Essential utility after every action.
    //  An "ObservableList" is a special type of List in JavaFX that notifies listeners whenever it's modified.
    //  It's a key part of the Model-View-Controller (MVC) design pattern used by JavaFX.
    //  It automatically sends notifications (events) when it "observes" that items are added, removed, or updated.
    //  When a change occurs in the ObservableList, the UI component (e.g. TableView, ListView, or ComboBox)
    //      automatically updates itself without requiring to manually refresh the view.
    //  This is crucial for keeping the UI synchronized with the data and JavaFX applications so responsive and easy to work with.
    public void refreshTable() {
        ObservableList<Product> data = dbManager.getAllProducts();
        productTable.setItems(data);
    }



    //  1. ADD BUTTON

    //  This will create a popup mssg box for adding a single product

    /* Legacy code...
    public void addButton() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add a Product to the Database");
        dialog.setHeaderText("Enter product details (SKU, Name, Desc, Qty, Price)");
        dialog.setContentText("Format: 123456, Any Name, Whatever Description, 2, 9.99");

        //  TODO: create separate mssg boxes for each: ID, name, etc.
        //  DONE!

        *//**
         *  The "showAndWait" displays a "dialog box" on the screen and then pauses for the user to enters some data.
         *  It returns an Optional object, which is a container that may (or may not) hold a value (the text entered).
         *  The "ifPresent" checks if the optional contains a value, in which case the anonymous lambda is executed.
         *  The variable "input" is a (temporary) name for the string that the user typed in, that was returned by the dialog.
         *  For "hardened" security to prevent SQL injections we use a "prepared statements" in the DatabaseManager class,
         *     not by validating the input from this "dialog" object (TextInputDialog).
         *  However, this code ensures that the data being passed to the "prepared statement" is in the expected format.
        **//*

        dialog.showAndWait().ifPresent(input -> {
            try {
                *//**
                 *  This next line splits the input string into an array of substrings using a comma as a delimiter.
                 *  The key part is the limit "6", instructing the split() method to perform a maximum of five splits,
                 *    resulting in an array of up to six elements.
                 *  This is a defensive technique that prevents an "ArrayIndexOutOfBoundsException" if a data line contains
                 *   unexpected commas, ensuring that any extra commas within a field (like a product description)
                 *   are treated as part of the final element rather than creating new ones.
                 **//*
                String[] field = input.split(",", 6);
                if (field.length == 6) {  //  Checking the number of parts entered (subject to change if separate inputs)
                    String sku = field[1].trim();
                    if (dbManager.doesProductExist(sku)) {  //  to prevent data duplication
                        new Alert(Alert.AlertType.ERROR, "SKU already exists.").showAndWait();
                    } else {
                        String category = field[0].trim();
                        String name = field[2].trim();
                        String descr = field[3].trim();
                        int qty = Integer.parseInt(field[4].trim());
                        double price = Double.parseDouble(field[5].trim());
                        Product newProduct = new Product(category, sku, name, descr, qty, price);
                        dbManager.addProduct(newProduct);
                        refreshTable();
                    }
                } else {
                    new Alert(Alert.AlertType.ERROR, "Invalid input format.").showAndWait();
                }
            } catch (NumberFormatException e) {  //  catch errors to ensure quantity and price are valid numbers.
                new Alert(Alert.AlertType.ERROR, "Invalid number format for quantity or price.").showAndWait();
            }
        });
    }  //  END LEGACY ADD BUTTON
    */



    public void addButton() {

        //  Create a new Stage for the add pop-up dialog box
        Stage addPopup = new Stage();
        addPopup.initModality(Modality.APPLICATION_MODAL);
        addPopup.setTitle("Add Product");

        //  LAY
        //  OUT AND UI CONTROLS (using a GridPane, like a CSS square grid. Flex won't cut it)
        GridPane gridAdd = new GridPane();
        gridAdd.setHgap(10);
        gridAdd.setVgap(10);
        gridAdd.setPadding(new Insets(20));


        //  LABELS AND TEXT FIELDS for each product
        //  We create blank TextFields (and their labels) to add a new product to the DB

        //  SKU
        TextField skuField = new TextField();
        gridAdd.add(skuField, 1, 1);
        gridAdd.add(new Label("Sku:"), 0, 1);


        //  CATEGORY
        //TextField categoryField = new TextField();  //  LEGACY
        //  We add a list of the predefined categories for our dropdown menu
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Electronics", "Appliances", "Furniture", "Paintings", "Books", "etc.");

        ComboBox<String> categoryCombo = new ComboBox<>(categories);
        categoryCombo.setValue("Electronics"); // Set a default value if desired

        // Set the cell factory to a ComboBoxTableCell
        //categoryCombo.setCellFactory(ComboBoxTableCell.forTableColumn(categories));
        gridAdd.add(new Label("Category:"), 0, 0);
        gridAdd.add(categoryCombo, 1, 0);


        //  NAME
        TextField nameField = new TextField();
        gridAdd.add(nameField, 1, 2);
        gridAdd.add(new Label("Name:"), 0, 2);


        //  DESCRIPTION
        TextField descriptionField = new TextField();
        gridAdd.add(descriptionField, 1, 3);
        gridAdd.add(new Label("Description:"), 0, 3);


        //  QUANTITY
        TextField quantityField = new TextField();
        gridAdd.add(new Label("Quantity:"), 0, 4);
        gridAdd.add(quantityField, 1, 4);


        //  PRICE
        TextField priceField = new TextField();
        gridAdd.add(new Label("Price:"), 0, 5);
        gridAdd.add(priceField, 1, 5);



        //  SAVE BUTTON
        Button saveButton = new Button("Add Product");

        //  Set up the event listener for the "save" button
        saveButton.setOnAction(e -> {
            try {
                //  Next we read data from the previous fields
                //  Note: The best way to prevent SQL injections is to use Prepared Statements on the DB mgr class
                String sku = skuField.getText().trim();
                String category = categoryCombo.getValue();
                String name = nameField.getText().trim();
                String descr = descriptionField.getText().trim();
                int qty = Integer.parseInt(quantityField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());

                // Validate that the SKU is not a duplicate before adding
                if (dbManager.doesItExist(sku)) {
                    new Alert(Alert.AlertType.ERROR, "SKU already exists.").showAndWait();
                } else {
                    // Create a new Product object and add it to the database
                    Product newProduct = new Product(sku, category, name, descr, qty, price);

                    //  Here, the magic happens... All the product info (fields) gets moved to the DB
                    dbManager.addProduct(newProduct);

                    // Refresh the main table and close the dialog
                    refreshTable();
                    addPopup.close();
                }
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid number format for quantity or price.").showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "An error occurred: " + ex.getMessage()).showAndWait();
            }
        });

        //  Save button added to the grid
        gridAdd.add(saveButton, 1, 6);

        //  Finally we create a scene and pass it the grid to add a product
        Scene dialogScene = new Scene(gridAdd);
        addPopup.setScene(dialogScene);  //  added the scene to the stage
        addPopup.show();  //  display the stage on the screen
    }

    //  END OF ADD





    //  VIEW OR UPDATE BUTTON

    public void updateButton() {

        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            //  Here is a new Stage for the update pop-up dialog box
            Stage updatePopup = new Stage();
            updatePopup.initModality(Modality.APPLICATION_MODAL);
            updatePopup.setTitle(".   Update Product");


            //  LAYOUT AND UI CONTROLS, using a GridPane

            GridPane gridUpdate = new GridPane();
            gridUpdate.setHgap(10);   //  Horizontal spacing between columns
            gridUpdate.setVgap(10);   //  Vertical spacing between rows
            gridUpdate.setPadding(new Insets(20));   //  Padding around the entire grid

            //  ColumnConstraints control the column widths
            ColumnConstraints col1 = new ColumnConstraints(70);
            ColumnConstraints col2 = new ColumnConstraints(150);

            //  The constraints have to be added to the GridPane
            gridUpdate.getColumnConstraints().addAll(col1, col2);

            //  LABELS AND TEXT FIELDS for sku, category, name, description, quantity, and price

            //  We populate the popup Edit dialog box, by adding the nodes to the GridPane at specific row and column indices
            //     according to the Syntax: gridUpdate.add(node, col, row);

            //  Note: A more detailed grid pane organization is
            //  GridPane.add(Node child, int columnIndex, int rowIndex, int colspan, int rowspan)

            //  SKU
            TextField skuField = new TextField(selectedProduct.getSku());
            gridUpdate.add(new Label("Sku:"), 0, 1);
            gridUpdate.add(skuField, 1, 1);



            //  CATEGORY
            //  We add a list of the predefined categories for our dropdown menu
            ObservableList<String> categories = FXCollections.observableArrayList(
                    "Electronics", "Appliances", "Furniture", "Paintings", "Books", "etc.");

            ComboBox<String> categoryCombo = new ComboBox<>(categories);
            categoryCombo.setValue(selectedProduct.getCategory()); // Set a default value if desired

            // Set the cell factory to a ComboBoxTableCell
            //categoryCombo.setCellFactory(ComboBoxTableCell.forTableColumn(categories));
            gridUpdate.add(new Label("Category:"), 0, 0);
            gridUpdate.add(categoryCombo, 1, 0);




            //  NAME
            TextField nameField = new TextField(selectedProduct.getName());
            gridUpdate.add(new Label("Name:"), 0, 2);
            gridUpdate.add(nameField, 1, 2);

            //  DESCRIPTION
            TextArea descriptionField = new TextArea(selectedProduct.getDescription());
            descriptionField.setPrefRowCount(3);
            descriptionField.setWrapText(true);
            gridUpdate.add(new Label("Description:"), 0, 3);
            gridUpdate.add(descriptionField, 1, 3);

            //  QUANTITY
            TextField quantityField = new TextField(String.valueOf(selectedProduct.getQuantity()));  //  double -> string
            gridUpdate.add(new Label("Quantity:"), 0, 4);
            gridUpdate.add(quantityField, 1, 4);

            //  PRICE
            TextField priceField = new TextField(String.valueOf(selectedProduct.getPrice()));  //  double -> string
            gridUpdate.add(new Label("Price:"), 0, 5);
            gridUpdate.add(priceField, 1, 5);




            //  UPDATE DB (LOGIC)

            Button saveButton = new Button("Save Changes");

            //  With a lambda expression we set up an event listener for the "save" button. ("On Click" do this...)
            saveButton.setOnAction(e -> {

                String originalSku = selectedProduct.getSku();  // keep the old key

                //  Update the "product" object with the "new data" from each of the "text fields"
                selectedProduct.setSku(skuField.getText());  //  get the text -> set the Sku
                selectedProduct.setCategory(categoryCombo.getValue());  //  get the value -> set the Category
                selectedProduct.setName(nameField.getText());
                selectedProduct.setDescription(descriptionField.getText());
                selectedProduct.setQuantity(Integer.parseInt(quantityField.getText()));
                selectedProduct.setPrice(Double.parseDouble(priceField.getText()));

                //  We call a method in the DatabaseManager class (from the constructor) to handle the update operation
                //dbManager.updateProduct(selectedProduct);
                dbManager.updateProduct(selectedProduct, originalSku);

                //  We then call this method to refresh the "table view" in the main application window (GUI)
                refreshTable();  //  line 69

                //  Finally, we close the pop-up window (the dialog box)
                updatePopup.close();
            });




            //  CLOSE/EXIT BUTTON
            Button closeButton = new Button("Close");

            //  Best way to exit this popup
            closeButton.setOnAction(e -> {
                updatePopup.close();  // The addPopup is the Stage object for the popup window
            });


            //  GRID PLACEMENT

            //  An HBox will contain both buttons: the button box
            HBox buttonBox = new HBox(15);  //  the spacing between the buttons
            buttonBox.setAlignment(Pos.BASELINE_RIGHT);  //  We align the buttons to the right
            buttonBox.getChildren().addAll(saveButton, closeButton);  //  We add the buttons to the HBox

            gridUpdate.add(buttonBox, 1, 6);


            //  The SCENE and the DIALOG BOX (window)
            Scene dialogScene = new Scene(gridUpdate);
            updatePopup.setScene(dialogScene);
            updatePopup.show();

        } else {
            new Alert(Alert.AlertType.WARNING, "Please select (click on) a product to update it.").showAndWait();
        }
    }
    //  END OF UPDATE






    /**  Added LOAD DATA FROM A CSV:
     *   This custom "loader" method is like a helper robot. This loader goes out to the system and finds
     *     a special list of new products that are written on a CSV file.
     *   The loader reads the file, one product line at a time, and puts each of them in the database.
     *   It makes sure each new product has an Id, a category, a name, a description, some quantity and a price.
     *   If the CSV list has a mistake, the loader skips that product and keeps going, as long as the file is not null.
     *   It will also log the quantity loaded and the skipped lines.
     *   When the loader is all done, it'll add the products to the DB: "dbManager.addProduct(newProduct);"
     **/
    public void loadDataFromCSV() {
        //  This FileChooser provides us with a dialog box for the user to select a file.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        // Set the initial directory to the root of the project to make file selection easier.
        String projectPath = System.getProperty(directoryPath);
        fileChooser.setInitialDirectory(new File(projectPath));

        // Display the file chooser dialog and wait for the user's selection.
        File file = fileChooser.showOpenDialog(new Stage());

        // Proceed only if the user selected a file.
        if (file != null) {
            int loadedCount = 0;  //  This will track the number of successfully loaded items.
            int rejectCount = 0;  //  and this will track the number of rejected items/lines.

            // The try-with-resources statement ensures the BufferedReader is automatically closed.
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                bufferedReader.readLine(); // Skip the header row of the CSV file.
                String line;
                // Read each line of the file until the end.
                while ((line = bufferedReader.readLine()) != null) {

                    //  We split the line into 6 fields using a comma as the delimiter.
                    //  The limit of 6 is crucial; it ensures any extra commas within the description are ignored,
                    //     and the entire description is kept as a single field.
                    String[] field = line.split(",", 6);

                    // If the line has exactly 5 parts, it's considered valid.
                    if (field.length == 6) {
                        try {
                            // Trim whitespace from each part before using it.
                            // This prevents errors from leading or trailing spaces.
                            String category = field[0].trim();
                            String sku = field[1].trim();
                            String name = field[2].trim();
                            String description = field[3].trim();
                            int quantity = Integer.parseInt(field[4].trim());
                            double price = Double.parseDouble(field[5].trim());

                            // Create a new Product object and add it to the database.
                            Product newProduct = new Product(category, sku, name, description, quantity, price);
                            dbManager.addProduct(newProduct);

                            loadedCount++; // Increment the counter for each successful addition.
                        } catch (NumberFormatException e) {
                            // Catch NumberFormatException if quantity or price cannot be parsed.
                            System.err.println("Skipping malformed row due to number format error: " + line);

                            rejectCount++; // Increment the counter for each rejected item.
                        }
                    } else {
                        // Log rows that don't have the expected number of fields.
                        System.err.println("Skipping malformed row: " + line);
                    }
                }
                // After loading, refresh the table view on the GUI.
                refreshTable();
                // Show a confirmation dialog with the number of items loaded.
                new Alert(Alert.AlertType.INFORMATION, loadedCount +
                        " items loaded from CSV successfully, and " + rejectCount + " were rejected.").showAndWait();
            } catch (IOException e) {
                // Handle any file reading errors.
                new Alert(Alert.AlertType.ERROR, "Error reading file: " + e.getMessage()).showAndWait();
            }
        }
    }






    //  2. REMOVE

    /**
    *   Because the user can select multiple products, the removeButton() method uses an ObservableList<Product>
    *     to get a list of selected products. It then iterates through that list to remove each item.
    *   This is the logic for deleting one or more products from the table and the database.
    *   It works by first getting all the products the user has selected. Then it asks for confirmation before looping
    *      through the list to delete each selected product individually from the database.
    *   Finally, it refreshes the table to show the changes.
    **/
    public void removeButton() {
        //  1. Get a list of all products the user has selected in the TableView.
        ObservableList<Product> selectedProducts = productTable.getSelectionModel().getSelectedItems();

        //  2. Check if the user has actually selected any items (click a line or shift click multiple lines).
        if (selectedProducts != null && !selectedProducts.isEmpty()) {

            //  creates a pop-up confirmation dialog to double-check if the user really wants to delete the items.
            //  It shows the number of products selected.
            Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to remove " + selectedProducts.size() + " selected product(s)?", ButtonType.YES, ButtonType.NO);
            confirmDelete.showAndWait().ifPresent(response -> {  //  wait for the user's response
                if (response == ButtonType.YES) {  // Check if the user clicked the "Yes" button.
                    //  If the user confirmed, then we count the number of products to be removed before the loop starts
                    int removedCount = selectedProducts.size();
                    //  Then we loop through, for-each product in the selectedProducts list, and delete them.
                    for (Product product : selectedProducts) {
                        //  Send to the DatabaseManager to delete the product from the database using its unique SKU.
                        dbManager.removeProduct(product.getSku());
                    }
                    //  After deletion from the database, we refresh and remove the deleted rows from the GUI (TableView).
                    refreshTable();

                    // Show a single alert with the count of removed items
                    new Alert(Alert.AlertType.INFORMATION, removedCount + " product(s) removed successfully.").showAndWait();
                }
            });
        } else {
            new Alert(Alert.AlertType.WARNING, "Please select one or more products to remove.").showAndWait();
        }
    }




    /**
     * We remove what the user typed in the searchInput text box
    **/

    //
    public void removeBySKUBtn(TextField searchBySku) {

        //  We extract and access the text from the TextField
        String skuString = searchBySku.getText().trim();

        // Check if the search term is empty
        if (skuString.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter a SKU value to search for.").showAndWait();
            return;
        }

        // Iterate through the products to find a match
        Product productToRemove = null;

        //  We iterate for-each of the products in the table:
        for (Product product : productTable.getItems()) {  //  using getItems from the JavaFX library
            //  TODO: search by name, descr, or price:
            /*if (product.getName().toLowerCase().contains(skuString) ||
                    product.getDescription().toLowerCase().contains(skuString) ||
                    String.valueOf(product.getPrice()).contains(skuString))*/

            // Search by Sku only for now. That's a unique ID in the DB
            if (product.getSku().toLowerCase().contains(skuString)) {
                productToRemove = product;
                break;  //  We then exit this loop here, as soon as a match is found
            }
        }

        if (productToRemove != null) {
            // Found a matching product, now remove it
            dbManager.removeProduct(productToRemove.getSku());
            refreshTable();
            new Alert(Alert.AlertType.INFORMATION, "The " + skuString + " sku was removed successfully.").showAndWait();
            //  Clear the search field after a successful removal
            searchBySku.setText("");
        } else {
            new Alert(Alert.AlertType.INFORMATION, "No matching product found.").showAndWait();
        }
    }


    public void login() {

        //  Create a new Stage for the add pop-up dialog box
        Stage addPopup = new Stage();
        addPopup.initModality(Modality.APPLICATION_MODAL);
        addPopup.setTitle("Add Product");

        //  LOGIN UI
        GridPane gridAdd = new GridPane();
        gridAdd.setHgap(10);
        gridAdd.setVgap(10);
        gridAdd.setPadding(new Insets(20));


        //  USER
        TextField userField = new TextField();
        gridAdd.add(userField, 1, 2);
        gridAdd.add(new Label("Name:"), 0, 2);


        //  PASSWORD
        TextField passwordField = new TextField();
        gridAdd.add(passwordField, 1, 3);
        gridAdd.add(new Label("Description:"), 0, 3);


        //  LOGIN BUTTON
        Button saveButton = new Button("Add Product");

        //  Set up the event listener for the "save" button
        saveButton.setOnAction(e -> {
            try {
                //  Next we read data from the previous fields
                //  Note: The best way to prevent SQL injections is to use Prepared Statements on the DB mgr class
                String user = userField.getText().trim();
                String pswd = passwordField.getText().trim();

                // Validate that the SKU is not a duplicate before adding
                if (!dbManager.doesItExist(user)) {
                    new Alert(Alert.AlertType.ERROR, "User does not exist.").showAndWait();
                } else {
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}