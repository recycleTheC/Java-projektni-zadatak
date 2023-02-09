/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.controller;

import hr.java.projekt.database.ArticleRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.articles.Article;
import hr.java.projekt.model.history.ChangeHistoryRecord;
import hr.java.projekt.model.history.ChangeHistoryRecordFiles;
import hr.java.projekt.util.dialog.CanReturnTableViewSelection;
import hr.java.projekt.util.dialog.MessageBox;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public final class ArticleSelectionDialogController implements CanReturnTableViewSelection<Article> {
    private static final Logger logger = LoggerFactory.getLogger(ArticleSelectionDialogController.class);
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
    @FXML
    private TextField filterField;

    public void initialize() {
        ArticleRepository repository = new ArticleRepository();

        try {
            FilteredList<Article> filteredData = new FilteredList<>(FXCollections.observableList(repository.getMany()), p -> true);

            filterField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(article -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    else {
                        String lowerCaseFilter = newValue.toLowerCase();

                        if (article.getCode().toLowerCase().startsWith(lowerCaseFilter)) return true;
                        else if (article.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                        else if (article.getPriceWithVAT().toString().contains(lowerCaseFilter)) return true;
                        else return article.getPriceWithVAT().toString().contains(lowerCaseFilter);
                    }
                });
            });

            SortedList<Article> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(articleTableView.comparatorProperty());
            articleTableView.setItems(sortedData);
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
