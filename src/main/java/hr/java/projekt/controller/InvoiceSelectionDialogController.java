/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.controller;

import hr.java.projekt.database.InvoiceOutputRepository;
import hr.java.projekt.database.InvoiceRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.invoices.Invoice;
import hr.java.projekt.model.invoices.InvoiceOutput;
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

public final class InvoiceSelectionDialogController implements CanReturnTableViewSelection<Invoice> {
    private static final class DateTableViewCell extends TableCell<Invoice, LocalDate> {
        @Override
        protected void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(DateTimeFormatter.ofPattern("dd.MM.yyyy.").format(item));
            }
        }
    }

    @FXML
    public TableView<Invoice> invoiceTableView;
    @FXML
    private TableColumn<Invoice, Long> idColumn;
    @FXML
    private TableColumn<Invoice, String> partnerColumn;
    @FXML
    private TableColumn<Invoice, LocalDate> dateColumn;
    @FXML
    private TableColumn<Invoice, LocalDate> dueDateColumn;
    @FXML
    private TableColumn<Invoice, BigDecimal> amountColumn;
    private static final Logger logger = LoggerFactory.getLogger(InvoiceSelectionDialogController.class);

    public void initialize() {
        InvoiceRepository repository = new InvoiceRepository();

        idColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        partnerColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPartner().getName()));
        dateColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getInvoiceDate()));
        dueDateColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDueDate()));
        amountColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStaticAmount()));

        dateColumn.setCellFactory(column -> new DateTableViewCell());
        dueDateColumn.setCellFactory(column -> new DateTableViewCell());

        try {
            invoiceTableView.setItems(FXCollections.observableList(repository.getMany()));
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e.getCause());
            MessageBox.show("Odabir računa", "Pogreška", "Nije moguće dohvatiti račune iz baze podataka!", e);
        }
    }

    @Override
    public Invoice getSelectedValue() {
        return this.invoiceTableView.getSelectionModel().getSelectedItem();
    }
}
