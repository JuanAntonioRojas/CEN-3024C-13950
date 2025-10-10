package com.example.ims;


import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
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
    double WD_SKU=WD_TBL*0.07,  WD_CAT=WD_TBL*0.10,  WD_NAME=WD_TBL*0.20,  WD_DESCR=WD_TBL*0.45;
    double WD_QTTY=WD_TBL*0.05,  WD_PRICE=WD_TBL*0.08,  WD_TOTCOST=WD_TBL*0.10;

    int WD_VBOX = 150, WD_BTN = 130;






    //  UI COMPONENTS

    //  LAYOUT

    //VBox layout = new VBox(10);  //  Me no likey: this is only 1 pane
    //  We're interested in a normal web window: Header, Nav (left), Content (center) and/or Nav (right)
    //  In order to achieve this we need the BorderPane class (that has 3 components)
    private BorderPane borderPane;  //  private means that borderPane can only be accessed from within this Frame class
    private TableView<Product> productTable;  //  Generic of type "product"
    private TextField searchBySku;  // This is a null variable until you initialize it

    //  UI elements to be wired up to the Actions class (to do something)
    private Button addButton, removeButton, updateButton, refreshButton, removeBySKUBtn, loadButton, loginBtn;



    /**
     *   This constructor has 2 parameters: an Actions and a TableView object. They're part of this class's public interface: its API.
     *   By passing both arguments we ensure that both classes are working on the exact same product:
     *
     *   1. The parameter object "actions" is like using a remote control to the TV. It "does" the things you want it to do.
     *      The Frame window doesn't need to know how the remote works internally; it just needs to know which button
     *         on the remote control calls what function (method) in the Actions class.
     *      This is a good analogy for "encapsulation" (hiding internal state information) in Object-Oriented Programming.
     *
     *   2. The "productTable" object of generic type "Product" will fill up the table with each product's information.
     *      For every row in the table the Product object will get the value of its property in each column.
     *      With the "productTable" object it sets up the "CellValueFactory()" for each of the table's columns,
     *          linking the visual table to the data fields within the Product class
     *
     *   Note: In the "styles.css" we attach a CSS class to every frame component, so that it can modify it (for a better UX).
    **/

    public Frame(Actions actions, TableView<Product> productTable) {

        //  0. The reference to the TableView creates the visual table and its columns
        //  The Actions class gets data from the database and tells the productTable to display it.
        this.productTable = productTable;



        //  1. WINDOW LAYOUT

        borderPane = new BorderPane();  //  BorderPane gives us the 3 panes we need: top Title, left Nav and center Table
        borderPane.setPadding(new Insets(20));  //  a little inside space from the border itself
        borderPane.getStyleClass().add("layout");  //  The proper CSS call for UI alterations




        //  2. HEADER

        HBox header = new HBox();
        header.getStyleClass().add("header");  //  This will take any UI style we give it
        borderPane.setTop(header);


        //  2.1. TITLE:
        Label title = new Label(topTitle);
        title.getStyleClass().add("title");
        header.getChildren().add(title);

        //  2.2. SPACER
        //  this will automatically expand and contract to fill all the empty space available,
        //  pushing the login button as far to the right as possible
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().add(spacer);

        //  2.3. LOGIN BUTTON
        loginBtn = new Button("Login");
        loginBtn.setMaxWidth(WD_BTN);
        header.getChildren().add(loginBtn);





        //  3. LEFT NAVIGATION

        VBox leftVertNav = new VBox(10);
        leftVertNav.setPrefWidth(WD_VBOX);   //  Sets the width of the vertical box (VBox) to 200 pixels
        leftVertNav.getStyleClass().add("leftNav");

        //  The common buttons. already defined but not initialized:
        addButton = new Button("Add a Product");
        removeButton = new Button("Remove Selected");
        updateButton = new Button("View/Update Prod.");

        //  The search and destroy area (button and text):
        Separator separator1 = new Separator();  //  to separate from the other buttons
        separator1.getStyleClass().add("separator");

        //  Remove an item based on the SKU number
        removeBySKUBtn = new Button("Remove by Sku");   //  The search btn

        //  A Tooltip instance will help the user.
        Tooltip tooltip1 = new Tooltip("Type in the SKU number, then\nclick \"Remove by Sku\"");


        //  The user also needs a text field input for the "Search & Remove" button.
        searchBySku = new TextField(); //  We need to initialize the text field
        searchBySku.setPromptText("Enter a value to remove");  //  popup for search
        searchBySku.setTooltip(tooltip1);  //  Added a Tooltip to this button




        //  Now we can Load into the DB a CSV file:
        Separator separator2 = new Separator();  //  to separate from the search box. cant add same object twice.
        separator2.getStyleClass().add("separator");
        loadButton = new Button("Load from CSV");



        //  In case we need to refresh and view the table from the DB:
        Separator separator3 = new Separator();
        separator2.getStyleClass().add("separator");
        refreshButton = new Button("Refresh Data");


        //  I want to set the buttons' WIDTH for a consistent look
        addButton.setMaxWidth(WD_BTN);
        removeButton.setMaxWidth(WD_BTN);
        updateButton.setMaxWidth(WD_BTN);
        removeBySKUBtn.setMaxWidth(WD_BTN);
        removeBySKUBtn.setTooltip(tooltip1);  //  Added a Tooltip to this button
        loadButton.setMaxWidth(WD_BTN);
        refreshButton.setMaxWidth(WD_BTN);



        //  The buttons (and separators) that we will see are:
        leftVertNav.getChildren().addAll(
                addButton,
                removeButton,
                updateButton,
                separator1,
                removeBySKUBtn,
                searchBySku,
                separator2,
                loadButton,
                separator3,
                refreshButton
        );

        //  Put everything inside the vertical Box:
        borderPane.setLeft(leftVertNav);

        //  END OF LEFT NAV





        //  4. TABLE CONTENT

        //  Tool Tip for column headers
        Tooltip sortTooltip = new Tooltip("Click to sort asc. or desc.");


        //  1. We place the columns in the table (TableColumn) and give it a name and header
        //  2. The "setCellValueFactory" ties the column to the Product class (and to the DB).
        //  3. The col widths are preset, as indicated above.
        //  4. The colHeaderToolTip inserts a Tool Tip on hover.

        TableColumn<Product, String> skuCol = new TableColumn<>("SKU #");  //  Automatic col Header
        skuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        skuCol.setPrefWidth(WD_SKU);
        colHeaderToolTip(skuCol, sortTooltip);

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(WD_CAT);
        colHeaderToolTip(categoryCol, sortTooltip);

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(WD_NAME);
        colHeaderToolTip(nameCol, sortTooltip);

        TableColumn<Product, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(WD_DESCR);
        colHeaderToolTip(descriptionCol, sortTooltip);

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Qty");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(WD_QTTY);
        colHeaderToolTip(quantityCol, sortTooltip);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(WD_PRICE);
        colHeaderToolTip(priceCol, sortTooltip);



        //  CALCULATED EXPRESSION:

        TableColumn<Product, Double> totalCol = new TableColumn<>("Tot. Cost");
        totalCol.setPrefWidth(WD_TOTCOST);
        colHeaderToolTip(totalCol, sortTooltip);

        //  This Lambda takes a CellDataFeatures object as input.
        totalCol.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();  //  a hypothetical product
            //  Now we multiply the quantity and price from the Product object, and assign to totCost
            double totCost = product.getQuantity() * product.getPrice();

            //  The TableView requires a special type of observable property to display the value.
            //  This line creates a SimpleDoubleProperty to hold the calculated total.
            return new SimpleDoubleProperty(totCost).asObject();
        });


        //  FORMAT PRICE AND TOTAL:

        //  this css methods align the columns
        quantityCol.getStyleClass().add("align-center");
        priceCol.getStyleClass().add("align-right");
        totalCol.getStyleClass().add("align-right");


        //  These 2 custom cell-factory methods format the doubles with commas and 2 decimals
        //  Price:
        priceCol.setCellFactory(pc -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setText(null); }  //  Here we gracefully handle empty or null cells
                else { setText(String.format("%,.2f", value)); }
            }
        });
        //  Total Cost:
        totalCol.setCellFactory(tc -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setText(null); }
                else { setText(String.format("%,.2f", value)); }
            }
        });



        //  Finally, by calling the "productTable" and "actions" instances, from passing both Frame and Actions above,
        //    we ensure that both classes are working on the exact same object.

        this.productTable.getColumns().addAll(categoryCol, skuCol, nameCol, descriptionCol, quantityCol, priceCol, totalCol);
        borderPane.setCenter(this.productTable);  //  fill up the "center" space in the borderPane frame.




        //  5. EVENT HANDLERS

        //  The buttons now get 'wired to' and call the methods that the 'actions' object has (what we passed to the Frame constructor)
        addButton.setOnAction(e -> actions.addButton());
        removeButton.setOnAction(e -> actions.removeButton());
        updateButton.setOnAction(e -> actions.updateButton());
        refreshButton.setOnAction(e -> actions.refreshTable());
        removeBySKUBtn.setOnAction(e -> actions.removeBySKUBtn(searchBySku));  //  take the text and send it to Actions
        loadButton.setOnAction(e -> actions.loadDataFromCSV());

        loginBtn.setOnAction(e -> actions.login());
    }


    //  TOOLTIP:

    /**
     *  This method takes the column and a tooltip text as arguments and applies the tooltip.
     *  We use 2 generic parameters: <Product, ?>  (this part is a wildcard generic type)
     *  1) Product: This specifies that the column must belong to a TableView that holds Product objects.
     *  2) ? (The Wildcard): This means the method doesn't care about the data type of the column itself (String, Double, etc.)
     *
     *  Note: The wildcard <?> means "any type," which tells the compiler to give up all type information.
     *        We can use it when we're just passing a table around without needing to know what's in it.
     *        However, when we need to access properties of the objects inside the table, the compiler needs to know
     *        if the description property actually exists.
     *        With <?>, it has no way to verify this, leading to compilation errors or runtime failures.
     *        It's like having a box that can hold "anything." You can move the box around, but we can't reach inside
     *        and grab a screwdriver if we don't know for sure that it's a toolbox, bc the wildcard prevents that.
     **/
    private void colHeaderToolTip(TableColumn<Product, ?> column, Tooltip tooltip) {
        //  Now we take the TableColumn's column argument, get the column's header and assign it to a label
        Label headerLabel = new Label(column.getText());
        //  that "label" can receive the tooltip
        headerLabel.setTooltip(tooltip);
        //  and it will now be displayed in place of or as the graphic for the (column's) header.
        column.setGraphic(headerLabel);
        //  We finally remove the original header text from the column. else we see it twice.
        column.setText(null);
    }



    //  This getter method allows the InventoryApp class to access and retrieve the completed UI layout that the Frame
    //     class created and set it as the scene's root. This way we keep the class's internal data private
    //     and expose it only through public getter method.
    public BorderPane getBorderPane() {
        return borderPane;
    }

}