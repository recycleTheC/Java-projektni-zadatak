/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.controller;

import hr.java.projekt.database.ArticleRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.articles.ArticleStock;
import hr.java.projekt.util.dialog.MessageBox;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class StockViewController {
    private static Logger logger = LoggerFactory.getLogger(StockViewController.class);

    @FXML
    private DatePicker datePicker;
    @FXML
    private TableView<ArticleStock> stockTableView;
    @FXML
    private TableColumn<ArticleStock, String> codeColumn;
    @FXML
    private TableColumn<ArticleStock, String> nameColumn;
    @FXML
    private TableColumn<ArticleStock, String> measureColumn;
    @FXML
    private TableColumn<ArticleStock, BigDecimal> stockColumn;
    @FXML
    private TableColumn<ArticleStock, BigDecimal> averagePriceColumn;
    private ArticleRepository articleRepository;

    @FXML
    private void initialize() {
        articleRepository = new ArticleRepository();
        datePicker.setValue(LocalDate.now());

        codeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getArticle().getCode()));
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getArticle().getName()));
        stockColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStock()));
        averagePriceColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getAveragePurchasePrice()));
        measureColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getArticle().getMeasure()));
    }

    @FXML
    private void getStockFromDatabase() {

        LocalDate date = datePicker.getValue();

        if (Optional.ofNullable(date).isPresent()) {
            try {
                stockTableView.setItems(FXCollections.observableList(articleRepository.getStock(date)));
            } catch (DatabaseException ex) {
                logger.error(ex.getMessage(), ex.getCause());
                MessageBox.show("Zalihe", "Pogreška pri radu s bazom podataka", ex.getMessage(), ex);
            }
        } else {
            MessageBox.show("Zalihe", "Neispravnost datuma", "Datum za pretragu zaniha nije upisan!", Alert.AlertType.WARNING);
        }
    }
}
