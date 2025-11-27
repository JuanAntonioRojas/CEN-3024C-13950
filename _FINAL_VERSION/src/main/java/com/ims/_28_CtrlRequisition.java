package com.ims;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//  Import our "toolbox" and "Lego brick" builders


/**
 *   This is the "Requisition Form Manager" (a Controller in MVC terms).
 *   Think of this class as a "specialized manager" or "brain" hired to handle the "Low Stock" button.
 *   However, it doesn't manage the main table, and it doesn't talk to the server
 *   It just coordinates all the pieces needed to show the "Re-Order" pop-up window.

 *   Its ONE job is to:
 *      1. Be "activated" (called) by the "stop sign" button in the product table.
 *      2. Show a pop-up window (the "Requisition Form").
 *      3. This form will be pre-filled with the product's info: name, supplier, price, etc.
 *      4. It will have a drop-down list of all suppliers from the DB, brought by the server.
 *      5. When the user clicks "Send," it will (in the future) send this requisition to the server.
 *         For now, it'll just package up all the info (Product, Supplier, Quantity) into a simple text block.
 */
public class _28_CtrlRequisition {

    // INSTANCE VARIABLES (the "tools" this manager needs)
    /**
     *   This is our "phone line" to the server, which we'll use to get the supplier list.
     *   It's 'final' because this manager gets one phone line when it's "hired" (created) and it will use that same
     *   phone line for its entire life.
     *   We will use this to call the server and ask for the supplier list.
     */
    private final _03_ClientServerLine communicator;


    /**
     *   This is a 'cache' (a temporary, in-memory storage) to hold the suppliers.
     *   Think of it as our 'Supplier Address Book'.
     *   WHY have this? Because asking the server for the entire supplier list is (or could be) a slow network operation.
     *   We don't want to do that every single time a user clicks the 're-order' button.
     *   So, the first time we need the list, we'll call the server and then 'save' the list in this variable.
     *   Every other time, we'll just use this saved list.
     *   It's 'null' at the beginning, which is how we know if we've "fetched it yet."
     *   (We use an 'ObservableList' because it's a special JavaFX list that 'plays nice' with UI controls like ComboBoxes.)
     */
    private ObservableList<_15_ModelSupplier> supplierCache = null;


    /**
     *   CONSTRUCTOR

     *   This is the "hiring" process. It's called when this manager is "hired" (created),  which usually happens when
     *      the main application starts up.
     *   The constructor, the "_23_CtrlMainSceneBuilder" class "hires" this manager and "gives" it the "phone line."

     *   To do its job, this manager needs the 'phone line' (communicator).
     *   So, the class that creates this manager (the "_23_CtrlMainSceneBuilder" class) must "give" it the communicator.
     *   This is a 'Dependency Injection'.
     *      @param communicator The one-and-only "phone line" to the server.
     */
    public _28_CtrlRequisition(_03_ClientServerLine communicator) {
        this.communicator = communicator;
    }


