package hr.java.projekt.controller;

import hr.java.projekt.database.ArticleRepository;
import hr.java.projekt.database.DatabaseException;
import hr.java.projekt.model.articles.Article;
import hr.java.projekt.util.dialog.CanReturnTableViewSelection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;

public final class ArticleSelectionDialog implements CanReturnTableViewSelection<Article> {
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

    public void initialize(){
        ArticleRepository repository = new ArticleRepository();
        try {
            articleTableView.setItems(FXCollections.observableList(repository.getMany()));
        } catch (DatabaseException e) {
            System.out.println(e.getMessage());
        }

        codeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCode()));
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        measureColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMeasure()));
        priceColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrice()));
        priceVATColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPriceWithVAT()));
    }

    @Override
    public TableView<Article> getSelectedValue() {
        return this.articleTableView;
    }
}
