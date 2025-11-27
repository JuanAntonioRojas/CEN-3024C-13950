package com.ims;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

//  Import our "Lego brick" factory
import static com.ims._30_GUI_Builder.*;

/**
 *  This is the "Pop-Up Window Factory" (a Controller/Helper).

 *  This is one of the most powerful "Lego bricks" in the whole project. It's a 'final' class, so no one can change it
 *     and its constructor is 'private' so no one can make an 'instance' of it.
 *  It has ONE 'static' method, '_31a_showAddDialog', which is  a "generic" factory for building 'any' kind of pop-up
 *     form (for Products, Users, Suppliers, Requisitions, etc.).
 *  This is the king of my "Don't Repeat Yourself" strategy.
 */


public final class _31_ViewAdd {

    //  A private constructor to prevent 'new _31_ViewAdd()'.
    private _31_ViewAdd() {}




    /**
     *  This is a powerful, generic, reusable "factory" helper method for creating any kind of "New Item."
     *  Through a pop-up window it can create any kind of entity (T): User, Product or Supplier.
     *  It opens a simple modal dialog that delegates form creation to the caller.
     *  It's like a "pizza restaurant" that can make any pizza you want, as long as you give it the right "toppings," in this
     *     case those would be the components for the Product, the User, and the Supplier. Just  give it the right instructions.

     *  THE BIG IDEA:
     *  We want to separate the general logic of "how to build a pop-up window" from the specific logic "what fields should this
     *     particular window have". This method handles the general part, and you provide the specific instructions as parameters.

     *     @param <T> This is a "generic type". It's a placeholder for the kind of thing we are creating, for example,
     *        a '_11_ModelProd' or a '_13_ModelUser'. The Generic <T> makes this method incredibly flexible.
     *     It lets the compiler infer the concrete type from the lambdas, and it returns an "Optional" so we get a value
     *        only when the user clicks Save (it would be empty on Cancel, not null).

     *     @param title The simple text that will appear at the top of the pop-up window, e.g., "Add New Product".

     *     @param formFactory These are the first custom "instructions for the chef" for what toppings.
     *     The 'Function' (lambda) we provide is a parameter function (a set of Instructions) that takes a
     *        blank pizza dough (an empty 'GridPane') and lets us add all our 'TextField's and 'ComboBox's (toppings) to it.
     *     It does two things:
     *        1. It tells the chef what toppings to put on it (you add your TextFields, Labels, etc.).
     *        2. It then hands back a written RECIPE (a 'Supplier<T>') for how to assemble the final product from those toppings.
     *     In other words, this "formFactory" is of type "Function<GridPane, Supplier<T>>" that offers the formFactory an empty
     *        GridPane (the popup window), to which we add the labeled controls and return a Supplier<T>, that knows how to read
     *        those controls (in the grid) and then build the entity model: Product, User or Supplier when OK is pressed.
     *     This functional interface "Supplier<T>" defers the model construction until the moment the user actually presses Save,
     *        so it captures (and holds) the current values rather than whatever existed when the form was created.

     *     @param onSave These are the second custom "instructions for the chef".
     *     This is an action, a Consumer<T>: a lambda, telling the chef "what to do with the food after it's made"
     *         immediately after the "Save" button is clicked; typically inserting on the DB and refreshing a table.
     *     In our client-server model app, we almost always set this to 'null' because our real "save" (the server call) happens after
     *         the pop-up closes (on click), not 'inside' it (i.e. one can go back and change any values).
     *     It says what to do with the built entity model (insert on DB, etc.), and the "saving" happens over the network 'after'
     *         the dialog closes.

     *     @return An 'Optional<T>'. This is like the pizza box at the end.
     *     The box will either contain the finished product (if you clicked "OK"), or it will be empty (if one clicked "Cancel").
     *     This is a safe way to handle the result without worrying about 'null' errors.


     *     So the flow is:
     *     1. The "add" dialog creates a grid
     *     2. The "formFactory" lays out fields and returns a builder (Supplier<T>)
     *     3. Then the user edits
     *     4. Finally, on "Save" the supplier builds T, onSave runs, and the method returns Optional.of(T): Prod, User or Supplier
     *     5. and vice versa, on "Cancel" it returns Optional.empty().

     *   This keeps the window elements (stage, buttons) separated from Form Details (controls, validation, persistence)
     *     while staying tiny and reusable.
     */
    public static <T> Optional<T> _31a_showAddDialog(
            String title,  //  in STEP 2d.
            Function<GridPane, Supplier<T>> formFactory,  //  in STEP 1a and STEP 1b. The formFactory is created on _24_CtrlProd
            Consumer<T> onSave )
    {
        //  STEP 1: PREPARE THE "FORM"

        //  STEP 1a: Create the 'GridPane' (the "blank pizza dough").
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setStyle("-fx-padding: 16;");


        //  STEP 1b: Call the 'formFactory' lambda (Parameter 1).
        //     This is where the 'caller' (_22_CtrlSignup, _24_CtrlProd) adds all their text fields to our 'grid'.
        //     It returns the "Recipe" ('Supplier<T>'), which we save in 'buildEntity'.
        Supplier<T> buildEntity = formFactory.apply(grid);  //  The logic and the grid were created on _24_CtrlProd


        //  STEP 2: PREPARE THE "WINDOW"

        //  STEP 2a: Create the "Save" and "Cancel" buttons using our '_30_GUI_Builder' factory.
        Button btnSignOk = _31f_Btn("Save", 130, null);
        btnSignOk.setDefaultButton(true); //  Press "Enter" to click

        Button btnSignCancel = _31f_Btn("Cancel", 130, null);
        btnSignCancel.setCancelButton(true); //  Press "Escape" to click

        //  STEP 2b: Put the buttons in a 'ToolBar' at the bottom.
        ToolBar footer = new ToolBar(btnSignOk, new Separator(), btnSignCancel);
        footer.setPadding(new Insets(6));

        //  STEP 2c: Put the 'grid' (form) and 'footer' (buttons) into a 'VBox' (a vertical stack).
        //     This 'root' is the final content of our pop-up.
        VBox root = new VBox(grid, footer);
        root.setSpacing(12);

        //  STEP 2d: Create the 'Stage' (the pop-up window itself).
        Stage stage = new Stage();

        //  Parameter 1:
        stage.setTitle(title);


        //  'MODALITY.APPLICATION_MODAL' means the user cannot click back to the main app until they close this pop-up.
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));

        //  STEP 2e: This is a little "hack." We need a 'final' container to hold the object that the lambda will create.
        //     An array of 1 is a common trick.
        final T[] resultHolder = (T[]) new Object[1];


        //  STEP 3: "WIRE" THE BUTTONS

        //  STEP 3a: Wire the "Save" button.
        btnSignOk.setOnAction(e -> {
            try {
                //  STEP 3a-1: Call the "Recipe" ('Supplier<T>')!
                //     The '.get()' method runs the lambda that reads the text fields and builds the object.
                //     If validation fails, it throws an exception.
                T entity = buildEntity.get();

                //  STEP 3a-2: If the 'onSave' (Parameter 2) was provided, run it. (We usually don't).
                if (onSave != null) onSave.accept(entity);

                //  STEP 3a-3: Put the new object in our "pizza box."
                resultHolder[0] = entity;

                //  STEP 3a-4: Close the pop-up.
                stage.close();

            } catch (Exception ex) {
                //  If 'buildEntity.get()' threw an exception (like "Password cannot be empty"), we catch it and
                //     show it as an error pop-up.
                //  The window 'stays open' so the user can fix it.
                _30i_showError("Validation Error", ex.getMessage());
            }
        });

        //  STEP 3b: Wire the "Cancel" button.
        btnSignCancel.setOnAction(e -> {
            //  STEP 3b-1: Put 'null' in the "pizza box."
            resultHolder[0] = null;
            //  STEP 3b-2: Close the pop-up.
            stage.close();
        });


        //  STEP 4: SHOW THE POP-UP

        //  STEP 4a: This is the big one. 'showAndWait()' pauses all our code right here until the 'stage.close()' method is called.
        stage.showAndWait();

        //  STEP 4b: Once the pop-up is closed, our code resumes. We return the "pizza box" ('resultHolder[0]') wrapped in an 'Optional'.
        //    'ofNullable' automatically creates an 'empty' Optional if the result is 'null' (i.e., user clicked "Cancel").
        return Optional.ofNullable(resultHolder[0]);
    }
}