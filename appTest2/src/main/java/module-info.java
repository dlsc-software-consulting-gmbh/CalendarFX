module com.example.apptest2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.apptest2 to javafx.fxml;
    exports com.example.apptest2;
}