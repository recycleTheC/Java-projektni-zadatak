package hr.java.projekt.app;

import hr.java.projekt.controller.DemoController;
import hr.java.projekt.controller.OsnovniPodatciController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;

public class MainController {
    @FXML
    private TabPane tabPane;

    public void initialize(){
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
    }

    private void showView(String naslov, URL fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(fxml);

        ScrollPane scrollPane = new ScrollPane(fxmlLoader.load());
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        Tab tab = new Tab(naslov, scrollPane);

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    public void showOsnovniPodatciView() throws IOException {
        showView("Osnovni podatci o tvrtki", OsnovniPodatciController.class.getResource("osnovni-podatci-view.fxml"));
    }

    public void showTestView() throws IOException {
        showView("Testni View", DemoController.class.getResource("demo-view.fxml"));
    }
}