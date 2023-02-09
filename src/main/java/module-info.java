module hr.java.projekt.projekt {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.slf4j;

    opens hr.java.projekt.app to javafx.fxml;
    opens hr.java.projekt.controller to javafx.fxml;
    opens hr.java.projekt.javafx to javafx.fxml;
    exports hr.java.projekt.app;
    exports hr.java.projekt.model.account;
    exports hr.java.projekt.model.operator;
}