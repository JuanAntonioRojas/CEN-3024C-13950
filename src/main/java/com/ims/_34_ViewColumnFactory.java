package com.ims;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *   This is the "helper for the helper" method. Without it, the ViewTable helper class would be ginormous.
 *   It's a "Lego Factory" that only builds one kind of lego piece: a single 'TableColumn'.
 *   This class is used by '_33_ViewTables' to keep the code even cleaner and more "DRY."
 *   I made it a 'generic' class (using <T>) so it can build a column for any kind of data model (Product, User, etc.).
 *   This is kind of the heart of the table building. The cornerstone. My masterpiece.
 */
public class _34_ViewColumnFactory {

    /**
     *   STRING COLUMN
     *   This method builds a 'TableColumn' for simple 'String' data(like ID, Name, Description).
     *      @param <T> The data model type (_11_ModelProd, _13_ModelUser or _15_ModelSupplier).
     *      @param colTitle The title for the column header.
     *      @param modProperty The 'name' of the 'getter' in the model (e.g., "sku" links to "getSku()", "Name" to "getProdName()").
     *      @param colWidth The preferred width of the column.
     *      @param toolTip The helper text to show on hover.
     *      @return A fully configured 'TableColumn' for 'String' data.
     */
    public static <T> TableColumn<T, String> _34a_stringColumn(String colTitle, String modProperty, double colWidth, String toolTip) {
        //  Step 1: Create the empty column.
        TableColumn<T, String> column = new TableColumn<>();

        //  Step 2: Set its width.
        column.setPrefWidth(colWidth);

        //  Step 3: Use our 'own' private helper to apply the title and tooltip.
        _34c_applyTooltip(column, colTitle, toolTip);//, colWidth);

        //  Step 4: This is the JavaFX magic. 'PropertyValueFactory' is the "wire" that automatically connects this
        //     column to the 'getter' (e.g., 'getSku()')  model, based on the 'modProperty' string.
        column.setCellValueFactory(new PropertyValueFactory<>(modProperty));

        //  Step 5: Return the finished column.
        return column;
    }


    /**
     *   NUMERICAL COLUMN
     *   This builds a 'TableColumn' for any kind of 'Number' (like 'Integer' for Quantity or 'Double' for Price).

     *   It also 'automatically' formats the cell's alignment and text!
     *      @param <T> The data model type (e.g., _11_ModelProd).
     *      @param <N> The numerical type (e.g., 'Integer' or 'Double').
     *      @param colTitle The title for the column header.
     *      @param modProperty The 'name' of the 'getter' in the model (e.g., "quantity").
     *      @param colWidth The preferred width of the column.
     *      @param toolTip The helper text to show on hover.
     *      @return A fully configured 'TableColumn' for 'Number' data.
     */
    public static <T, N extends Number> TableColumn<T, N> _34b_numberColumn(String colTitle, String modProperty, double colWidth, String toolTip) {
        //  Step 1: Create the column and set its properties.
        TableColumn<T, N> column = new TableColumn<>();
        _34c_applyTooltip(column, colTitle, toolTip);//, colWidth);
        column.setCellValueFactory(new PropertyValueFactory<>(modProperty));
        column.setPrefWidth(colWidth);

        //  Step 2: THE AUTO-FORMATTING MAGIC
        //     'setCellFactory' lets us build the cell's look and feel 'manually'.
        column.setCellFactory(tc -> new TableCell<T, N>() {
            //  This 'updateItem' method is called for 'every cell' in the column.
            @Override
            protected void updateItem(N value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    //  If the cell is empty, show nothing.
                    setText(null);
                    setStyle("");
                } else if (value instanceof Double) {
                    //  IF the value is a 'Double' (like Price)...
                    //  ...format it as "currency" (e.g., "1,234.50").
                    setText(String.format("%,.2f", value.doubleValue()));
                    //  ...and 'align' it to the 'right'.
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 10 0 0;");
                } else {
                    //  IF it's any other number (like 'Integer')...
                    //  ...just show the number as a plain string.
                    setText(value.toString());
                    //  ...and 'align' it to the 'center'.
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        return column;
    }


    /**
     *   TOOLTIP HELPER
     *   A private tool to apply a 'Label' (as a graphic) and a 'Tooltip' to a column's header.
     *   We use a 'Label' as a 'graphic' so that the 'tooltip' shows up on the text, not just the empty space in the header.
     */
    public static <T, C> void _34c_applyTooltip(TableColumn<T, C> column, String title, String toolTip) {//, double colWidth) {
        //  Step 1: Create a 'Label' to hold the title.
        Label headerLabel = new Label(title);
        //headerLabel.setPrefWidth(colWidth); //  Make the label fill the header

        //  Step 2: If the 'toolTip' isn't 'null', add it to the 'Label'.
        if (toolTip != null && !toolTip.isEmpty()) {
            headerLabel.setTooltip(new Tooltip(toolTip));
        }

        //  Step 3: Set the 'Label' (with its tooltip) as the 'graphic' for the column header.
        column.setGraphic(headerLabel);

        //  Step 4: Remove the 'original' 'text' from the column, or it will show up 'twice'!
        column.setText(null);
    }
}