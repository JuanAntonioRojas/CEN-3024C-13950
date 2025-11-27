package com.ims;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

//  Import our "Lego brick" factory
import java.io.IOException;


/**
 *   This class is a "Lego brick" that builds the 'main visual layout' of the application (the "car chassis").

 *   It uses a 'BorderPane' to create the classic layout:
 *   - TOP: Header (Title)
 *   - LEFT: Navigation (Buttons)
 *   - CENTER: The main data table

 *   This a "Generic" class, so it can build a frame for Products, Users, or any table of type <T>.
 *   It's a 'dumb' view. It doesn't know what the buttons do.
 *   It doesn't know anything about the DB or how to handle each user action (clicks, key-downs, etc.).
 *   It's just given a "remote control" (_27_CtrlTableActions) and it wires that remote to the buttons it builds.
 *   That way each UI click here connects to a method.
 *   Think of it like LEGO: I create blocks (buttons, table) and snap them into a window (BorderPane).
 *   Now, the Frame class can't create its own Actions object because that object needs access to a DB Manager and
 *      TableView, which are managed by the main InventoryApp class.
 */
public class _32_ViewFrame<T> {

    // INSTANCE VARIABLES

    //  This is the "remote control" that the _23_CtrlMainSceneBuilder gives us.
    private _27_CtrlTableActions<T> actions;

    //  The main 'BorderPane' (the "chassis") that holds everything.
    private BorderPane borderPane;
    private _04_Main mainApp;

    //  We keep references to the buttons, so we can wire them up.
    private Button addButton, removeButton, updateButton, refreshButton, loadButton;
    private Button viewProductsButton, viewUsersButton, viewSuppliersButton, logoutButton, exitButton;

    //  The 'TableView' (the "engine") that is passed in from the _23_CtrlMainSceneBuilder.
    private TableView<T> table;


