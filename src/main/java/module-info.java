module com.example.lms {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.ims to javafx.fxml;
    exports com.example.ims;
}