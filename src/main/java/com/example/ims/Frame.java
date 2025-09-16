package com.example.ims;


import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import static com.example.ims.InventoryApp.*;


/**
 *   This class builds the visual interface (the screen layout: header, left buttons, center table).
 *   However, it doesn't know anything about the database or how to handle user actions (clicks, key-downs, etc.).
 *   For the buttons in the Frame to work, they need to be linked to the methods in the Actions class. That way
 *      each UI click here connects to a method (Action). Think of it like LEGO: I create blocks (buttons, table)
 *      and snap them into a window (BorderPane).
 *   Now, the Frame class can't create its own Actions object because that object needs access to the DatabaseManager and
 *      TableView, which are managed by the main InventoryApp class.
 */



public class Frame {

    //  CONSTANTS
    double WD_TBL = wdScene * 0.77;  //  the table width

    //  These are the column widths: As a % of the table width
    double WD_SKU=WD_TBL*0.10, WD_NAME=WD_TBL*0.22, WD_DESCR=WD_TBL*0.52, WD_QTTY=WD_TBL*0.06, WD_PRICE=WD_TBL*0.15;

    int WD_VBOX = 150, WD_BTN = 130;



    //  UI COMPONENTS

    //  LAYOUT

    //VBox layout = new VBox(10);  //  Me no likey: this is only 1 pane
    //  We're interested in a normal web window: Header, Nav (left), Content (center) and/or Nav (right)
    //  In order to achieve this we need the BorderPane class (that has 3 components)
    private BorderPane borderPane;  //  private means that borderPane can only be accessed from within this Frame class
    private TableView<Product> productTable;
    private TextField searchInput;  // This is a null variable until you initialize it

    //  UI elements to be wired up
    private Button addButton, removeButton, updateButton, refreshButton, removeBySearchButton, loadButton;



    /**  This constructor receives 2 injected parameters (the Actions and TableView objects) from the main application.
     *   By passing both we ensure that both classes are working on the exact same object.
     *   Calling the Actions object to work is like using a remote control to the TV, or any other tool for that matter.
     *   The Frame doesn't need to know how the remote works internally; it just needs to know which button on the
     *      remote control called which function (method) in the Actions class.
     *   Note: we attach a CSS class to every frame component, so that "styles.css" can modify it (for a better UX).
    **/

    public Frame(Actions actions, TableView<Product> productTable) {

        //  0. The reference to the TableView creates the visual table and its columns
        //  The Actions class gets data from the database and tells the productTable to display it.
        this.productTable = productTable;



        //  1. LAYOUT
        borderPane = new BorderPane();  //  BorderPane gives us the 3 panes we need: top Title, left Nav and center Table
        borderPane.setPadding(new Insets(20));  //  a little inside space from the border itself
        borderPane.getStyleClass().add("layout");  //  The proper CSS call for UI alterations



        //  2. HEADER
        HBox header = new HBox();
        header.getStyleClass().add("header");  //  This will take any UI style we give it
        borderPane.setTop(header);


        //  2.1. TITLE:
        Label title = new Label("Inventory Management System");
        title.getStyleClass().add("title");
        header.getChildren().add(title);



        //  3. LEFT NAVIGATION
        VBox leftVertNav = new VBox(10);
        leftVertNav.setPrefWidth(WD_VBOX);   //  Sets the width of the vertical box (VBox) to 200 pixels
        leftVertNav.getStyleClass().add("leftNav");

        //  The common buttons. already defined but not initialized:
        addButton = new Button("Add Product");
        removeButton = new Button("Remove Selected");
        updateButton = new Button("Update Product");
        refreshButton = new Button("Refresh Data");

        //  The search and destroy area (button and text):
        Separator separator1 = new Separator();  //  to separate from the other buttons
        separator1.getStyleClass().add("separator");

        removeBySearchButton = new Button("Remove by Search");   //  The search btn

        //  The user also needs a text field input for the "Search & Remove" button.
        searchInput = new TextField(); //  We need to initialize the text field
        searchInput.setPromptText("Enter a value to remove");  //  popup for search




        //  Now we can Load into the DB a CSV file:
        Separator separator2 = new Separator();  //  to separate from the search box. cant add same object twice.
        separator2.getStyleClass().add("separator");
        loadButton = new Button("Load from CSV");



        // Set button WIDTHS for consistent look
        addButton.setMaxWidth(WD_BTN);
        removeButton.setMaxWidth(WD_BTN);
        updateButton.setMaxWidth(WD_BTN);
        refreshButton.setMaxWidth(WD_BTN);
        removeBySearchButton.setMaxWidth(WD_BTN);
        loadButton.setMaxWidth(WD_BTN);



        //  The buttons (and separator) that we will see are:
        leftVertNav.getChildren().addAll(
                addButton,
                removeButton,
                updateButton,
                refreshButton,
                separator1,
                removeBySearchButton,
                searchInput,
                separator2,
                loadButton
        );

        //  Put everything inside the vertical Box:
        borderPane.setLeft(leftVertNav);



        //  4. TABLE
        TableColumn<Product, String> skuCol = new TableColumn<>("SKU #");
        skuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        skuCol.setPrefWidth(WD_SKU); // Set the width to 10% as indicated above

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(WD_NAME);

        TableColumn<Product, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(WD_DESCR);

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Qtty");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(WD_QTTY);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(WD_PRICE);



        //  By creating the "productTable" and "actions" instances, from passing both Frame and Actions above,
        //    we ensure that both classes are working on the exact same object.

        this.productTable.getColumns().addAll(skuCol, nameCol, descriptionCol, quantityCol, priceCol);
        borderPane.setCenter(this.productTable);  //  fill up the "center" space in the borderPane frame.




        //  5. EVENT HANDLERS

        //  The buttons now get 'wired to' and call the methods that the 'actions' object has (what we passed to the Frame constructor)
        addButton.setOnAction(e -> actions.addButton());
        removeButton.setOnAction(e -> actions.removeButton());
        updateButton.setOnAction(e -> actions.updateButton());
        refreshButton.setOnAction(e -> actions.refreshTable());
        removeBySearchButton.setOnAction(e -> actions.removeBySearchButton(searchInput.getText()));
        loadButton.setOnAction(e -> actions.loadDataFromCSV());
    }


    //  This getter method allows the InventoryApp class to access and retrieve the completed UI layout that the Frame
    //     class created and set it as the scene's root. This way we keep the class's internal data private
    //     and expose it only through public getter method.
    public BorderPane getBorderPane() {
        return borderPane;
    }


    public TextField getSearchInput() {
        return searchInput;
    }
}