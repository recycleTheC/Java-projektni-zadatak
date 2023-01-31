/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.controller;

import hr.java.projekt.database.AccountRepository;
import hr.java.projekt.database.DatabaseException;
import hr.java.projekt.javafx.LongNumberTextField;
import hr.java.projekt.model.account.Account;
import hr.java.projekt.model.account.AccountBuilder;
import hr.java.projekt.model.account.AccountType;
import hr.java.projekt.model.history.CreatedChangeRecord;
import hr.java.projekt.model.history.DeletedChangeRecord;
import hr.java.projekt.model.history.UpdatedChangeRecord;
import hr.java.projekt.threads.HistoryWriterThread;
import hr.java.projekt.util.CustomStringConverters;
import hr.java.projekt.util.dialog.ConfirmationDialog;
import hr.java.projekt.util.dialog.MessageBox;
import hr.java.projekt.util.dialog.SelectionDialog;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public final class AccountEditorController implements CanSetTabTitle {
    @FXML
    private LongNumberTextField codeField;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<AccountType> accountTypeComboBox;
    @FXML
    private Button deleteButton;

    private AccountRepository accountRepository;
    private Optional<Account> savedItem;
    private Tab tab;
    private static final Logger logger = LoggerFactory.getLogger(AccountEditorController.class);
    @FXML
    private void initialize() {
        savedItem = Optional.empty();
        accountRepository = new AccountRepository();

        codeField.focusedProperty().addListener((observableValue, unfocused, focused) -> {
            if(unfocused){
                try {
                    Long id = codeField.getNumber();
                    Optional<Account> account = accountRepository.get(id);
                    account.ifPresent(this::setFields);
                } catch (DatabaseException e) {
                    logger.error(e.getMessage(), e.getCause());
                    MessageBox.show("Konto","Pogreška u radu s bazom podataka", "Nije moguće napraviti provjeru u bazi podataka!\n" + e.getCause().getMessage());
                }
            }
        });

        accountTypeComboBox.setItems(FXCollections.observableArrayList(AccountType.values()));
        accountTypeComboBox.setConverter(CustomStringConverters.AccountTypeStringConverter);

        deleteButton.setDisable(true);
    }

    @FXML
    private void selectAction() throws IOException {
        Optional<Account> selection = SelectionDialog.showDialog("Konto", "Odabir konta", "account-selection-dialog.fxml");
        selection.ifPresent(this::setFields);
    }

    @FXML
    private void saveAction(){
        String code = codeField.getText();
        String name = nameField.getText();
        AccountType type = accountTypeComboBox.getValue();

        StringBuilder validationError = new StringBuilder();

        if(code.length() <= 1) validationError.append("Šifra konta nije navedena ili je neispravna!\n");
        if(name.isBlank()) validationError.append("Naziv konta nije naveden!\n");
        if(Optional.ofNullable(type).isEmpty()) validationError.append("Tip konta nije naveden!\n");

        if(!validationError.isEmpty()){
            MessageBox.show("Konto", "Neispravne vrijednosti", validationError.toString(), Alert.AlertType.WARNING);
            return;
        }

        boolean confirmation = ConfirmationDialog.showDialog("Konto", "Spremanje konta", "Želite li spremiti konto '" + code + "'?");
        if(!confirmation) return;

        Account account = new AccountBuilder().setName(name).setCode(code).setType(type).build();

        try {
            if(savedItem.isPresent()) {
                accountRepository.update(savedItem.get().getId(), account);
                new HistoryWriterThread<>(new UpdatedChangeRecord<>(savedItem.get(), account)).start();
            }
            else {
                accountRepository.save(account);
                new HistoryWriterThread<>(new CreatedChangeRecord<>(account)).start();
            }

            MessageBox.show("Konto", "Spremanje konta", "Konto " + account.getCode() + " uspješno spremljen!");
            resetFields();
        } catch (DatabaseException ex) {
            logger.error(ex.getMessage(), ex.getCause());
            MessageBox.show("Konto", "Pogreška u radu s bazom podataka", "Konto " + account.getCode() + " nije spremljen!", ex);
        }
    }

    @FXML
    private void deleteAction(){
        boolean result = ConfirmationDialog.showDialog("Konto", "Brisanje konta", "Jeste li sigurni da želite obrisati konto '" + codeField.getText() + "'?");

        if(result) {
            try {
                accountRepository.delete(savedItem.get());
                new HistoryWriterThread<>(new DeletedChangeRecord<>(savedItem.get())).start();
                MessageBox.show("Konto", "Brisanje konta", "Konto " + savedItem.get().getCode() + " uspješno obrisan!");
                resetFields();
            } catch (DatabaseException ex) {
                logger.error(ex.getMessage(), ex.getCause());
                MessageBox.show("Konto", "Pogreška u radu s bazom podataka", "Konto " + savedItem.get().getCode() + "se ne može obrisati iz baze podataka!", ex);
            }
        }
    }

    @FXML
    private void cancelEdit(){
        if(savedItem.isEmpty() || codeField.getNumber() == 0) return;
        boolean confirmation = ConfirmationDialog.showDialog("Konto", "Odustajanje", "Jeste li sigurni da želite odustati od uređivanja konta?");
        if(confirmation) resetFields();
    }

    @FXML
    private void resetFields() {
        setTabTitle("Knjigovodstvena konta");
        savedItem = Optional.empty();

        codeField.setNumber(0L);
        codeField.setDisable(false);
        deleteButton.setDisable(true);

        nameField.setText("");
        accountTypeComboBox.getSelectionModel().clearSelection();
    }

    private void setFields(Account account){
        setTabTitle("Konto '" + account.getId() + "'");
        savedItem = Optional.of(account);

        codeField.setNumber(Long.valueOf(account.getCode()));
        nameField.setText(account.getName());
        accountTypeComboBox.getSelectionModel().select(account.getType());

        deleteButton.setDisable(false);
        codeField.setDisable(true);
    }
    @Override
    public void storeTabReference(Tab tab) {
        this.tab = tab;
    }

    @Override
    public void setTabTitle(String title) {
        this.tab.setText(title);
    }
}
