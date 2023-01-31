package hr.java.projekt.controller;

import hr.java.projekt.database.ArticleRepository;
import hr.java.projekt.database.DatabaseException;
import hr.java.projekt.model.articles.Article;
import hr.java.projekt.util.dialog.CanReturnTableViewSelection;
import hr.java.projekt.util.dialog.MessageBox;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public final class ArticleSelectionDialogController implements CanReturnTableViewSelection<Article> {
    @FXML
    public TableView<Article> articleTableView;
    @FXML
    private TableColumn<Article, String> codeColumn;
    @FXML
    private TableColumn<Article, String> nameColumn;
    @FXML
    private TableColumn<Article, String> measureColumn;
    @FXML
    private TableColumn<Article, BigDecimal> priceColumn;
    @FXML
    private TableColumn<Article, BigDecimal> priceVATColumn;
    private static final Logger logger = LoggerFactory.getLogger(ArticleSelectionDialogController.class);

    public void initialize(){
        ArticleRepository repository = new ArticleRepository();
        try {
            articleTableView.setItems(FXCollections.observableList(repository.getMany()));
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e.getCause());
            MessageBox.show("Odabir artikla", "Pogreška", "Nije moguće dohvatiti artikle iz baze podataka!", e);
        }

        codeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCode()));
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        measureColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMeasure()));
        priceColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrice()));
        priceVATColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPriceWithVAT()));
    }

    @Override
    public Article getSelectedValue() {
        return this.articleTableView.getSelectionModel().getSelectedItem();
    }
}