    /**
     *   CONSTRUCTOR
     *   This is the main  for building the application's primary user interface (the "car").
     *      @param actions The "_27_CtrlTableActions" (the "remote control") that holds all the logic (as lambdas) for the buttons.
     *      @param table The pre-configured 'TableView' (the "engine") that will be displayed in the center.
     *      @param title The text to be displayed in the header.
     */
    public _32_ViewFrame(_04_Main mainApp, _27_CtrlTableActions<T> actions, TableView<T> table, String title) {
        //  Step 1: Save the "remote" and the "engine."
        this.mainApp = mainApp;
        this.actions = actions;
        this.table = table;

        // STEP 2: BUILD THE "CHASSIS" (THE LAYOUT)

        //  Create the 'BorderPane'. This is the master layout that gives us 'Top', 'Left', and 'Center'.
        borderPane = new BorderPane();
        borderPane.setPadding(new Insets(20)); //  A little space around the edges
        borderPane.getStyleClass().add("layout"); //  For CSS styling


        // STEP 3: BUILD THE "TOP" (HEADER)

        //  Step 3a: An 'HBox' (Horizontal Box) will hold the title.
        HBox header = new HBox();
        header.getStyleClass().add("header");

        //  Step 3b: Create the title 'Label'.
        Label theTitle = new Label(title);
        theTitle.getStyleClass().add("title");
        header.getChildren().add(theTitle);

        //  Step 3c: Put the 'header' into the 'Top' slot of our 'BorderPane'.
        borderPane.setTop(header);


        // STEP 4: BUILD THE "LEFT" (NAVIGATION)

        //  Step 4a: A 'VBox' (Vertical Box) will hold our stack of buttons. (10px spacing).
        VBox leftVertNav = new VBox(10);
        leftVertNav.setPrefWidth(150); //  Set a fixed width
        leftVertNav.getStyleClass().add("leftNav");

        //  Step 4b: Use our "_30_GUI_Builder" factory to create all the buttons.
        addButton       = _30_GUI_Builder._31f_Btn("Add", 130, null);
        removeButton    = _30_GUI_Builder._31f_Btn("Remove Selected", 130, null);
        updateButton    = _30_GUI_Builder._31f_Btn("View / Update", 130, null);
        loadButton      = _30_GUI_Builder._31f_Btn("Load from CSV", 130, null);
        refreshButton   = _30_GUI_Builder._31f_Btn("Refresh Data", 130, null);
        viewProductsButton = _30_GUI_Builder._31f_Btn("View Products", 130, new Tooltip("View and manage products"));
        viewUsersButton = _30_GUI_Builder._31f_Btn("View Users", 130, new Tooltip("View and manage user accounts"));
        viewSuppliersButton = _30_GUI_Builder._31f_Btn("View Suppliers", 130, new Tooltip("View and manage suppliers"));

        //  LOG-OUT BUTTON
        logoutButton = _30_GUI_Builder._31f_Btn("Logout", 65, new Tooltip("Logout from the application"));
        //  EXIT BUTTON
        exitButton = _30_GUI_Builder._31f_Btn("Exit", 65, new Tooltip("Close the application"));

        //  Custom separators.
        _30_GUI_Builder.HzLine s1 = new _30_GUI_Builder.HzLine();
        _30_GUI_Builder.HzLine s2 = new _30_GUI_Builder.HzLine();
        _30_GUI_Builder.HzLine s3 = new _30_GUI_Builder.HzLine();
        _30_GUI_Builder.HzLine s4 = new _30_GUI_Builder.HzLine();

        //  Step 4c: Add the buttons and separators to the 'VBox' in the correct order.
        leftVertNav.getChildren().addAll(
                addButton,
                removeButton,
                updateButton,
                s1,
                loadButton,
                s2,
                refreshButton,
                s3
        );

        //  Step 4d: PERMISSION CHECK 4 ADMINS
        //     This is how we handle Admin-only buttons!
        //     We check if the "remote control" has a 'onViewUsers' button programmed.
        //  If it does, we add the Admin buttons.
        //  If not (e.g., it's the 'Staff' remote), these buttons are never even created.
        if (actions.onViewProducts != null) {
            leftVertNav.getChildren().add(viewProductsButton);
        }
        if (actions.onViewUsers != null) {
            leftVertNav.getChildren().add(viewUsersButton);
        }
        if (actions.onViewSuppliers != null) {
            leftVertNav.getChildren().add(viewSuppliersButton);
        }

        //  LOGOUT and EXIT buttons in one horizontal row:
        HBox buttonRow = new HBox(10); // 10px spacing

        buttonRow.setAlignment(Pos.CENTER); //  Centering the buttons
        buttonRow.getChildren().addAll(logoutButton, exitButton);

        //  Now both buttons can go on the same row:
        leftVertNav.getChildren().addAll(s4, buttonRow); //  Separator and Exit button

        //  Step 4e: Put the 'leftVertNav' (with all its buttons) into the 'Left' slot of our 'BorderPane'.
        borderPane.setLeft(leftVertNav);

        // STEP 5: BUILD THE "CENTER" (THE TABLE)

        //  Step 5a: This is the easy part. We just take the fully-built "engine" ('table') we were given and drop it into the 'Center' slot.
        borderPane.setCenter(this.table);


        // STEP 6: "WIRE" THE BUTTONS TO THE "REMOTE"

        //  Step 6a: For each button, we check if its matching action on the "remote" exists (is not null).
        //  Step 6b: The 'setOnAction' is the way we attach a piece of code to a button click.
        //  Step 6c: 'e -> actions.onAdd.run()' is a "lambda" anonymous function.
        //     It just means: "When 'e' (the click event) happens, run the 'onAdd' code from our 'actions' remote."

        if (actions.onAdd != null)          addButton.setOnAction(          e -> actions.onAdd.run());
        if (actions.onRemove != null)       removeButton.setOnAction(       e -> actions.onRemove.run());
        if (actions.onUpdate != null)       updateButton.setOnAction(       e -> actions.onUpdate.run());
        if (actions.onLoadCsv != null)      loadButton.setOnAction(         e -> actions.onLoadCsv.run());
        if (actions.onRefresh != null)      refreshButton.setOnAction(      e -> actions.onRefresh.run());
        if (actions.onViewProducts != null) viewProductsButton.setOnAction( e -> actions.onViewProducts.run());
        if (actions.onViewUsers != null)    viewUsersButton.setOnAction(    e -> actions.onViewUsers.run());
        if (actions.onViewSuppliers != null)viewSuppliersButton.setOnAction(e -> actions.onViewSuppliers.run());

        //  THE LOG-OUT BUTTON
        // Action is always the same: Platform.exit()
        logoutButton.setOnAction(e -> {
            _00_Utils.println("Logout button clicked. Logging out from the application..."); // analysis log
            try {
                this.mainApp._04a_showLoginScreen(); //  Tells JavaFX to log out
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        //  THE EXIT BUTTON
        // Action is always the same: Platform.exit()
        exitButton.setOnAction(e -> {
            _00_Utils.println("Exit button clicked. Closing the application..."); // Optional log
            Platform.exit(); //  Tells JavaFX to shut down gracefully
        });
    }

    /**
     *   This is the "getter" for the finished "car."
     *   The _23_CtrlMainSceneBuilder calls this to get the final layout and put it in the main 'Scene'.
     *      @return The fully constructed 'BorderPane' layout.
     */
    public Parent _32a_getBorderPane() {
        return borderPane;
    }
}