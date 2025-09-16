package com.example.ims;

import javafx.collections.ObservableList;
import javafx.scene.control.*;

import javafx.stage.FileChooser;
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

    // Set the initial directory to the root of your project
    private final String directoryPath = "user.dir";

    /**
     *  With this constructor we establish the dependency injection, so that the Actions class can do its job without
     *    creating its own dependencies. Without this, the Actions class wouldn't know about the database or the table.
     *  If it creates its own DatabaseManager and TableView dependencies it would lead to separate, unmanaged instances,
     *     or be unable to perform any actions at all.
     *  We give the Actions class a single, shared instance of the "DatabaseManager", to connect to & interact with the DB.
     *  We also give it access to the specific "TableView" object that is displayed on the screen.
     *  When a user adds or removes a product, the Actions class needs a reference to this table to refresh its contents.
     *  By passing these 2 objects through the constructor, we ensure that Actions is working with the same components
     *     that were created by InventoryApp and displayed by Frame, preventing disconnected functions and resource leaks.
     **/

    public Actions(DatabaseManager dbManager, TableView<Product> productTable) {

        this.dbManager = dbManager;
        this.productTable = productTable;

        // Enable multiple selection on the table. I like selecting multiple rows, so I can delete a few at once
        productTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }




    //  REFRESH TABLE
    //  Essential utility after every action:
    public void refreshTable() {
        ObservableList<Product> data = dbManager.getAllProducts();
        productTable.setItems(data);
    }



    //  1. ADD

    //  This will create a popup mssg box for adding a single product
    public void addButton() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add a Product to the Database");
        dialog.setHeaderText("Enter product details (SKU, Name, Desc, Qty, Price)");
        dialog.setContentText("Format: 123456, Any Name, Whatever Description, 2, 9.99");

        //  TODO: create separate mssg boxes for each: ID, name, etc.

        /**
         *  "showAndWait" displays a dialog box on the screen and then pauses for the user to enters some data.
         *  It returns an Optional object, which is a container that may (or may not) hold a value (the text entered).
         *  The "ifPresent" checks if the optional contains a value, in which case the anonymous lambda is executed.
         *  The variable "input" is a (temporary) name for the string that the user typed in, that was returned by the dialog.
         *  For "hardened" security to prevent SQL injections we use a "prepared statements" in the DatabaseManager class,
         *     not by validating the input from this "dialog" object (TextInputDialog).
         *  However, this code ensures that the data being passed to the "prepared statement" is in the expected format.
        **/

        dialog.showAndWait().ifPresent(input -> {
            try {
                String[] parts = input.split(",");
                if (parts.length == 5) {  //  Checking the number of parts entered (subject to change if separate inputs)
                    String sku = parts[0].trim();
                    if (dbManager.doesProductExist(sku)) {  //  to prevent data duplication
                        new Alert(Alert.AlertType.ERROR, "SKU already exists.").showAndWait();
                    } else {
                        int qty = Integer.parseInt(parts[3].trim());
                        double price = Double.parseDouble(parts[4].trim());
                        Product newProduct = new Product(sku, parts[1].trim(), parts[2].trim(), qty, price);
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
    }


    public void setSearchInput(TextField searchInput) {
    }


    //  2. REMOVE

    /**  This is the logic for deleting one or more products from the table and the database.
    *  It works by first getting all the products the user has selected. Then it asks for confirmation before looping
    *    through the list to delete each selected product individually from the database.
    *  Finally, it refreshes the table to show the changes.
    **/
    public void removeButton() {
        //  1. Get a list of all products the user has selected in the TableView.
        ObservableList<Product> selectedProducts = productTable.getSelectionModel().getSelectedItems();

        //  2. check if the user has actually selected any items. If they haven't, it shows a warning message.
        if (selectedProducts != null && !selectedProducts.isEmpty()) {

            //  creates a pop-up confirmation dialog to double-check if the user really wants to delete the items.
            //  It shows the number of products selected.
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to remove " + selectedProducts.size() + " selected product(s)?", ButtonType.YES, ButtonType.NO);
            confirmation.showAndWait().ifPresent(response -> {  //  wait for the user's response
                if (response == ButtonType.YES) {  // Check if the user clicked the "Yes" button.
                    //  If the user confirmed, then loop through each product in the selected list.
                    for (Product product : selectedProducts) {
                        //  the DatabaseManager has to delete the product from the database using its unique SKU.
                        dbManager.removeProduct(product.getSku());
                    }
                    //  After deletion from the database, we refresh and remove the deleted rows from the GUI (TableView).
                    refreshTable();
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
    public void removeBySearchButton(String searchTerm) {

        // Check if the search term is empty
        if (searchTerm.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter a value to search for.").showAndWait();
            return;
        }

        // Iterate through the products to find a match
        Product productToRemove = null;
        for (Product product : productTable.getItems()) {
            // Search by name, description, or price
            if (product.getName().toLowerCase().contains(searchTerm) ||
                    product.getDescription().toLowerCase().contains(searchTerm) ||
                    String.valueOf(product.getPrice()).contains(searchTerm)) {

                productToRemove = product;
                break; // Stop after the first match
            }
        }

        if (productToRemove != null) {
            // Found a matching product, now remove it
            dbManager.removeProduct(productToRemove.getSku());
            refreshTable();
            new Alert(Alert.AlertType.INFORMATION, "Product removed successfully.").showAndWait();
        } else {
            new Alert(Alert.AlertType.INFORMATION, "No matching product found.").showAndWait();
        }
    }






    //  UPDATE BUTTON
    public void updateButton() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            // For now, this is a placeholder. You'd need a more robust update dialog.
            selectedProduct.setQuantity(selectedProduct.getQuantity() + 1);
            dbManager.updateProduct(selectedProduct);
            refreshTable();
        } else {
            new Alert(Alert.AlertType.WARNING, "Please select a product to update.").showAndWait();
        }
    }







    /**  Added LOAD DATA FROM A CSV:
     *   This special loadDataFromCSV() method is like a helper robot, the "loader". This loader goes out to the system
     *     and finds a special list of new products that are written on a piece of paper (the CSV file).
     *   The loader reads the paper, one product at a time, and puts each of them in the database.
     *   It makes sure each new product has an Id, a name, a color, some quantity and a price, just like all the other products.
     *   If the CSV list has a mistake, the loader skips that product and keeps going.
     *   When the robot is all done, it shows all the products in the DB, so you can see the new ones you just added!
    **/

    public void loadDataFromCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        // Set the initial directory to the root of your project
        String projectPath = System.getProperty(directoryPath);
        fileChooser.setInitialDirectory(new File(projectPath));

        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            int loadedCount = 0; // The counter variable
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                bufferedReader.readLine();   //   Skip the header row
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 5) {
                        try {
                            String sku = parts[0].trim();
                            String name = parts[1].trim();
                            String description = parts[2].trim();
                            int quantity = Integer.parseInt(parts[3].trim());
                            double price = Double.parseDouble(parts[4].trim());

                            Product newProduct = new Product(sku, name, description, quantity, price);
                            dbManager.addProduct(newProduct);
                            loadedCount++; // Increment the counter for each successful addition
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping malformed row: " + line);
                        }
                    }
                }
                refreshTable();
                new Alert(Alert.AlertType.INFORMATION, loadedCount + " items loaded from CSV successfully.").showAndWait();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Error reading file: " + e.getMessage()).showAndWait();
            }
        }
    }
}