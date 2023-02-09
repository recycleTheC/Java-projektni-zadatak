/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.controller;

import hr.java.projekt.database.BusinessRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.business.Business;
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

public final class PartnerSelectionDialogController implements CanReturnTableViewSelection<hr.java.projekt.model.business.Business> {
    private static final Logger logger = LoggerFactory.getLogger(PartnerSelectionDialogController.class);
    @FXML
    public TableView<Business> partnerTableView;
    @FXML
    private TableColumn<Business, Long> idColumn;
    @FXML
    private TableColumn<Business, String> nameColumn;
    @FXML
    private TableColumn<Business, String> addressColumn;
    @FXML
    private TableColumn<Business, String> postalColumn;
    @FXML
    private TableColumn<Business, String> oibColumn;
    @FXML
    private TextField filterField;

    public void initialize() {
        BusinessRepository repository = new BusinessRepository();
        try {
            FilteredList<Business> filteredData = new FilteredList<>(FXCollections.observableList(repository.getMany()), p -> true);

            filterField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(partner -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    else {
                        String lowerCaseFilter = newValue.toLowerCase();

                        if (partner.getId().toString().startsWith(lowerCaseFilter)) return true;
                        else if (partner.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                        else if (partner.getAddress().contains(lowerCaseFilter)) return true;
                        else if (partner.getOIB().startsWith(lowerCaseFilter)) return true;
                        else return partner.getPostalCodeAndTown().contains(lowerCaseFilter);
                    }
                });
            });

            SortedList<Business> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(partnerTableView.comparatorProperty());
            partnerTableView.setItems(sortedData);
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e.getCause());
            MessageBox.show("Odabir partnera", "Pogreška", "Nije moguće dohvatiti partnere iz baze podataka!", e);
        }

        idColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        addressColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAddress()));
        postalColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPostalCodeAndTown()));
        oibColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getOIB()));
    }

    @Override
    public Business getSelectedValue() {
        return this.partnerTableView.getSelectionModel().getSelectedItem();
    }
}
