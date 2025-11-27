package com.ims;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import static com.ims._00_Utils.*;

/**
 *   This is the "MASTER ASSEMBLER" for the main application (Product Inventory), a dedicated "builder" class.
 *   Its one and only job is to build the entire MAIN INVENTORY SCENE after a user has successfully logged in.
 *   This is a 'Controller' class because it assembles and 'wires' the Views (the Legos) to the other Controllers (the brains).

 *   This is the "how-to" guide for building the main app:
 *      1. Grab the "car chassis" (the _32_ViewFrame).
 *      2. Grab the "engine" (the _33_ViewTables).
 *      3. Grab the "computer brain" (the _24_CtrlProd).
 *      4. Grab the "requisition form" logic (_28_CtrlRequisition).
 *      5. Join and wire them all together.

 *   By moving all this complex logic out of the _04_Main class, we keep our code clean, organized, and easy to understand.
 */
public class _23_CtrlMainSceneBuilder {
    /**
     *   CONSTRUCTOR
     *   A private constructor prevents anyone from creating an instance of this class.
     *   All its methods are static, meaning we can call them directly without needing an object. It's a pure utility class.
     */
    private _23_CtrlMainSceneBuilder() {}



    /**
     *   The main public method of this class.
     *   It takes all the necessary "ingredients" and builds the main app SCENE, then shows it on the primary STAGE.
     *      @param mainApp      A reference to the _04_Main class
     *      @param primaryStage The main "stage" (window) from _04_Main.
     *      @param communicator The one-and-only "phone line."
     *      @param userName     The logged-in user's name (for the header).
     *      @param userRole     The logged-in user's role (for permissions).
     */
    public static void _23a_show(_04_Main mainApp, Stage primaryStage, _03_ClientServerLine communicator, String userName, String userRole) {

        //  STEP 1: CREATE THE "LEGO BRICKS" (VIEWS)

        //  Step 1a: Create an empty TableView.
        //     Build the main JavaFX empty table object (w TableView) for displaying products.
        //     This is just the "engine block" with no parts.
        TableView<_11_ModelProd> productTable = new TableView<>();



        //  STEP 2: CREATE THE "BRAINS" (CONTROLLERS)

        //  Step 2a: Create a controller that will handle all product-related actions.
        //     It's like the "Product Department Manager."
        //     A controller is the "brain" for a specific view. We give it the tools it needs:
        //        1. the "phone line" communicator to talk to the server.
        //        2. the "engine block" the table it needs to update.
        _24_CtrlProd prodController = new _24_CtrlProd(communicator, productTable);

        //  Step 2b: (LOW STOCK FEATURE!) Create the "Requisition Form Manager."
        //     This controller's job is to pop up the re-order form.
        //     We pass it the "phone line" so it can talk to the DB and get the supplier list.
        _28_CtrlRequisition requisitionController = new _28_CtrlRequisition(communicator);



        //  STEP 3: "WIRE UP" THE LEGO BRICKS

        //  Step 3a: Now we call the "Table Factory" (_33_ViewTables) to install all the "pistons" (columns) into our "engine" (table).
        //
        //  LOW STOCK FEATURE
        //     We pass the 'requisitionController' to the table factory.
        //     Why? So the factory can wire the "stop sign" button in the table directly to this controller!
        //     If the item has enough inventory, the "reorder" button will not show.
        _33_ViewTables._33a_productsTable(productTable, requisitionController);



        //  STEP 4: CREATE THE "REMOTE CONTROL" (ACTIONS)

        //  Create the different sets of button actions for Admins and Staff.
        //  This is a powerful pattern that lets us define user permissions cleanly.
        //  The "::" syntax is a "method reference", a shorthand for a lambda.
        //  For example, `prodController::handleAddProduct` is the same as `() -> prodController.handleAddProduct()`.

        //  Step 4a: Define the "button bundle" for Admins.
        //     We use '::' (a "method reference") as a clean shortcut for a lambda, e.g., '() -> prodController._24b_handleAddProduct()'
        _27_CtrlTableActions<_11_ModelProd> actionsAdmin = new _27_CtrlTableActions<_11_ModelProd>()
                ._27a_add(prodController::_24b_handleAddProduct)
                ._27b_remove(prodController::_24f_handleRemoveProduct)
                ._27c_update(prodController::_24e_handleUpdateProduct)
                ._27d_refresh(prodController::_24d_handleRefreshTable)
                ._27e_loadCsv(prodController::_24c_handleLoadFromCSV)

                //  ADMIN ONLY BUTTONS
                //  Now we need to wire the admin buttons to call the new scene-building methods
                ._27h_viewUsers(() -> _23b_showUserScene(mainApp, primaryStage, communicator, userName, userRole))
                ._27i_viewSuppliers(() -> _23c_showSupplierScene(mainApp, primaryStage, communicator, userName, userRole));
        //  (We would add ._26g_viewUsers() here)

        //  Step 4b: Define the "button bundle" for Staff.
        //     (Right now it's the same, but we could remove admin-only buttons here later).
        _27_CtrlTableActions<_11_ModelProd> actionsStaff = new _27_CtrlTableActions<_11_ModelProd>()
                ._27a_add(prodController::_24b_handleAddProduct)
                ._27b_remove(prodController::_24f_handleRemoveProduct)
                ._27c_update(prodController::_24e_handleUpdateProduct)
                ._27d_refresh(prodController::_24d_handleRefreshTable)
                ._27e_loadCsv(prodController::_24c_handleLoadFromCSV);


        //  Step 4c: Pick the 'correct' remote control (action bundle) based on the user's role.
        _27_CtrlTableActions<_11_ModelProd> selectedActions = ROLE_ADMIN.equalsIgnoreCase(userRole) ? actionsAdmin : actionsStaff;


        //  STEP 5: BUILD THE FINAL "CAR" (THE FRAME)

        //  Step 5a: Create the header text.
        String headerText = _04_Main.topTitle + "    >    Logged in as: " + userName + " (" + userRole + ")";

        //  Step 5b: Build the main application "car chassis" (the Frame).
        //     We pass it the 'remote control' (selectedActions) it should use, and the fully-built 'engine' (productTable)
        //         it should display, and the text for the header.
        _32_ViewFrame<_11_ModelProd> appFrame = new _32_ViewFrame<>(mainApp,selectedActions, productTable, headerText);



        //  STEP 6: PUT THE NEW "SET" ON THE "STAGE"

        //  Create the new 'Scene' (the set design), which is the "content" that goes inside the window
        //     and put our fully-built 'appFrame' into it.
        Scene mainScene = new Scene(appFrame._32a_getBorderPane(), _04_Main.wdScene, _04_Main.htScene);
        mainScene.getStylesheets().add(_23_CtrlMainSceneBuilder.class.getResource("/styles.css").toExternalForm());

        //  Step 6b: Safely update the user interface.
        //     Any time you make a major change to the UI (like swapping a whole scene), it's best to wrap it in `Platform.runLater()`.
        //     This tells JavaFX: "Please run this UI update as soon as you're ready."
        //     This 'Platform.runLater' is important.
        //     It tells JavaFX: "Please wait until you're done with your current thought, and 'then' safely swap the scene on the main UI thread."
        //     This prevents glitches.
        Platform.runLater(() -> {
            primaryStage.setTitle("Inventory Management System");
            //  This is the magic moment the screen 'changes' from the login screen to the main app!
            primaryStage.setScene(mainScene);
            primaryStage.centerOnScreen();

            //  Step 6c: Now that the scene is 'visible', we tell the product controller to make its 'first' call
            //     to the server to get all the data. This performs the initial data load now that the new scene is visible.
            prodController._24d_handleRefreshTable();
        });
    }



