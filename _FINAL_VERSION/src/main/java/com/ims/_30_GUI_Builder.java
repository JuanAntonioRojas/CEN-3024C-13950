package com.ims;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;

/**
 *   A comprehensive utility class with 'static' helper methods for creating and configuring common JavaFX UI components.

 *   This class follows the DRY ('Don't Repeat Yourself') principle by putting all the repetitive "Lego brick" creation code in one place.
 *   It's my "factory" for making styled buttons, text fields, and other form parts.
 */
public class _30_GUI_Builder {

    //  CONSTRUCTOR
    //  It prevents anyone from creating an instance of this utility class.
    private _30_GUI_Builder() {}




    // =================================================================================================================
    //  General UI Components
    // =================================================================================================================

    // A simple inner class to create a new styled separator with one line of code: 'new HzLine()'
    public static class HzLine extends Separator {
        public HzLine() {
            getStyleClass().add("separator");
        }
    }



    /**
     *   A simple GENERIC helper method method (using Generics) Now, we uo add any labeled 'Node' (it a ComboBox or TextAr
     *   Format: _30a_addNodeToGrid(grid, label, genericNode, row);
     *      @param grid The 'GridPane' to add to.
     *      @param label The 'Label' for the component.
     *      @param node The 'Node' (component) to add.
     *      @param row The row number to add them in.
     */

    public static <T extends Node> T _30a_addNodeToGrid(GridPane grid, String label, T node, int row) {
        //  Step 1: Create the 'Label' (in column 0)
        Label txtLabel = new Label(label + ":");

        //  Step 2: Add the new label to the grid
        grid.add(txtLabel, 0, row);

        //  Step 2: Add the new generic node to the grid
        grid.add(node, 1, row);
        return node;
    }




    // =================================================================================================================
    //  Form Field Builders (for use with GridPane)
    // =================================================================================================================

    /**
     *   TEXT FIELD
     *   Encapsulates the process of creating both the 'Label' and the 'TextField', and adds them to a specified row.
     *      @return The created 'TextField' so we can get its text later.
     */
    public static TextField _30b_TextField(GridPane grid, String labelText, int row) {
        //  Now, we use the simple GENERIC helper method to add it to the grid.
        return _30a_addNodeToGrid(grid, labelText, new TextField(), row);
    }




    /**
     *   PASSWORD FIELD
     *   Creates a 'Label' and a 'PasswordField' and adds them to a specified row in a 'GridPane'.
     *      @return The created 'PasswordField' so we can get its text.
     */
    public static PasswordField _30c_PasswordField(GridPane grid, String labelText, int row) {
        //  Now, we use the simple GENERIC helper method to add it to the grid.
        return _30a_addNodeToGrid(grid, labelText, new PasswordField(), row);
    }



    /**
     *   "SEE" THE PASSWORD: "EYE" METHOD.

     *   Creates a "smart" password component with a "Show" / "Hide" toggle button.
     *   This component stacks a PasswordField and TextField in the same space and uses the button to toggle their visibility.
     *      @param grid The GridPane to add the components to.
     *      @param labelText The text for the label (e.g., "Password").
     *      @param row The row index on the grid.
     *      @return The PasswordField. The .getText() method will always return the correct password.
     */
    public static PasswordField _30c1_TogglePasswordField(GridPane grid, String labelText, int row) {
        //  STEP 1. We need to create the two fields to toggle between them.
        PasswordField passField = new PasswordField();
        TextField textField = new TextField();

        //  STEP 2. Then we add the toggle button
        //ToggleButton toggleButton = new ToggleButton("Show");// replaced by the "eye" (it small and unobtrusive)
        ToggleButton toggleButton = new ToggleButton(" 👁️");  //  This looks better. It's "\uD83D\uDC41"
        //  ... and pad it:
        toggleButton.setStyle("-fx-font-size: 0.8em; -fx-padding: 3 6 3 6;");

        //  STEP 3.  This is the magic!
        //     Binding the text of the two fields together: Typing in one updates the other.
        textField.textProperty().bindBidirectional(passField.textProperty());

        //  STEP 4. Then we stack all three components together.
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(passField, textField, toggleButton);

        //  Aligning the button to the far right inside the stack
        StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
        //  And adding a little padding, so it doesn't touch the edge
        toggleButton.setTranslateX(-5);

        //  STEP 5. Finally, we set the initial visibility (password is hidden by default)
        textField.setVisible(false);
        passField.setVisible(true);

        //  STEP 6. ... so that the action listener for the button, will toggle them.
        toggleButton.setOnAction(e -> {
            if (toggleButton.isSelected()) {
                //  Show password: make TextField visible
                textField.setVisible(true);
                passField.setVisible(false);
                toggleButton.setText("\uD83D\uDC41\uFE0F\u200D\uD83D\uDDE8\uFE0F"); //  the fancy eye
            } else {
                //  Hide password: make PasswordField visible
                textField.setVisible(false);
                passField.setVisible(true);
                toggleButton.setText(" 👁️");
            }
        });

        // STEP 7. Now we get to use the generic helper to add label + stackPane to the grid
        _30a_addNodeToGrid(grid, labelText, stackPane, row);

        //  8. Return the main PasswordField.
        //  The .getText() will always work because its text is bound to the visible TextField.
        return passField;
    }