    /**
     *    THE MAIN PUBLIC METHOD
     *    This is the main "start button". This is the method that the "stop sign" button in the table will call  to initiate the whole process.
     *       @param product The specific '_11_ModelProd' object (the row) that the user clicked the button on. This is how we know *what* to re-order.
     */
    public void _28a_showRequisitionForm(_11_ModelProd product) {


        //  STEP 1: Get the data we need for the form:
        //
        //     1. We need a list of suppliers to show in the drop-down menu.
        //        We call our private helper method "_28b_getSuppliers()" to get them.
        //        This helper method is "smart" and will use the 'cache' if it can.
        ObservableList<_15_ModelSupplier> suppliers = _28b_getSuppliers();

        //  STEP 2: A "Safety Check" (Guard Clause)
        //     If we still have no suppliers (e.g., the server call failed), we can't do anything. We can't show the
        //        form (it would be useless without suppliers). So, we show an error and "return" (exit) the method early.
        if (suppliers.isEmpty()) {
            _30_GUI_Builder._30i_showError("Error", "Could not load supplier list from server. Please try again later.");
            return;
        }

        //  STEP 3: Call our "Pop-Up Window Factory" (_31_ViewAdd).
        //
        //  Here, we use our GUI_Builder tools to create and show a pop-up dialog.
        //  This "_31a_showAddDialog" method is very flexible. We have to give it some arguments:
        //      1. A title for the window.
        //      2. A "recipe" (a Lambda function) for how to "build the form".
        //      3. (We pass 'null' for the 'onSave' parameter because we're using a  more complex version that returns a value).
        //
        //  The `Optional<String>` it returns is a "safety box". This box will either contain the requisition `String`
        //     (if the user clicks "OK") or it will be empty (if the user clicks "Cancel").
        Optional<String> requisitionOptional = _31_ViewAdd._31a_showAddDialog(

                //  Argument 1: The title for the pop-up window.
                "Create Re-Order Requisition",

                //  Argument 2: The "Form Factory" recipe. It says what fields to add.
                //     This `grid -> { ... }` is a set of instructions we pass to the dialog.
                //     It says, "Hey Pop-Up Factory, please use these steps to build the form fields inside your grid."
                grid -> {
                    int row = 0;  //  To keep track of which row in the grid we're adding to.

                    //  (1) Create a read-only field for the product.
                    var productField = _30_GUI_Builder._30b_TextField(grid, "Product", row++);
                    productField.setText(product.getProdName() + " (SKU: " + product.getSku() + ")");
                    productField.setEditable(false);  //  User can't change the product.

                    //  (2) Create a drop-down (ComboBox) for the suppliers.
                    var supplierCombo = new ComboBox<>(suppliers);

                    //  CRITICAL STEP
                    //  TODO: need to change the way suppliers are displayed, it shows a memory address.
                    //  A "_15_ModelSupplier" is a complex Java object.
                    //  By default, a ComboBox doesn't know "what" to display. It would just show a weird memory address
                    //  like: "_15_ModelSupplier@1a2b3c4d"
                    //
                    //  We have to "teach" the ComboBox how to display the object.
                    //  We do this by giving it a 'Converter', which we get from our private helper method `_28c_getSupplierConverter()`.
                    //  This converter tells it: "Please show the supplier's company name.
                    //
                    //  TODO: DONE: This 'setConverter' is the magic. It now shows the company name.
                    supplierCombo.setConverter(_28c_getSupplierConverter());

                    //  Set a default selection (e.g., the first supplier in the list)
                    if (!suppliers.isEmpty()) {
                        supplierCombo.setValue(suppliers.get(0));
                    }
                    //  Add the label and the ComboBox to the grid.
                    _30_GUI_Builder._30a_addNodeToGrid(grid, new String("Supplier"), supplierCombo, row++);


                    //  (3)  Add a field for QUANTITY and a NOTES box.
                    var qtyField = _30_GUI_Builder._30d_NumberField(grid, "Quantity", row++);
                    qtyField.setText("50"); //  Default to ordering 50

                    //  (4)  Add a field 4 "Notes"
                    var notesArea = new TextArea();
                    notesArea.setPromptText("Optional notes (e.g., 'Rush order', 'Terms: 30/20', etc.)...");
                    notesArea.setPrefRowCount(3);
                    _30_GUI_Builder._30a_addNodeToGrid(grid, new String("Notes"), notesArea, row++);


                    //  Return the "What to do on Save" Recipe (another Lambda!) (Supplier).
                    //  This recipe will return a 'String' (the full requisition).
                    //  This `return () -> { ... }` is the second recipe we provide.
                    //  This code defines what happens when the user clicks the "OK" button.
                    //  The pop-up factory will run this code.
                    return () -> {
                        //  1. Get the values from the form fields
                        _15_ModelSupplier selectedSupplier = supplierCombo.getValue();
                        String quantity = qtyField.getText();

                        //  VALIDATION
                        //  We "must" check the user's input before continuing.
                        if (selectedSupplier == null) {
                            //  This 'throw' will be caught by the pop-up factory, which will show the message as an error
                            //     to the user and keep the pop-up open, so they can fix it.
                            throw new IllegalArgumentException("You must select a supplier.");
                        }

                        //  (5) Build a simple string as our "requisition."
                        //  If validation passes, we build a simple string.
                        //  This string is the "thing" we want to send to the server.
                        //  This string is what will be put inside the 'Optional' box.
                        return "REQUISITION\n" +
                                "TO: " + selectedSupplier.getCompanyName() + "\n" +
                                "PRODUCT: " + productField.getText() + "\n" +
                                "QUANTITY: " + qtyField.getText() + "\n" +
                                "NOTES: " + notesArea.getText();
                    };
                },
                null //  'onSave' is null because our recipe above returns the value.
        );

        //  STEP 4: Handle the Pop-Up's Result.
        //
        //  This code runs after the pop-up window has closed.
        //  The "requisitionOptional.ifPresent(...)" line says either:
        //     1. "IF the 'safety box' is not empty (i.e., the user clicked 'OK' and validation passed), then run the following code."
        //     2. If the user clicked "Cancel," this code block is just skipped.
        requisitionOptional.ifPresent(requisitionText -> {
            //  STEP 4a: Right now, we just show the requisition we built.
            //     In the future, we would send this 'requisitionText' to the server.
            //     This confirms that the operation "worked."
            //OLD WAY: _30_GUI_Builder._30h_showAlert("Requisition Created", requisitionText);//  No COPY button
            //  NEW WAY: with Copy button
            _30_GUI_Builder.showInfoCopyable("Requisition Created", requisitionText);



            //  TODO: (Future Enhancement)
            //  This is where we would actually send the data to the server.
            //  It would look something like this:
            //
            //  String serverResponse = communicator._03r_sendRequisition(requisitionText);
            //  if (serverResponse.startsWith("SUCCESS")) {
            //      _30h_showAlert("Success", "Requisition sent to server!");
            //  } else {
            //      _30i_showError("Server Error", serverResponse);
            //  }
        });
    }



