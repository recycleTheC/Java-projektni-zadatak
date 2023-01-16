module hr.java.projekt.projekt {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens hr.java.projekt.app to javafx.fxml;
    opens hr.java.projekt.controller to javafx.fxml;
    exports hr.java.projekt.app;
}