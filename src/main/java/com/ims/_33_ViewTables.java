package com.ims;



import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
// Import NumberFormat for currency formatting
import java.text.NumberFormat;

//  Import our "Column Factory" helper


/**
 *    This class is another "Lego Factory."
 *    Its job is to build the entire 'TableView' layouts (the "engine") used for each data model (Products, Users, etc.).
 *    Think of it as the blueprint designer for the tables and data shown in the UI.

 *    Its main job is to define 'which' columns each table should have, how these column should get their data from the
 *       underlying model objects (like _11_ModelProd or _13_ModelUser).
 *    It also defines how each column should look (width, title, and special formatting like currency).

 *    It uses the helper class `_34_ViewColumnFactory` to create the individual columns, keeping this code cleaner and
 *       following the "Don't Repeat Yourself" (DRY) principle.

 *    REORDER FEATURE!
 *    This class now also includes the special logic for the additional "Low Status" column which displays a button
 *       icon ("🛑") only for products that are low on stock
 *    I wired (connected) this button to the `_28_CtrlRequisition` controller to display a req form.
 */

public class _33_ViewTables {
    //------------------------------------------------------------------------------------------------------------------
    //  CONSTANTS: The configuration for the Table Layout
    //------------------------------------------------------------------------------------------------------------------
    //  We defined all our column widths here, so it's easy to change them later.
    //  I defined the overall width of the tables relative to the main application window's scene width, which we get
    //     from the _04_Main class's static variable, using a percentage (0.77 = 77%) to make the table width adapt
    //     if the window size changes.

    static double TB_WD = _04_Main.wdScene * 0.75; //  Table width based on window width
/*  I think it's clearer if I just put the % in the column definition itself
    //  COLUMN WIDTHS
    //  I defined them as fractions of the total table width. This makes it easy to adjust the relative column sizes in one place.
    static double   WD_SKU   = TB_WD * 0.07, // SKU column width = 7% of table width
            WD_CAT = TB_WD * 0.10, // Category column width = 10%
            WD_NAME = TB_WD * 0.20, // Name column width = 20%
            WD_DESCR = TB_WD * 0.35, // Description column width = 35% (widest)
            WD_QTTY = TB_WD * 0.05, // Quantity column width = 5%
            WD_PRICE = TB_WD * 0.08, // Price column width = 8%
            WD_TOT = TB_WD * 0.10, // Totals column width = 10%
            WD_LOW = TB_WD * 0.02; // Status column width = 5%
*/

    //  TOOLTIP TEXT
    //  This is a standard tooltip text to be displayed when hovering over column headers that allow sorting.
    public static String sortTooltip = "Click to sort asc. or desc.";


    //------------------------------------------------------------------------------------------------------------------
    //  1. PRODUCTS TABLE BUILDER:   (_33a_)
    //------------------------------------------------------------------------------------------------------------------

