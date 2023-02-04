/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.controller;

import hr.java.projekt.database.AccountRepository;
import hr.java.projekt.database.DatabaseException;
import hr.java.projekt.model.account.Account;
import hr.java.projekt.util.dialog.CanReturnTableViewSelection;
import hr.java.projekt.util.dialog.MessageBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AccountSelectionDialogController implements CanReturnTableViewSelection<Account> {
    @FXML
    public TableView<Account> accountTableView;
    @FXML
    private TableColumn<Account, String> codeColumn;
    @FXML
    private TableColumn<Account, String> nameColumn;
    @FXML
    private TableColumn<Account, String> typeColumn;
    private static final Logger logger = LoggerFactory.getLogger(AccountSelectionDialogController.class);

    public void initialize() {
        AccountRepository repository = new AccountRepository();
        try {
            accountTableView.setItems(FXCollections.observableList(repository.getMany()));
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e.getCause());
            MessageBox.show("Odabir konta", "Pogreška", "Nije moguće dohvatiti konta iz baze podataka!", e);
        }

        codeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCode()));
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        typeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType().getDescription()));
    }

    @Override
    public Account getSelectedValue() {
        return this.accountTableView.getSelectionModel().getSelectedItem();
    }
}
