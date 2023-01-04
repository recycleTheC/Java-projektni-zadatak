package hr.java.projekt.app;

import hr.java.projekt.controller.DemoController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.io.IOException;

public class MainController {
    @FXML
    private TabPane tabPane;

    public void initialize(){
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
    }

    public void showTestView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DemoController.class.getResource("demo-view.fxml"));
        tabPane.getTabs().add(new Tab("Test", new ScrollPane(fxmlLoader.load())));
    }
}