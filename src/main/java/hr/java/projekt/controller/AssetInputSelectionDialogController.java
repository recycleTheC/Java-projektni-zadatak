package hr.java.projekt.controller;

import hr.java.projekt.database.AssetInputRepository;
import hr.java.projekt.database.DatabaseException;
import hr.java.projekt.model.inventory.assetinput.AssetInput;
import hr.java.projekt.util.dialog.CanReturnTableViewSelection;
import hr.java.projekt.util.dialog.MessageBox;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class AssetInputSelectionDialogController implements CanReturnTableViewSelection<AssetInput> {
    private static final class DateTableViewCell extends TableCell<AssetInput, LocalDate> {
        @Override
        protected void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(item));
            }
        }
    }

    @FXML
    public TableView<AssetInput> assetInputTableView;
    @FXML
    private TableColumn<AssetInput, Long> idColumn;
    @FXML
    private TableColumn<AssetInput, String> supplierColumn;
    @FXML
    private TableColumn<AssetInput, LocalDate> dateColumn;
    @FXML
    private TableColumn<AssetInput, BigDecimal> amountColumn;
    @FXML
    private TableColumn<AssetInput, String> invoiceIdColumn;
    @FXML
    private TableColumn<AssetInput, LocalDate> invoiceDateColumn;
    private static final Logger logger = LoggerFactory.getLogger(AssetInputSelectionDialogController.class);

    public void initialize() {
        AssetInputRepository repository = new AssetInputRepository();

        idColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        supplierColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSupplier().getName()));
        dateColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getInputDate()));
        amountColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getAmount()));
        invoiceIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getInvoiceId()));
        invoiceDateColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getInvoiceDate()));

        dateColumn.setCellFactory(column -> new DateTableViewCell());
        invoiceDateColumn.setCellFactory(column -> new DateTableViewCell());

        try {
            assetInputTableView.setItems(FXCollections.observableList(repository.getMany()));
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e.getCause());
            MessageBox.show("Odabir primke", "Pogreška", "Nije moguće dohvatiti primke iz baze podataka!", e);
        }
    }

    @Override
    public AssetInput getSelectedValue() {
        return this.assetInputTableView.getSelectionModel().getSelectedItem();
    }
}