    /**
     *   This method "assembles" the entire Products Table. It configures and populates the columns for the main TableView.
     *   This method takes an existing (but empty) TableView and adds all the necessary columns for displaying all the
     *      Product data (_11_ModelProd objects).
     *    It also calculates their total cost and low inventory.

     *   @param table The empty TableView<_11_ModelProd> instance passed in from the scene builder (_23_CtrlMainSceneBuilder).
     *   This is the actual UI component that will display the data.
     *   @param reqController The "requisition brain" that our new "Low Status" button needs to talk to.
     *   It's a reference to the _28_CtrlRequisition controller. This "Low Stock" button in the status column can call
     *      the method to show the reorder form.
     */
    public static void _33a_productsTable(TableView<_11_ModelProd> table, _28_CtrlRequisition reqController) {

        //  STEP 1: Use our "Column Factory" to create all the simple standard columns.
        //     We tell it the 'header title', the 'model property name', the 'width', and the 'tooltip'.
        //     These columns are directly mapped to properties in the _11_ModelProd class.
        //     Here we use our helper method from _34_ViewColumnFactory, which creates a column for displaying text (String) data.
        TableColumn<_11_ModelProd, String> idCol    = _34_ViewColumnFactory._34a_stringColumn("SKU", "sku",          TB_WD * 0.15, sortTooltip); //width = 7% of table width
        TableColumn<_11_ModelProd, String> catCol   = _34_ViewColumnFactory._34a_stringColumn("Brand", "brand", TB_WD * 0.10, sortTooltip); // += 17
        TableColumn<_11_ModelProd, String> nameCol  = _34_ViewColumnFactory._34a_stringColumn("Name", "prodName",         TB_WD * 0.23, sortTooltip); // += 40
        TableColumn<_11_ModelProd, String> descrCol = _34_ViewColumnFactory._34a_stringColumn("Description", "description", TB_WD * 0.25, sortTooltip);// += 65

        //  Our `_34b_numberColumn` column factory automatically formats 'Integer' (Qty) and 'Double' (Price).
        //  It automatically handles basic number formatting (like right-alignment for doubles).
        TableColumn<_11_ModelProd, Integer> qttyCol = _34_ViewColumnFactory._34b_numberColumn("Qty", "quantity",      TB_WD * 0.05, sortTooltip); // += 70
        TableColumn<_11_ModelProd, Double> priceCol = _34_ViewColumnFactory._34b_numberColumn("Price", "price",       TB_WD * 0.08, sortTooltip); // += 78



        //  STEP 2: Create a CALCULATED EXPRESSION: THE TOTALS COLUMN
        //     This column's values are not made directly in the model (_11_ModelProd); we calculate it: Qty * Price.
        //     TableColumn is specifically defined with Double values (the calculated total).
        TableColumn<_11_ModelProd, Double> totalCol = new TableColumn<>();
        totalCol.setPrefWidth(TB_WD * 0.10);  // += 88
        // Apply the header text ("Totals") and the standard sorting tooltip using our helper, using: owner, title, message, width.
        _34_ViewColumnFactory._34c_applyTooltip(totalCol, "Totals", sortTooltip);


        //  STEP 2a: Define HOW TO GET THE DATA: 'setCellValueFactory' is where the magic happens.
        //     This tells the column how to calculate the value for each row, through a lambda expression `cellData -> { ... }`.
        //     This lambda runs once for 'every row' to figure out what value to show.
        //     We have to use custom cell factory for formatting the calculated total (like the price column)
        totalCol.setCellValueFactory(cellData -> {  //  this set method comes from the TableColumn class (JavaFX
            //     1. cellData.getValue(): Gets the "_11_ModelProd" object for the current row.
            _11_ModelProd product = cellData.getValue();
            //     2. Perform the calculation using the product's getter methods.
            double totCost = product.getQuantity() * product.getPrice();
            //     3. Return the result wrapped in a JavaFX Property object.
            //        The imported "SimpleDoubleProperty" is suitable for Double values. ".asObject()" is needed by the factory.
            return new SimpleDoubleProperty(totCost).asObject();
        });

        //  STEP 2b: Now we need to DISPLAY THE DATA: "setCellFactory"
        //     This tells the column how to format and display the calculated Double value in the cell.
        //     Here we create a NumberFormat specifically for currency, for the default locale (e.g., US locale will use $)
        final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();

        //     The prev. defined "totalCol" uses "setCellFactory" to take a lambda that returns a new TableCell instance.
        //     This allows us to customize the cell's appearance and content.
        totalCol.setCellFactory(tabCol -> { // `tabCol` represents the TableColumn itself, lambda returns a TableCell
                    // Create a new, custom TableCell specifically for displaying Doubles.
                    return new TableCell<>() { // Using <> infers TableCell<_11_ModelProd, Double>

                        /**
                         * Overriding updateItem lets us control exactly what's shown in the cell.
                         * JavaFX calls this whenever the cell needs to be drawn or updated.
                         *
                         * @param value The Double value for this cell (calculated by setCellValueFactory).
                         * @param empty True if this cell is for an empty placeholder row.
                         */
                        @Override
                        protected void updateItem(Double value, boolean empty) {
                            //  1. Before we do anything custom, we MUST ALWAYS call the original updateItem method
                            //        from the parent TableCell class.
                            //     This handles essential housekeeping cleanuplike:
                            //        1. Clearing the cell if it's being reused for a different row.
                            //        2. Applying default styles (like background colors for rows or selection).
                            //     If we forget this, we'll get strange visual bugs (like old data appearing in wrong rows).
                            super.updateItem(value, empty);

                            //  2. Check if this cell is for an empty row OR if the data value itself is null.
                            if (empty || value == null) {  // If it's empty, make sure the cell shows nothing.
                                setText(null);  //  Remove any text content.
                                setStyle("");   //  Clear any custom alignment styles we might have set before.
                            }
                            // Handle Cells WITH Data
                            else {
                                //  3. Use the currency formatter:  Format the Number as Currency
                                //     We use the 'currencyFormatter' (which we created earlier using NumberFormat.getCurrencyInstance())
                                //        to turn the raw Double (like 99.98999) into a nicely formatted currency String based on
                                //        the computer's locale settings (e.g., "$99.99" in the US, "€99,99" in Germany).
                                setText(currencyFormatter.format(value)); // Set the cell's text to the formatted currency string.
                                //  4. Align the Text
                                //     By default, text is usually left-aligned. Currency looks better right-aligned.
                                //     We apply inline CSS using setStyle() to:
                                //     1. Align the text to the right side of the cell (`CENTER-RIGHT`).
                                //     2. Add a little padding on the right (`0 10 0 0`) so the text isn't touching the cell border.
                                setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 10 0 0;");
                            }
                        }  //  End of updateItem
                    };     //  End of new TableCell
        });                //  End of setCellFactory


        //  STEP 3:  THE "LOW STOCK" COLUMN

        //  STEP 3a: Here we need a new column that will hold a 'Button' object, to indicate that we're running out of stock
        //     But we'll use an emoji icon. The STOP sign.
        TableColumn<_11_ModelProd, Object> lowQtty = new TableColumn<>();
        //     and of course, we apply a tooltip
        _34_ViewColumnFactory._34c_applyTooltip(lowQtty, "Low", "Low Stock Status");  //  3% of the Table Width
        lowQtty.getStyleClass().add("low_qtty-col");


        //  STEP 3b:  We use a 'CellFactory' to 'manually' build the cell's contents.
        lowQtty.setCellFactory(col -> new TableCell<>() {

            //  This is the little "stop sign" button.
            private final Button button = new Button("🛑");  // Stop sign emoji

            //  We need to override the default behavior of the updateItem from Java 17, else we'll see weird visual glitches.
            @Override
            protected void updateItem(Object item, boolean empty) {
                //  But we still need to 'call' that table property to be able to update an item on a cell.
                //  We need to let the parent class do its standard housekeeping before we add our specific button logic.
                super.updateItem(item, empty);

                if (empty) {  //  If the row is empty, show nothing.
                    setGraphic(null);  //  setGraphic is from JavaFX
                } else {
                    //  If the row is 'not' empty, we need to get the product index for this specific cell, that index gives
                    //     us the product itself, matching our model product.
                    //  In other words: We get the specific _11_ModelProd object backing 'this particular row'
                    _11_ModelProd product = getTableView().getItems().get(getIndex());  //  getTableView from JavaFX
                    //  How it works:
                    //     1. getIndex(): Returns the row number (index) this cell currently represents.
                    //     2. getTableView(): Gets a reference to the entire TableView containing this cell.
                    //     3. getItems(): Gets the underlying ObservableList<_11_ModelProd> bound to the TableView.
                    //     4. .get(index): Retrieves the product object from the list at the correct row index.


                    //  STEP 3c: THE BUSINESS RULE: LOW INVENTORY.
                    //     Check the product's 'isLowStock()' method.
                    if (product._11g_isLowStock()) {
                        //  If stock is LOW:
                        //     1. Show the button in the cell!
                        setGraphic(button);

                        //  MAKE THE BUTTON RED
                        // Set inline CSS style: red text, transparent background
                        //button.setStyle("-fx-text-fill: red; -fx-background-color: transparent; -fx-font-weight: bold;");
                        //  This is better controlled using my existing CSS, in resources
                        button.getStyleClass().add("low-qtty-button"); // this CSS class name is at bottom of styles.css

                        //     2. Need to connect the button's 'click' event to call our 'requisitionController'.
                        button.setOnAction(e -> reqController._28a_showRequisitionForm(product));

                    } else {  setGraphic(null);  } //  If stock is OK, show nothing.
                }
            }
        });

        //  STEP 4: Add all.
        //     Finally, we inject all the columns into the table object, with the help of the
        //        Generics Observable List's getColumns method of JavaFX. This has "SORTING" capabilities as well.
        table.getColumns().addAll(idCol, catCol, nameCol, descrCol, qttyCol, priceCol, totalCol, lowQtty);

    }  //  End of method "PRODUCTS TABLE BUILDER"







