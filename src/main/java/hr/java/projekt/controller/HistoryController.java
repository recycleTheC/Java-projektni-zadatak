package hr.java.projekt.controller;

import hr.java.projekt.model.articles.Business;
import hr.java.projekt.model.company.BasicCompanyData;
import hr.java.projekt.model.history.*;
import hr.java.projekt.util.dialog.MessageBox;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class HistoryController {
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
    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);

    @FXML
    private void initialize(){
        recordDateTimeColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTimeStamp()));
        recordDateTimeColumn.setCellFactory((TableColumn<ChangeHistoryRecord, LocalDateTime> column) -> new TableCell<>() {
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
            if(Optional.ofNullable(cell.getValue().getOldValue()).isPresent()){
                if(cell.getValue().getOldValue() instanceof Business){
                    return new SimpleStringProperty("Artikl");
                }
                else if(cell.getValue().getOldValue() instanceof BasicCompanyData){
                    return new SimpleStringProperty("Osnovni podatci tvrtke");
                }
            } else if(Optional.ofNullable(cell.getValue().getNewValue()).isPresent()){
                if(cell.getValue().getNewValue() instanceof Business){
                    return new SimpleStringProperty("Artikl");
                }
                else if(cell.getValue().getNewValue() instanceof BasicCompanyData){
                    return new SimpleStringProperty("Osnovni podatci tvrtke");
                }
            }
            return new SimpleStringProperty("nepoznato");
        });
        changeTypeRecordColumn.setCellValueFactory(cell -> {
            if(cell.getValue() instanceof CreatedChangeRecord<?>){
                return new SimpleStringProperty("kreiranje");
            } else if (cell.getValue() instanceof UpdatedChangeRecord<?>) {
                return new SimpleStringProperty("ažuriranje");
            } else if (cell.getValue() instanceof DeletedChangeRecord<?>) {
                return new SimpleStringProperty("brisanje");
            } else return new SimpleStringProperty("nepoznato");
        });
        identifierRecordColumn.setCellValueFactory(cell -> {
            if(Optional.ofNullable(cell.getValue().getOldValue()).isPresent()){
                if(cell.getValue().getOldValue() instanceof WritableHistory item){
                    return new SimpleStringProperty(item.getIdentifier());
                }
            }
            else if(Optional.ofNullable(cell.getValue().getNewValue()).isPresent()){
                if(cell.getValue().getNewValue() instanceof WritableHistory item){
                    return new SimpleStringProperty(item.getIdentifier());
                }
            }

            return new SimpleStringProperty("nepoznato");
        });
        operatorRecordColumn.setCellValueFactory(cell -> new SimpleStringProperty("nepoznato"));

        try {
            changeRecordsTable.setItems(FXCollections.observableArrayList(ChangeHistoryRecordFiles.readAll()));
        } catch (IOException | ClassNotFoundException e) {
           logger.error(e.getMessage(), e);
            MessageBox.show("Dohvat promjena", "Greška kod dohvata podataka", "Dohvat promjena nije moguć!", e);
        }
    }

    @FXML
    private void showChanges(){
        ChangeHistoryRecord selectedRecord = changeRecordsTable.getSelectionModel().getSelectedItem();
        if(Optional.ofNullable(selectedRecord).isEmpty()) {
            oldTextArea.setText("");
            newTextArea.setText("");
            return;
        }

        if(Optional.ofNullable(selectedRecord.getOldValue()).isPresent()){
            if(selectedRecord.getOldValue() instanceof WritableHistory item){
                oldTextArea.setText(item.stringGenerator().toString());
            }
            else oldTextArea.setText("Pogreška kod čitanja!");
        }
        else oldTextArea.setText("");

        if(Optional.ofNullable(selectedRecord.getNewValue()).isPresent()){
            if(selectedRecord.getNewValue() instanceof WritableHistory item){
                newTextArea.setText(item.stringGenerator().toString());
            }
            else newTextArea.setText("Pogreška kod čitanja!");
        }
        else newTextArea.setText("");
    }
}
