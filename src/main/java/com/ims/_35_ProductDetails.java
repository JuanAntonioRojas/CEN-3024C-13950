package com.ims;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;







public class _35_ProductDetails {

    //  Replace _11_ModelProd with your real product model class
    public static void show(Window owner, _11_ModelProd product) {

        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(product.getProdName());

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        //  LEFT: PRODUCT IMAGE
        ImageView imageView = new ImageView();
        try {
            if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
                Image img = new Image(product.getImageUrl(), true);
                imageView.setImage(img);
            }
        } catch (Exception ex) {
            //  If image fails to load, we just leave it empty
            ex.printStackTrace();
        }

        imageView.setFitWidth(260);      //  similar to the T-shirt example
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        VBox leftBox = new VBox(imageView);
        leftBox.setAlignment(Pos.TOP_CENTER);
        leftBox.setPadding(new Insets(0, 20, 0, 0));

        root.setLeft(leftBox);

        //  RIGHT: DETAILS
        Label nameLabel = new Label(product.getProdName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        ComboBox<String> sizeCombo = new ComboBox<>();
        sizeCombo.getItems().addAll("XS", "S", "M", "L", "XL");
        sizeCombo.setPromptText("Select Size");

        Spinner<Integer> qtySpinner = new Spinner<>(1, 999, 1);
        qtySpinner.setPrefWidth(80);

        Button addToCartBtn = new Button("ADD TO CART");
        addToCartBtn.setDefaultButton(true);

        HBox buyRow = new HBox(10, qtySpinner, addToCartBtn);
        buyRow.setAlignment(Pos.CENTER_LEFT);

        //  Header for description
        Label detailsHeader = new Label("Product Details");
        detailsHeader.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextArea detailsArea = new TextArea(product.getDescription());
        detailsArea.setWrapText(true);
        detailsArea.setEditable(false);
        detailsArea.setPrefRowCount(8);

        VBox rightBox = new VBox(10,
                nameLabel,
                priceLabel,
                sizeCombo,
                buyRow,
                new Separator(),
                detailsHeader,
                detailsArea
        );
        rightBox.setAlignment(Pos.TOP_LEFT);
        rightBox.setFillWidth(true);

        root.setCenter(rightBox);

        Scene scene = new Scene(root, 800, 400);
        stage.setScene(scene);
        stage.show();
    }
}