    /**
     *   NUMBER FIELD
     *   Creates a 'Label' and a 'TextField' that is specially configured to only accept numerical input.
     *      @return The created numerical 'TextField'.
     */
    public static TextField _30d_NumberField(GridPane grid, String labelText, int row) {
        //  Now, we use the simple GENERIC helper method to add it to the grid.
        TextField numberField = _30b_TextField(grid, labelText, row);

        //  Step 1: Add a "listener." This is a little "spy" that watches the 'textProperty' of the field.
        numberField.textProperty().addListener((obs, oldV, newV) -> {
            //  Step 2: 'newV' is the "new value" the user is trying to type. We check it with a "Regex."
            //  Step 3: This Regex says "allow digits, and optionally, one decimal point followed by more digits."
            if (!newV.matches("\\d*([\\.]\\d*)?")) {
                //  Step 4: If the new text doesn't match (like if they typed "a"), we "set" the text back to the "old value," 'oldV'.
                //     This makes it impossible to type letters.
                numberField.setText(oldV);
            }
        });

        return numberField;
    }




    /**
     *   COMBO BOX (Drop-Down Menu)
     *   Creates a generic 'ComboBox' with a 'Label'.
     *      @return The created 'ComboBox' so we can get the selected value.
     */
    public static <T> ComboBox<T> _30e_ComboBox(GridPane grid, String labelText, ObservableList<T> items, int row) {
        ComboBox<T> comboBox = new ComboBox<>(items);
        //  Now, we use the simple GENERIC helper method to add it to the grid.
        return _30a_addNodeToGrid(grid, labelText, comboBox, row);
    }










    // =================================================================================================================
    //  Button Builders
    // =================================================================================================================

    /**
     *   Creates a new 'Button' with a set name, width, and an optional 'Tooltip'.
     *      @param name The text to display on the button.
     *      @param width The maximum width for the button.
     *      @param tooltip An optional 'Tooltip' to show on hover.
     *      @return A fully configured 'Button' instance.
     */
    public static Button _31f_Btn(String name, int width, Tooltip tooltip) {
        //  Step 1: Create the button.
        Button btn = new Button(name);

        //  Step 2: Set its max width (so all our nav buttons are the same size).
        btn.setMaxWidth(width);

        //  Step 3: If the 'tooltip' isn't 'null', add it.
        if (tooltip != null) {
            btn.setTooltip(tooltip);
        }

        //  Step 4: Return the finished button.
        return btn;
    }



    // =================================================================================================================
    //  Alert Dialogs (Pop-ups)
    // =================================================================================================================

    /**
     *   Shows a standard informational pop-up alert (like "Success!").
     *      @param title The text for the title bar of the pop-up.
     *      @param message The main message text inside the pop-up.
     */
    public static void _30h_showAlert(String title, String message) {
        //  Step 1: Create a new 'Alert' of the 'INFORMATION' type.
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null); //  We set 'Header' to 'null' for a cleaner look.
        alert.setContentText(message);

        //  Step 2: 'showAndWait()' pauses the whole application until the user clicks "OK".
        alert.showAndWait();
    }

    /**
     *   Shows an error-styled pop-up alert (like "Failed!").
     *      @param title The title for the error pop-up (e.g., "Login Failed").
     *      @param message The specific error message to show the user.
     */
    public static void _30i_showError(String title, String message) {
        //  Step 1: Create a new 'Alert' of the 'ERROR' type (shows a red 'X').
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        //  Step 2: Pause the app until the user reads the error.
        alert.showAndWait();
    }

    /**
     *   A helper to get a sample list of product categories.
     *      @return An 'ObservableList' (a special JavaFX list) of strings.
     */
    public static ObservableList<String> _31j_getCategories() {
        //  'FXCollections.observableArrayList' just creates the
        //  special kind of list that ComboBoxes and Tables like.
        return FXCollections.observableArrayList(
                "Electronics", "Appliances", "Furniture", "Paintings", "Books", "etc."
        );
    }


    //------------------------------------------------------------------------------------------------------------------
    //  COPY THE BOX CONTENT: For Reordering
    //------------------------------------------------------------------------------------------------------------------

    public static void showInfoCopyable(String title, String message) {
        Alert mssgBox = new Alert(Alert.AlertType.INFORMATION);
        mssgBox.setTitle(title);
        mssgBox.setHeaderText(null);

        //  STEP 1: We need to use a TextArea so the text looks good and can be selected
        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(6);
        textArea.setPrefColumnCount(40);

        mssgBox.getDialogPane().setContent(textArea);

        //  STEP 2: Then we add a Copy button next to OK
        ButtonType copyType = new ButtonType("Copy", ButtonBar.ButtonData.LEFT);
        ButtonType okType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

        //  STEP 3: give it an action: copy the text!
        mssgBox.getButtonTypes().setAll(copyType, okType);

        //  STEP 4: Finally we show dialog and see which button was pressed
        mssgBox.showAndWait().ifPresent(btn -> {
            if (btn == copyType) {
                //  1. Copy to clipboard
                ClipboardContent content = new ClipboardContent();
                content.putString(textArea.getText());
                Clipboard.getSystemClipboard().setContent(content);

                //  2. Highlight all text
                textArea.requestFocus();
                textArea.selectAll();

                //  3. Keep dialog open so user sees it
                //evt.consume();
            }
            //  We don't need an "else" bc if they click on the OK, it naturally closes the mssgBox.
        });
    }
}