    /**
     *   This builds the "Manage Users" scene
     */
    public static void _23b_showUserScene(_04_Main mainApp, Stage primaryStage, _03_ClientServerLine communicator, String userName, String userRole) {

        // 1. CREATE THE "ENGINE" (The User Table)
        TableView<_13_ModelUser> userTable = new TableView<>();

        // 2. CREATE THE "BRAIN" (The User Controller)
        _25_CtrlUser userController = new _25_CtrlUser(communicator, userTable);

        // 3. CREATE THE "ADD USER" BRAIN (We re-use the Signup Controller)
        _22_CtrlSignup signupController = new _22_CtrlSignup();
        signupController._22a_setCommunicator(communicator);

        // 4. "WIRE UP" THE COLUMNS
        _33_ViewTables._33b_usersTable(userTable);

        // 5. CREATE THE "REMOTE CONTROL" for the User Scene
        _27_CtrlTableActions<_13_ModelUser> userActions = new _27_CtrlTableActions<_13_ModelUser>()
                ._27a_add(() -> signupController._22b_showSignUpDialog()) // "Add" button opens signup
                ._27b_remove(userController::_25d_handleRemoveUser)
                ._27c_update(userController::_25c_handleUpdateUser)
                ._27d_refresh(userController::_25a_handleRefreshTable)

                //  NAVIGATION BUTTONS
                //  With this new button we can now "View Products"
                ._27g_viewProducts(() -> _23a_show(mainApp, primaryStage, communicator, userName, userRole))
                // 'viewSuppliers' still goes to suppliers
                ._27i_viewSuppliers(() -> _23c_showSupplierScene(mainApp, primaryStage, communicator, userName, userRole));

        // 6. BUILD THE FINAL "CAR" (THE FRAME)
        String headerText = _04_Main.topTitle + " for ADMIN:  " + userName + "    >     MANAGE USERS";
        _32_ViewFrame<_13_ModelUser> appFrame = new _32_ViewFrame<>(mainApp, userActions, userTable, headerText);

        //  Style the page a little
        appFrame._32a_getBorderPane().setStyle("-fx-background-color: pink;");

        // 7. PUT THE NEW "SET" ON THE "STAGE"
        Scene userScene = new Scene(appFrame._32a_getBorderPane(), _04_Main.wdScene, _04_Main.htScene);
        userScene.getStylesheets().add(_23_CtrlMainSceneBuilder.class.getResource("/styles.css").toExternalForm());

        Platform.runLater(() -> {
            primaryStage.setTitle("Inventory Management - Users");
            primaryStage.setScene(userScene);
            // Load the user data
            userController._25a_handleRefreshTable();
        });
    }



