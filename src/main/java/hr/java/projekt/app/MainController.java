package hr.java.projekt.app;

import hr.java.projekt.controller.ArticleEditorController;
import hr.java.projekt.controller.DemoController;
import hr.java.projekt.controller.BasicCompanyDataController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

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
        showView("Osnovni podatci o tvrtki", BasicCompanyDataController.class.getResource("osnovni-podatci-view.fxml"));
    }

    public void showTestView() throws IOException {
        showView("Testni View", DemoController.class.getResource("demo-view.fxml"));
    }
    /*public void showArticlesView() throws IOException {
        URL url = ArticleSelectionDialog.class.getResource("article-selection-dialog.fxml");
        Optional<Article> article = SelectionDialog.showDialog("Artikli", "Odabir artikla", url);
        System.out.println(article.get().toString());
    }*/

    public void showArticlesView() throws IOException {
        showView("Artikli", ArticleEditorController.class.getResource("article-editor.fxml"));
    }
}