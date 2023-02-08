package hr.java.projekt.app;

import hr.java.projekt.controller.*;
import hr.java.projekt.threads.LastHistoryReaderThread;
import javafx.application.Platform;
import javafx.beans.value.ObservableStringValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class MainController {
    @FXML
    private TabPane tabPane;
    @FXML
    private Label lastHistoryRecord;

    public void initialize() throws IOException {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        new LastHistoryReaderThread(lastHistoryRecord).start();

        showHome();
    }

    private void showView(String title, URL fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(fxml);

        ScrollPane scrollPane = new ScrollPane(fxmlLoader.load());
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        Tab tab = new Tab(title, scrollPane);

        if (fxmlLoader.getController() instanceof CanSetTabTitle controller) {
            controller.storeTabReference(tab);
        }

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private void showHome() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HomeController.class.getResource("home.fxml"));

        ScrollPane scrollPane = new ScrollPane(fxmlLoader.load());
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        Tab tab = new Tab("Home", scrollPane);
        tab.setClosable(false);

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    public void showOsnovniPodatciView() throws IOException {
        showView("Osnovni podatci o tvrtki", BasicCompanyDataController.class.getResource("osnovni-podatci-view.fxml"));
    }

    public void showArticlesView() throws IOException {
        showView("Artikli", ArticleEditorController.class.getResource("article-editor.fxml"));
    }

    public void showChangesView() throws IOException {
        showView("Promjene", HistoryController.class.getResource("changes-view.fxml"));
    }

    public void showPartnersView() throws IOException {
        showView("Partneri", PartnerEditorController.class.getResource("partner-editor.fxml"));
    }

    public void showAccountsView() throws IOException {
        showView("Konta", AccountEditorController.class.getResource("account-editor.fxml"));
    }

    public void showAssetInputView() throws IOException {
        showView("Primka", AccountEditorController.class.getResource("asset_input-editor.fxml"));
    }

    public void showDefaultParametersEditor() throws IOException {
        showView("Parametri dokumenata", DefaultDocumentParametersController.class.getResource("default_accounts_editor.fxml"));
    }

    public void showStockView() throws IOException {
        showView("Stanje zaliha", StockViewController.class.getResource("stock-view.fxml"));
    }

    public void showInvoiceOutputEditorView() throws IOException {
        showView("Račun-otpremnica", StockViewController.class.getResource("invoice_output-editor.fxml"));
    }
}