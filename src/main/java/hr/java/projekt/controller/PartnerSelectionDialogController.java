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
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PartnerSelectionDialogController implements CanReturnTableViewSelection<hr.java.projekt.model.business.Business> {
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
    private static final Logger logger = LoggerFactory.getLogger(PartnerSelectionDialogController.class);

    public void initialize() {
        BusinessRepository repository = new BusinessRepository();
        try {
            partnerTableView.setItems(FXCollections.observableList(repository.getMany()));
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
