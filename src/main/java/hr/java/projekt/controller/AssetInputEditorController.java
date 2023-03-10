/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.controller;

import hr.java.projekt.database.AssetInputRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.javafx.BigDecimalTextField;
import hr.java.projekt.javafx.LongNumberTextField;
import hr.java.projekt.model.Entity;
import hr.java.projekt.model.articles.Article;
import hr.java.projekt.model.articles.Asset;
import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.history.CreatedChangeRecord;
import hr.java.projekt.model.history.UpdatedChangeRecord;
import hr.java.projekt.model.inventory.ArticleTransaction;
import hr.java.projekt.model.inventory.assetinput.AssetInput;
import hr.java.projekt.model.inventory.assetinput.AssetInputBuilder;
import hr.java.projekt.threads.HistoryWriterThread;
import hr.java.projekt.util.dialog.ConfirmationDialog;
import hr.java.projekt.util.dialog.MessageBox;
import hr.java.projekt.util.dialog.SelectionDialog;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AssetInputEditorController implements CanSetTabTitle {
    @FXML
    private TableView<ArticleTransaction> transactionTableView;
    @FXML
    private TableColumn<ArticleTransaction, String> codeColumn;
    @FXML
    private TableColumn<ArticleTransaction, String> nameColumn;
    @FXML
    private TableColumn<ArticleTransaction, BigDecimal> quantityColumn;
    @FXML
    private TableColumn<ArticleTransaction, BigDecimal> discountColumn;
    @FXML
    private TableColumn<ArticleTransaction, String> measureColumn;
    @FXML
    private TableColumn<ArticleTransaction, BigDecimal> priceColumn;
    @FXML
    private TableColumn<ArticleTransaction, BigDecimal> amountColumn;
    @FXML
    private Spinner<Double> quantitySpinner;
    @FXML
    private Spinner<Double> priceSpinner;
    @FXML
    private Spinner<Double> discountSpinner;
    @FXML
    private LongNumberTextField assetInputId;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField invoiceField;
    @FXML
    private Text partnerLabel;
    @FXML
    private BigDecimalTextField invoiceAmountField;
    @FXML
    private DatePicker invoiceDatePicker;
    @FXML
    private Text assetLabel;
    @FXML
    private Button deleteButton;
    @FXML
    private Button addRowButton;
    @FXML
    private Button cancelRowButton;
    @FXML
    private Button deleteRowButton;
    @FXML
    private Text totalText;
    private Tab tab;

    private Optional<AssetInput> document;
    private Optional<Business> selectedPartner;
    private Optional<Article> selectedArticle;
    private NumberFormat numberFormatter;
    private AssetInputRepository assetInputRepository;
    private static final Logger logger = LoggerFactory.getLogger(AssetInputEditorController.class);

    @FXML
    private void initialize() {
        assetInputRepository = new AssetInputRepository();

        numberFormatter = NumberFormat.getInstance();
        numberFormatter.setGroupingUsed(true);
        numberFormatter.setMinimumFractionDigits(2);
        numberFormatter.setMaximumFractionDigits(2);

        codeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getArticle().getCode()));
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getArticle().getName()));
        quantityColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getQuantity()));
        measureColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getArticle().getMeasure()));
        discountColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDiscount()));
        priceColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrice()));
        amountColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTotal()));

        transactionTableView.getItems().addListener((ListChangeListener<? super ArticleTransaction>) observable -> {
            totalText.setText(numberFormatter.format(AssetInput.getTransactionsTotalAmount(transactionTableView.getItems())));
        });

        quantitySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0));
        priceSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0));
        discountSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0));

        assetInputId.focusedProperty().addListener((observableValue, unfocused, focused) -> {
            if (unfocused) {
                try {
                    Optional<AssetInput> selectedDocument = assetInputRepository.get(assetInputId.getNumber());
                    selectedDocument.ifPresent(this::setFields);
                } catch (DatabaseException ex) {
                    MessageBox.show("Primka", "Pogre??ka u radu s bazom podataka", "Primka nije dohva??ena iz baze podataka!", ex);
                }
            }
        });

        invoiceAmountField.setNumber(BigDecimal.ZERO.setScale(2,RoundingMode.HALF_UP));

        document = Optional.empty();
        selectedPartner = Optional.empty();
        selectedArticle = Optional.empty();

        deleteRowButton.setDisable(true);
        cancelRowButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @FXML
    private void selectPartner() throws IOException {
        Optional<Business> selection = SelectionDialog.showDialog("Partner", "Odabir partnera", "partner-selection-dialog.fxml");
        if (selection.isPresent()) {
            selectedPartner = selection;
            partnerLabel.setText(selection.get().getName());
        } else {
            selectedPartner = Optional.empty();
            partnerLabel.setText("-");
        }
    }

    @FXML
    private void selectArticle() throws IOException {
        Optional<Article> selection = SelectionDialog.showDialog("Artikl", "Odabir artikla", "article-selection-dialog.fxml");
        if (selection.isPresent()) {
            if (selection.get() instanceof Asset) {
                selectedArticle = selection;
                assetLabel.setText(selection.get().getName());
                cancelRowButton.setDisable(false);
            } else {
                MessageBox.show("Artikl", "Pogre??no odabran artikl", "Odabran je nedozvoljeno artikl -> usluga!", Alert.AlertType.WARNING);
                resetRow();
            }
        } else resetRow();
    }

    @FXML
    private void addRowToTable() {
        if (selectedArticle.isEmpty()) {
            MessageBox.show("Primka", "Artikl nije odabran", "Morate odabrati artikl da bi dodali stavku u primku!", Alert.AlertType.WARNING);
            return;
        }

        BigDecimal quantity = BigDecimal.valueOf(quantitySpinner.getValue()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal price = BigDecimal.valueOf(priceSpinner.getValue()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = BigDecimal.valueOf(discountSpinner.getValue()).setScale(2, RoundingMode.HALF_UP);

        StringBuilder validationError = new StringBuilder();

        if (quantity.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)))
            validationError.append("Koli??ina ne smije biti 0!\n");
        if (price.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)))
            validationError.append("Cijena ne smije biti 0!\n");
        if (discount.compareTo(new BigDecimal("100.00")) > 0) validationError.append("Popust nije ispravan!\n");

        if (validationError.isEmpty()) {
            int index = transactionTableView.getSelectionModel().getSelectedIndex();
            ArticleTransaction transaction = new ArticleTransaction(selectedArticle.get(), quantity, discount, price);

            if (index == -1) transactionTableView.getItems().add(transaction);
            else transactionTableView.getItems().set(index, transaction);

            resetRow();
        } else {
            MessageBox.show("Primka", "Neispravno unesene vrijednosti zapisa!", validationError.toString(), Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void resetRow() {
        selectedArticle = Optional.empty();
        assetLabel.setText("-");
        quantitySpinner.getValueFactory().setValue((double) 0);
        priceSpinner.getValueFactory().setValue((double) 0);
        discountSpinner.getValueFactory().setValue((double) 0);
        transactionTableView.getSelectionModel().clearSelection();
        addRowButton.setText("Dodaj stavku");
        deleteRowButton.setDisable(true);
        cancelRowButton.setDisable(true);
    }

    private void resetFields() {
        assetInputId.setNumber(0L);
        datePicker.setValue(null);
        setTabTitle("Primka");
        invoiceAmountField.setNumber(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        document = Optional.empty();

        selectedPartner = Optional.empty();
        partnerLabel.setText("-");
        invoiceField.setText("");
        invoiceDatePicker.setValue(null);

        transactionTableView.getItems().clear();
        deleteButton.setDisable(true);
    }

    @FXML
    private void setRow() {
        ArticleTransaction transaction = transactionTableView.getSelectionModel().getSelectedItem();

        if (Optional.ofNullable(transaction).isPresent()) {
            selectedArticle = Optional.of(transaction.getArticle());
            assetLabel.setText(transaction.getArticle().getName());
            quantitySpinner.getValueFactory().setValue(transaction.getQuantity().doubleValue());
            priceSpinner.getValueFactory().setValue(transaction.getPrice().doubleValue());
            discountSpinner.getValueFactory().setValue(transaction.getDiscount().doubleValue());
            addRowButton.setText("A??uriraj stavku");
            deleteRowButton.setDisable(false);
            cancelRowButton.setDisable(false);
        }
    }

    @FXML
    private void deleteRow() {
        int index = transactionTableView.getSelectionModel().getSelectedIndex();

        if (index >= 0) {
            transactionTableView.getItems().remove(index);
            resetRow();
        }
    }

    private void setFields(AssetInput assetInput) {
        document = Optional.of(assetInput);
        selectedPartner = Optional.of(assetInput.getSupplier());
        partnerLabel.setText(assetInput.getSupplier().getName());
        setTabTitle("Primka " + document.get().getId());
        assetInputId.setNumber(assetInput.getId());

        invoiceField.setText(assetInput.getInvoiceId());
        invoiceDatePicker.setValue(assetInput.getInvoiceDate());
        invoiceAmountField.setNumber(assetInput.getAmount());
        datePicker.setValue(assetInput.getInputDate());

        transactionTableView.getItems().clear();
        transactionTableView.getItems().addAll(FXCollections.observableList(assetInput.getTransactions()));
        deleteButton.setDisable(false);
    }

    @FXML
    private void selectAction() throws IOException {
        try {
            Optional<AssetInput> selection = SelectionDialog.showDialog("Primka", "Odabir primke", "asset_input-selection-dialog.fxml");
            if (selection.isPresent()) {
                Optional<AssetInput> selectedDocument = assetInputRepository.get(selection.get().getId());
                selectedDocument.ifPresent(this::setFields);
            }
        } catch (DatabaseException ex) {
            logger.error(ex.getMessage(), ex.getCause());
            MessageBox.show("Primka", "Gre??ka u radu s bazom podataka", "Nije mogu??e dohvatiti odabranu primku!", ex);
        }
    }

    @FXML
    private void deleteAction() {
        if (document.isPresent()) {
            try {
                boolean confirmation = ConfirmationDialog.showDialog("Primka", "Brisanje primke", "Jeste li sigurni da ??elite obrisati Primku " + document.get().getId() + "?");
                if (confirmation) {
                    assetInputRepository.delete(document.get());
                    MessageBox.show("Primka ", "Brisanje primke", "Primka " + document.get().getId() + " je uspje??no obrisana iz baze podataka.");

                    resetRow();
                    resetFields();
                }
            } catch (DatabaseException ex) {
                logger.error(ex.getMessage(), ex.getCause());
                MessageBox.show("Primka", "Brisanje primke", "Primka " + document.get().getId() + " nije obrisana iz baze podataka!", ex);
            }
        }
    }

    @FXML
    private void cancelEdit() {
        if (document.isEmpty() || transactionTableView.getItems().isEmpty()) return;

        boolean confirmation = ConfirmationDialog.showDialog("Primka", "Odustajanje", "Jeste li sigurni da ??elite odustati od ure??ivanja primke?");
        if (confirmation) {
            resetRow();
            resetFields();
            document = Optional.empty();
        }
    }

    @FXML
    private void saveAction() {
        LocalDate date = datePicker.getValue();

        String invoiceId = invoiceField.getText();
        LocalDate invoiceDate = invoiceDatePicker.getValue();
        BigDecimal invoiceAmount = invoiceAmountField.getNumber();

        List<ArticleTransaction> transactions = transactionTableView.getItems().parallelStream().toList();
        BigDecimal amount = AssetInput.getTransactionsTotalAmount(transactions);

        StringBuilder validationError = new StringBuilder();

        if (selectedPartner.isEmpty()) {
            validationError.append("Dobavlja?? nije odabran!\n");
        }
        if (Optional.ofNullable(date).isEmpty()) {
            validationError.append("Datum primke nije naveden!\n");
        }
        if (invoiceId.isBlank()) {
            validationError.append("Dokument dobavlja??a nije naveden!\n");
        }
        if (Optional.ofNullable(invoiceDate).isEmpty()) {
            validationError.append("Datum dokumenta dobavlja??a nije naveden!\n");
        }
        if (selectedArticle.isPresent()) {
            validationError.append("Redak u tablici zapisa nije dovr??en!!!\n");
        }
        if (transactions.isEmpty()) {
            validationError.append("Primka ne sadr??i niti jedan zapis!\n");
        }
        if (amount.compareTo(invoiceAmount) != 0) {
            validationError.append("Iznos ra??una nije jednak zbroju iznosa stavki iz tablice!\n");
        }

        if (validationError.isEmpty()) {
            AssetInputBuilder builder = new AssetInputBuilder();
            builder.setSupplier(selectedPartner.get());
            builder.setAmount(amount);
            builder.setInputDate(date);
            builder.setInvoiceDate(invoiceDate);
            builder.setInvoiceId(invoiceId);
            builder.setTransactions(transactions);

            boolean confirmation = ConfirmationDialog.showDialog("Primka", "Spremanje primke", "??elite li nastaviti sa spremanjem primke?");
            if (!confirmation) return;

            try {
                Long documentId = document.map(Entity::getId).orElse(assetInputId.getNumber());

                if (document.isPresent()) {
                    AssetInput assetInput = builder.setId(document.get().getId()).build();
                    assetInputRepository.update(documentId, assetInput);
                    new HistoryWriterThread<>(new UpdatedChangeRecord<>(document.get(), assetInput)).start();
                } else {
                    AssetInput assetInput = builder.build();

                    if (documentId.equals(0L)) {
                        documentId = assetInputRepository.save(assetInput);
                    } else {
                        assetInputRepository.save(documentId, assetInput);
                    }

                    new HistoryWriterThread<>(new CreatedChangeRecord<>(assetInput)).start();
                }

                MessageBox.show("Primka", "Uspje??no spremanje", "Primka " + documentId + " je uspje??no spremljena u bazu podataka!");
                resetFields();
            } catch (DatabaseException ex) {
                logger.error(ex.getMessage(), ex.getCause());
                MessageBox.show("Primka", "Gre??ka u radu s bazom podataka", "Spremanje primke nije uspjelo!", ex);
            }

        } else MessageBox.show("Primka", "Gre??ka", validationError.toString(), Alert.AlertType.ERROR);
    }

    @Override
    public void storeTabReference(Tab tab) {
        this.tab = tab;
    }

    @Override
    public void setTabTitle(String title) {
        tab.setText(title);
    }
}