    /**
     *   Builds and displays the "Manage Suppliers" scene.
     *   Follows the pattern established in _23b_showUserScene.
     */
    public static void _23c_showSupplierScene(_04_Main mainApp, Stage primaryStage, _03_ClientServerLine communicator, String userName, String userRole) {

        // 1. CREATE THE "ENGINE" (The Supplier Table)
        TableView<_15_ModelSupplier> supplierTable = new TableView<>();

        // 2. CREATE THE "BRAIN" (The Supplier Controller)
        //    Use the _26_CtrlSupplier class you created.
        _26_CtrlSupplier supplierController = new _26_CtrlSupplier(communicator, supplierTable);

        // 3. "WIRE UP" THE COLUMNS (Requires _33c_suppliersTable)
        //    We need to define how the supplier table should look.
        //    *** YOU STILL NEED TO CREATE THIS METHOD in _33_ViewTables.java ***
        _33_ViewTables._33c_suppliersTable(supplierTable); // Assuming this method exists

        // 4. CREATE THE "REMOTE CONTROL" for the Supplier Scene
        //    Use the correct generic type <_15_ModelSupplier>
        _27_CtrlTableActions<_15_ModelSupplier> supplierActions = new _27_CtrlTableActions<_15_ModelSupplier>()
                // Use the handlers from supplierController
                ._27a_add(supplierController::_26a_handleAddSupplier)       // "Add" button action
                ._27b_remove(supplierController::_26e_handleRemoveSupplier) // "Remove Selected" button action
                ._27c_update(supplierController::_26d_handleUpdateSupplier) // "View / Update" button action
                ._27d_refresh(supplierController::_26b_handleRefreshTable)  // "Refresh Data" button action

                //  NAVIGATION BUTTONS
                //  We need to re-purpose the admin buttons for navigation from the Supplier scene:
                //  Go to USERS
                ._27h_viewUsers(() -> _23b_showUserScene(mainApp, primaryStage, communicator, userName, userRole))
                //  Go to PRODUCTS
                ._27g_viewProducts(() -> _23a_show(mainApp, primaryStage, communicator, userName, userRole));

        //  5. BUILD THE FINAL "CAR" (THE FRAME)
        //    Set the correct header text for the Supplier scene.
        String headerText = _04_Main.topTitle + " for ADMIN:  " + userName + "    >     MANAGE SUPPLIERS";
        //    Use the correct generic type <_15_ModelSupplier>
        _32_ViewFrame<_15_ModelSupplier> appFrame = new _32_ViewFrame<>(mainApp, supplierActions, supplierTable, headerText);

        //  Style the page a little
        appFrame._32a_getBorderPane().setStyle("-fx-background-color: green;");

        // 6. PUT THE NEW "SET" ON THE "STAGE"
        //    Create the Scene using the supplier frame.
        Scene supplierScene = new Scene(appFrame._32a_getBorderPane(), _04_Main.wdScene, _04_Main.htScene); // Changed scene variable name
        supplierScene.getStylesheets().add(_23_CtrlMainSceneBuilder.class.getResource("/styles.css").toExternalForm());

        // 7. SHOW THE SCENE (using Platform.runLater for safety)
        Platform.runLater(() -> {
            primaryStage.setTitle("Inventory Management - Suppliers"); // Changed window title
            primaryStage.setScene(supplierScene); // Set the new supplier scene
            primaryStage.centerOnScreen(); // Recenter the window (optional)

            // Step 7a: Perform the initial data load for the supplier table.
            supplierController._26b_handleRefreshTable();
        });
    }

}