    /**
     *   PRIVATE HELPER METHODS (the "internal" work)
     *   A private "smart" getter for the supplier list.
     *   Its job is to get the list of all suppliers for the drop-down menu.

     *   It's "smart" because of "cache" (an in-memory variable) so it calls the server only once.
     *   It follows this logic:
     *      1. "Have I already fetched this list before?" (Is `supplierCache` NOT null?)
     *      2. If YES: "Great! Just return the saved list. Don't call the server."
     *      3. If NO:  "This is the first time. I must call the server, parse the response, save the list in the cache, and then return it."

     *      @return The list of suppliers (either from the cache or a fresh server call).
     */
    private ObservableList<_15_ModelSupplier> _28b_getSuppliers() {
        //  Step 1: Check if our 'cache' is already full.
        if (this.supplierCache != null) {
            //  If it is, just return the cached list. No server call!
            return this.supplierCache;
        }

        //  Step 2: If the cache is 'null' (empty), this must be the first time. Call the server.
        _00_Utils.println("First-time call: Fetching supplier list from server...");
        String serverResponse = communicator._03o_getAllSuppliers();
        String[] parts = serverResponse.split("\\|", 2);

        //  Step 3: Check the response.
        if ("SUCCESS".equals(parts[0])) {
            String data = (parts.length > 1) ? parts[1] : "";

            //  Step 4: Parse the supplier data. (This is a quick, local parser)
            List<_15_ModelSupplier> supplierList = new ArrayList<>();
            if (!data.isEmpty()) {
                for (String sString : data.split(";")) {
                    // Expected: id|company|contactName|phone|email|address|notes
                    String[] fields = sString.split("\\|", 7);

                    if (fields.length == 7) {
                        _15_ModelSupplier supplier = new _15_ModelSupplier(
                                fields[0], // id
                                fields[1], // company
                                fields[2], // contact name
                                fields[3], // phone
                                fields[4], // email
                                fields[5], // address
                                fields[6]  // notes
                        );
                        supplierList.add(supplier);
                    }
                }

            }

            //  Step 5: Save the list in our 'cache' and return it.
            this.supplierCache = FXCollections.observableArrayList(supplierList);
            return this.supplierCache;

        } else {
            //  If the server call failed, return an empty list.
            return FXCollections.observableArrayList();
        }
    }

    /**
     *      A private helper that "teaches" a 'ComboBox' how to display a '_15_ModelSupplier' object, in a human-readable way.

     *    THE PROBLEM: A `ComboBox<_15_ModelSupplier>` holds entire `_15_ModelSupplier` objects.
     *    It doesn't know what part of the object to show the user.
     *    By default, it shows the object's memory address (e.g., "com.ims._15_ModelSupplier@1a2b3c4d"), which is useless.

     *    THE SOLUTION: We give the `ComboBox` a `StringConverter`. This is a tiny "translator" object with two methods.
     *       @return A 'StringConverter' object.
     */
    private javafx.util.StringConverter<_15_ModelSupplier> _28c_getSupplierConverter() {
        return new javafx.util.StringConverter<_15_ModelSupplier>() {/**
         *    This is the main translation method.
         *    JavaFX asks: "Given this `_15_ModelSupplier` OBJECT, what STRING should I display in the drop-down list?"
         *       @param supplier The `_15_ModelSupplier` object.
         *       @return The String to display.
         */
            @Override
            public String toString(_15_ModelSupplier supplier) {
                if (supplier == null) {//  If the object is null, show nothing.
                    return null;
                } else {
                    //  We answer: Tell the ComboBox "Show the supplier's company name."
                    return supplier.getCompanyName();
                }
            }

            @Override
            public _15_ModelSupplier fromString(String s) {
                //  This part isn't needed since the user can't type a new supplier, only select one.
                return null;
            }
        };
    }
}