    //------------------------------------------------------------------------------------------------------------------
    //  2. USERS TABLE BUILDER (_33b_)
    //------------------------------------------------------------------------------------------------------------------

    /**
     *    This method configures and populates the columns for assembles the User Table.
     *   Configures and populates the columns for the Users TableView.
     *      @param table The empty TableView<_13_ModelUser> instance to configure.
     */
    public static void _33b_usersTable(TableView<_13_ModelUser> table) {

        //  These standard columns use the factory mapping to properties (the getters) in "_13_ModelUser".
        TableColumn<_13_ModelUser, String> idCol    = _34_ViewColumnFactory._34a_stringColumn("ID #", "id", TB_WD * 0.05, sortTooltip);
        TableColumn<_13_ModelUser, String> roleCol  = _34_ViewColumnFactory._34a_stringColumn("Role", "role", TB_WD * 0.20, sortTooltip);
        TableColumn<_13_ModelUser, String> nameCol  = _34_ViewColumnFactory._34a_stringColumn("Name", "name", TB_WD * 0.20, sortTooltip);
        TableColumn<_13_ModelUser, String> emailCol = _34_ViewColumnFactory._34a_stringColumn("Email", "email", TB_WD * 0.25, sortTooltip);
        TableColumn<_13_ModelUser, String> phoneCol = _34_ViewColumnFactory._34a_stringColumn("Phone", "phone", TB_WD * 0.20, sortTooltip);
        //  This is a potential future column:
        //TableColumn<_13_ModelUser, Integer> attemptsCol = _34b_numberColumn("Attempts", "loginAttempts", WD_SKU, "Failed login attempts");

        // Add the columns to the user table.
        table.getColumns().addAll(idCol, roleCol, nameCol, emailCol, phoneCol);
    }






