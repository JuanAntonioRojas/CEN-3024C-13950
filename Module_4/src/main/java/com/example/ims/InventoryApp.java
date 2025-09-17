package com.example.ims;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;



/**  This is the main show, the public face of our program. It's where all the buttons, text boxes, and tables live.
 *  It used to be that our main function would just print a menu to the screen and wait for input, but with JavaFX,
 *     we have a proper graphical user interface (GUI). The InventoryApp class is the conductor of this orchestra.
 *  This class is responsible for setting up the stage (the main window), arranging all the components (the UI), and
 *     listening for the user to do something (like clicking a button or typing in a text box).
 *  When a user does something, this class translates that action into a command for the DatabaseManager to handle.
 *  It doesn't know how the data gets added or removed from the database; it just knows to ask the DatabaseManager to do it.
 *  This clean separation of concerns (the UI from the business logic) keeps the UI code from becoming a tangled mess.
 *  We will use a Start and Stop methods to guarantee that the setup and teardown are handled in a structured, predictable way.
**/



public class InventoryApp extends Application {

    // Window size. If I ever want to read these from a config, they are in one place.
    public static int wdScene = 1200;
    public static int htScene = 740;


    //  I'd rather 'keep the DB manager out here (not inside start()) so I can close it in stop().
    private DatabaseManager dbManager;

    public static void main(String[] args) {
        launch(args);  //  JavaFX bootstraps and eventually calls start()
    }



    //  This is the entry point for our app (it's part of the JavaFX Application lifecycle).
    //  This is where we set up the user interface, create a stage, scene, and all the UI components.
    @Override
    public void start(Stage primaryStage) {

        // 1. Start the DB layer first so the UI can talk to it
        dbManager = new DatabaseManager();

        // 2. The TableView instance (outside the Frame, so it can be shared)
        TableView<Product> productTable = new TableView<>();

        // 3. The Actions instance (passing it the DB and TableView)
        // We cannot pass the TextField here because the Frame has not been created yet
        Actions appActions = new Actions(dbManager, productTable);

        // 4. We create a new Frame instance (passing it the Actions and TableView)
        Frame appFrame = new Frame(appActions, productTable);

        // 5. And set up the stage calling the Frame's getter method (getRootLayout)
        Scene scene = new Scene(appFrame.getBorderPane(), wdScene, htScene);

        // 6. Link the scene to the css file, in the resources' folder:
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // 7. Finally, we create a new stage:
        primaryStage.setTitle("Inventory Management System");
        primaryStage.setScene(scene);
        primaryStage.show();


        //  REFRESH the table
        appActions.refreshTable();
    }




    //  The "stop" method is called by the JavaFX runtime just before the application exits.
    //  It's designed for cleanup tasks. In our case, this is where we close the database connection,
    //     ensuring that all resources are properly released before the program terminates.
    @Override
    public void stop() {
        // JavaFX calls this when the app is closing. Great moment to close DB.
        if (dbManager != null) {
            try {
                dbManager.closeConnection();
            } catch (Exception e) {
                System.err.println("Problem closing DB: " + e.getMessage());
            }
        }
    }
}
