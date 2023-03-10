package hr.java.projekt.controller;

import hr.java.projekt.model.Entity;
import hr.java.projekt.model.account.Account;
import hr.java.projekt.model.articles.Article;
import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.company.BasicCompanyData;
import hr.java.projekt.model.history.*;
import hr.java.projekt.model.inventory.assetinput.AssetInput;
import hr.java.projekt.model.invoices.Invoice;
import hr.java.projekt.model.invoices.InvoiceOutput;
import hr.java.projekt.model.operator.Operator;
import hr.java.projekt.util.dialog.MessageBox;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class HistoryController {
    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);
    @FXML
    private DatePicker startDateField;
    @FXML
    private DatePicker endDateField;
    @FXML
    private TableView<ChangeHistoryRecord> changeRecordsTable;
    @FXML
    private TableColumn<ChangeHistoryRecord, LocalDateTime> recordDateTimeColumn;
    @FXML
    private TableColumn<ChangeHistoryRecord, String> documentRecordColumn;
    @FXML
    private TableColumn<ChangeHistoryRecord, String> identifierRecordColumn;
    @FXML
    private TableColumn<ChangeHistoryRecord, String> operatorRecordColumn;
    @FXML
    private TableColumn<ChangeHistoryRecord, String> changeTypeRecordColumn;
    @FXML
    private TextArea oldTextArea;
    @FXML
    private TextArea newTextArea;

    @FXML
    private void initialize() {
        recordDateTimeColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTimeStamp()));
        recordDateTimeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
                }
            }
        });
        documentRecordColumn.setCellValueFactory(cell -> {
            if (Optional.ofNullable(cell.getValue().getOldValue()).isPresent()) {
                return getRecordType(cell.getValue().getOldValue());
            } else if (Optional.ofNullable(cell.getValue().getNewValue()).isPresent()) {
                return getRecordType(cell.getValue().getNewValue());
            } else return new SimpleStringProperty("nepoznato");
        });
        changeTypeRecordColumn.setCellValueFactory(cell -> {
            if (cell.getValue() instanceof CreatedChangeRecord<?>) {
                return new SimpleStringProperty("kreiranje");
            } else if (cell.getValue() instanceof UpdatedChangeRecord<?>) {
                return new SimpleStringProperty("a??uriranje");
            } else if (cell.getValue() instanceof DeletedChangeRecord<?>) {
                return new SimpleStringProperty("brisanje");
            } else return new SimpleStringProperty("nepoznato");
        });
        identifierRecordColumn.setCellValueFactory(cell -> {
            try {
                if (Optional.ofNullable(cell.getValue().getOldValue()).isPresent()) {
                    if (cell.getValue().getOldValue() instanceof WritableHistory item) {
                        return new SimpleStringProperty(item.getIdentifier());
                    }
                } else if (Optional.ofNullable(cell.getValue().getNewValue()).isPresent()) {
                    if (cell.getValue().getNewValue() instanceof WritableHistory item) {
                        return new SimpleStringProperty(item.getIdentifier());
                    }
                }
            } catch (NullPointerException e) {
                return new SimpleStringProperty("pogre??ka kod ??itanja");
            }

            return new SimpleStringProperty("nepoznato");
        });

        operatorRecordColumn.setCellValueFactory(cell -> {
            try {
                if (Optional.ofNullable(cell.getValue().getOperator()).isPresent()) {
                    return new SimpleStringProperty(cell.getValue().getOperator().getName());
                }
            } catch (NullPointerException e) {
                return new SimpleStringProperty("pogre??ka kod ??itanja");
            }
            return new SimpleStringProperty("nepoznato");
        });

        fetchChanges();
    }

    @FXML
    private void fetchChanges() {
        startDateField.setValue(null);
        endDateField.setValue(null);
        try {
            FilteredList<ChangeHistoryRecord> filteredData = new FilteredList<>(FXCollections.observableArrayList(ChangeHistoryRecordFiles.readAll()), p -> true);

            startDateField.valueProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(record -> {
                    if (newValue == null) return true;
                    else return record.getTimeStamp().isAfter(startDateField.getValue().atStartOfDay());
                });
            });

            endDateField.valueProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(record -> {
                    if (newValue == null) return true;
                    else return record.getTimeStamp().isBefore(endDateField.getValue().atStartOfDay());
                });
            });

            SortedList<ChangeHistoryRecord> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(changeRecordsTable.comparatorProperty());
            changeRecordsTable.setItems(sortedData);
        } catch (IOException | ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            MessageBox.show("Dohvat promjena", "Gre??ka kod dohvata podataka", "Dohvat promjena nije mogu??!", e);
        }
    }

    private SimpleStringProperty getRecordType(Entity value) {
        if (value instanceof Article) {
            return new SimpleStringProperty("Artikl");
        } else if (value instanceof BasicCompanyData) {
            return new SimpleStringProperty("Osnovni podatci tvrtke");
        } else if (value instanceof Business) {
            return new SimpleStringProperty("Partner");
        } else if (value instanceof Account) {
            return new SimpleStringProperty("Konto");
        } else if (value instanceof AssetInput) {
            return new SimpleStringProperty("Primka");
        } else if (value instanceof InvoiceOutput) {
            return new SimpleStringProperty("Ra??un-otpremnica");
        } else if (value instanceof Invoice) {
            return new SimpleStringProperty("Ra??un");
        } else if (value instanceof Operator) {
            return new SimpleStringProperty("Operater");
        } else return new SimpleStringProperty("Nepoznato");
    }

    @FXML
    private void showChanges() {
        ChangeHistoryRecord selectedRecord = changeRecordsTable.getSelectionModel().getSelectedItem();
        if (Optional.ofNullable(selectedRecord).isEmpty()) {
            oldTextArea.setText("");
            newTextArea.setText("");
            return;
        }

        if (Optional.ofNullable(selectedRecord.getOldValue()).isPresent()) {
            if (selectedRecord.getOldValue() instanceof WritableHistory item) {
                oldTextArea.setText(item.stringGenerator().toString());
            } else oldTextArea.setText("Pogre??ka kod ??itanja!");
        } else oldTextArea.setText("");

        if (Optional.ofNullable(selectedRecord.getNewValue()).isPresent()) {
            if (selectedRecord.getNewValue() instanceof WritableHistory item) {
                newTextArea.setText(item.stringGenerator().toString());
            } else newTextArea.setText("Pogre??ka kod ??itanja!");
        } else newTextArea.setText("");
    }
}
