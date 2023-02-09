/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.controller;

import hr.java.projekt.database.OperatorRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.history.ChangeHistoryRecord;
import hr.java.projekt.model.history.CreatedChangeRecord;
import hr.java.projekt.model.history.DeletedChangeRecord;
import hr.java.projekt.model.history.UpdatedChangeRecord;
import hr.java.projekt.model.operator.Operator;
import hr.java.projekt.model.operator.Role;
import hr.java.projekt.threads.HistoryWriterThread;
import hr.java.projekt.util.dialog.ConfirmationDialog;
import hr.java.projekt.util.dialog.MessageBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class OperatorsEditorController {
    private static Logger logger = LoggerFactory.getLogger(OperatorsEditorController.class);

    @FXML
    private TableView<Operator> operatorsTableView;
    @FXML
    private TableColumn<Operator, String> usernameColumn;
    @FXML
    private TableColumn<Operator, String> nameColumn;
    @FXML
    private TableColumn<Operator, String> roleColumn;
    @FXML
    private ComboBox<Role> roleComboBox;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField nameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button deleteButton;
    private OperatorRepository operatorRepository;
    private Optional<Operator> selectedOperator;

    @FXML
    private void initialize() {
        operatorRepository = new OperatorRepository();
        selectedOperator = Optional.empty();

        usernameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUsername()));
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        roleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRole().getType()));

        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        deleteButton.setDisable(true);

        loadOperators();
    }

    private void loadOperators() {
        try {
            operatorsTableView.setItems(FXCollections.observableList(operatorRepository.getMany()));
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e);
            MessageBox.show("Operateri", "Pogreška pri radu s bazom podataka", e.getMessage(), e);
        }
    }

    @FXML
    private void selectOperator() {
        Operator operator = operatorsTableView.getSelectionModel().getSelectedItem();

        if(operator != null){
            selectedOperator = Optional.of(operator);

            usernameField.setText(operator.getUsername());
            nameField.setText(operator.getName());
            roleComboBox.getSelectionModel().select(operator.getRole());
            passwordField.setText("");
            deleteButton.setDisable(false);
        }
    }

    private void resetFields() {
        selectedOperator = Optional.empty();
        usernameField.setText("");
        nameField.setText("");
        roleComboBox.getSelectionModel().select(null);
        passwordField.setText("");
        deleteButton.setDisable(true);
    }

    @FXML
    private void saveAction() {
        String username = usernameField.getText();
        String name = nameField.getText();
        Role role = roleComboBox.getValue();
        String password = passwordField.getText();

        StringBuilder validationError = new StringBuilder();

        if (name.isBlank()) {
            validationError.append("Ime i prezime operatera nije navedeno!\n");
        }
        if (username.isBlank()) {
            validationError.append("Korisničko ime nije navedeno!\n");
        }
        if (password.isBlank()) {
            validationError.append("Lozinka nije navedena!\n");
        }
        if (Optional.ofNullable(role).isEmpty()) {
            validationError.append("Ovlaštenje nije odabrano!\n");
        }

        if (validationError.isEmpty()) {
            boolean confirmation = ConfirmationDialog.showDialog("Operateri", "Spremanje operatera", "Jeste li sigurni da želite nastaviti sa spremanjem operatera?");
            if (!confirmation) return;

            try {
                Operator operator;

                if (selectedOperator.isEmpty()) {
                    operator = new Operator(username, name, password, role);
                    operatorRepository.save(operator);

                    new HistoryWriterThread<>(new CreatedChangeRecord<>(operator)).start();
                } else {
                    operator = new Operator(selectedOperator.get().getId(), username, name, password, role);
                    operatorRepository.update(selectedOperator.get().getId(), operator);
                    new HistoryWriterThread<>(new UpdatedChangeRecord<>(selectedOperator.get(), operator)).start();
                }

                MessageBox.show("Operateri", "Spremanje operatera", "Operater " + operator.getName() + " je uspješno spremljen!");
                loadOperators();
                resetFields();
            } catch (DatabaseException e) {
                logger.error(e.getMessage(), e);
                MessageBox.show("Operateri", "Pogreška pri radu s bazom podataka", e.getMessage(), Alert.AlertType.ERROR);
            }
        } else MessageBox.show("Operateri", "Pogreška", validationError.toString(), Alert.AlertType.WARNING);
    }

    @FXML
    private void deleteAction() {
        boolean confirmation = ConfirmationDialog.showDialog("Operateri", "Brisanje operatera", "Jeste li sigurni da želite nastaviti s brisanjem operatera?");
        if (!confirmation) return;

        try {
            if (selectedOperator.isEmpty()) {
                MessageBox.show("Operateri","Pogreška","Nešto nije u redu! Nije odabran operater?!",Alert.AlertType.WARNING);
            } else {
                operatorRepository.delete(selectedOperator.get());
                new HistoryWriterThread<>(new DeletedChangeRecord<>(selectedOperator.get())).start();
            }

            MessageBox.show("Operateri", "Brisanje operatera", "Operater " + selectedOperator.get().getName() + " je uspješno obrisan!");
            loadOperators();
            resetFields();
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e);
            MessageBox.show("Operateri", "Pogreška pri radu s bazom podataka", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}