    //------------------------------------------------------------------------------------------------------------------
    //  3. SUPPLIERS TABLE BUILDER (_33c_)
    //------------------------------------------------------------------------------------------------------------------

    /**
     *    This method configures and populates the columns for assembles the Supplier's Table.
     *       @param table The empty TableView<_15_ModelSupplier> instance to configure.
     */
    public static void _33c_suppliersTable(TableView<_15_ModelSupplier> table) {

        //  These are the standard columns using the factory, mapping to properties in "_15_ModelSupplier".
        TableColumn<_15_ModelSupplier, String> idCol = _34_ViewColumnFactory._34a_stringColumn("ID", "id", TB_WD * 0.05, sortTooltip); // Maybe smaller width for ID
        TableColumn<_15_ModelSupplier, String> companyCol = _34_ViewColumnFactory._34a_stringColumn("Company", "companyName", TB_WD * 0.18, sortTooltip);
        TableColumn<_15_ModelSupplier, String> contactCol = _34_ViewColumnFactory._34a_stringColumn("Contact", "contactName", TB_WD * 0.18, sortTooltip);
        TableColumn<_15_ModelSupplier, String> phoneCol = _34_ViewColumnFactory._34a_stringColumn("Phone", "phone", TB_WD * 0.10, sortTooltip);
        TableColumn<_15_ModelSupplier, String> emailCol = _34_ViewColumnFactory._34a_stringColumn("Email", "email", TB_WD * 0.15 * 1.5, sortTooltip);
        TableColumn<_15_ModelSupplier, String> addressCol = _34_ViewColumnFactory._34a_stringColumn("Address", "address", TB_WD * 0.18, sortTooltip);
        TableColumn<_15_ModelSupplier, String> notesCol = _34_ViewColumnFactory._34a_stringColumn("Notes", "notes", TB_WD * 0.20, sortTooltip);

        //  Finally, we add the columns to the supplier table.
        table.getColumns().addAll(idCol, companyCol, contactCol, phoneCol, emailCol, addressCol, notesCol);
    }

}  //  End of class _33_ViewTables



















