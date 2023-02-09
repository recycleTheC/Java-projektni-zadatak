/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.controller;

import hr.java.projekt.database.ArticleRepository;
import hr.java.projekt.database.InvoiceRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.javafx.BigDecimalTextField;
import hr.java.projekt.javafx.LongNumberTextField;
import hr.java.projekt.model.Entity;
import hr.java.projekt.model.TaxRate;
import hr.java.projekt.model.articles.Article;
import hr.java.projekt.model.articles.ArticleStock;
import hr.java.projekt.model.articles.Asset;
import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.history.CreatedChangeRecord;
import hr.java.projekt.model.history.UpdatedChangeRecord;
import hr.java.projekt.model.inventory.ArticleTransaction;
import hr.java.projekt.model.invoices.Invoice;
import hr.java.projekt.model.invoices.InvoiceBuilder;
import hr.java.projekt.model.invoices.InvoicePayment;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class InvoiceEditorController implements CanSetTabTitle {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceEditorController.class);
    @FXML
    private LongNumberTextField invoiceIdField;
    @FXML
    private DatePicker invoiceDatePicker;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private DatePicker paymentDatePicker;
    @FXML
    private Spinner<Double> quantitySpinner;
    @FXML
    private Spinner<Double> priceSpinner;
    @FXML
    private Spinner<Double> discountSpinner;
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
    private TableView<InvoicePayment> paymentTableView;
    @FXML
    private TableColumn<InvoicePayment, LocalDate> paymentDateColumn;
    @FXML
    private TableColumn<InvoicePayment, BigDecimal> paymentAmountColumn;
    @FXML
    private Text partnerLabel, assetLabel;
    @FXML
    private Label stockLabel, paymentLabel;
    @FXML
    private Text totalText, vat25Text, vat13Text, vat05Text, vat00Text, totalWithVATText;
    @FXML
    private Text vat25BasisText, vat13BasisText, vat05BasisText, vat00BasisText;
    @FXML
    private Button addRowButton, cancelRowButton, deleteRowButton;
    @FXML
    private BigDecimalTextField paymentAmountField;
    @FXML
    private Button addPaymentRowButton, cancelPaymentRowButton, deletePaymentRowButton;
    @FXML
    private Button deleteButton;
    private Tab tab;
    private Optional<Business> selectedPartner;
    private Optional<Article> selectedArticle;
    private Optional<ArticleStock> selectedArticleStock;
    private Optional<Invoice> document;
    private NumberFormat numberFormatter;
    private ArticleRepository articleRepository;
    private BigDecimal invoiceTotalAmount;
    private InvoiceRepository invoiceRepository;

    @FXML
    private void initialize() {
        invoiceRepository = new InvoiceRepository();
        invoiceTotalAmount = new BigDecimal("0").setScale(2, RoundingMode.HALF_UP);
        selectedPartner = Optional.empty();
        selectedArticle = Optional.empty();
        selectedArticleStock = Optional.empty();
        document = Optional.empty();

        articleRepository = new ArticleRepository();

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
            invoiceTotalAmount = Invoice.getTotalAmountWithVAT(transactionTableView.getItems());
            totalText.setText(numberFormatter.format(Invoice.getTotalBasisAmount(transactionTableView.getItems())));

            vat25Text.setText(numberFormatter.format(Invoice.getTaxAmountByRate(transactionTableView.getItems(), TaxRate.REGULAR_RATE)));
            vat13Text.setText(numberFormatter.format(Invoice.getTaxAmountByRate(transactionTableView.getItems(), TaxRate.LOWER_RATE)));
            vat05Text.setText(numberFormatter.format(Invoice.getTaxAmountByRate(transactionTableView.getItems(), TaxRate.LOWEST_RATE)));
            vat00Text.setText(numberFormatter.format(Invoice.getTaxAmountByRate(transactionTableView.getItems(), TaxRate.TAX_FREE).add(Invoice.getTaxAmountByRate(transactionTableView.getItems(), TaxRate.TRANSIENT_RATE))));

            vat25BasisText.setText(numberFormatter.format(Invoice.getBasisAmountByTaxRate(transactionTableView.getItems(), TaxRate.REGULAR_RATE)));
            vat13BasisText.setText(numberFormatter.format(Invoice.getBasisAmountByTaxRate(transactionTableView.getItems(), TaxRate.LOWER_RATE)));
            vat05BasisText.setText(numberFormatter.format(Invoice.getBasisAmountByTaxRate(transactionTableView.getItems(), TaxRate.LOWEST_RATE)));
            vat00BasisText.setText(numberFormatter.format(Invoice.getBasisAmountByTaxRate(transactionTableView.getItems(), TaxRate.TAX_FREE).add(Invoice.getBasisAmountByTaxRate(transactionTableView.getItems(), TaxRate.TRANSIENT_RATE))));

            totalWithVATText.setText(numberFormatter.format(Invoice.getTotalAmountWithVAT(transactionTableView.getItems())));
        });

        paymentDateColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDate()));
        paymentDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd. MM. yyyy.")));
                }
            }
        });

        paymentAmountColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getAmount()));

        paymentTableView.getItems().addListener((ListChangeListener<? super InvoicePayment>) observable -> {
            paymentLabel.setText(numberFormatter.format(InvoicePayment.getTotalAmount(paymentTableView.getItems())));
        });

        invoiceIdField.focusedProperty().addListener((observableValue, unfocused, focused) -> {
            if (unfocused) {
                try {
                    Optional<Invoice> selectedDocument = invoiceRepository.get(invoiceIdField.getNumber());
                    selectedDocument.ifPresent(this::setFields);
                } catch (DatabaseException ex) {
                    MessageBox.show("Račun", "Pogreška u radu s bazom podataka", "Račun nije dohvaćen iz baze podataka!", ex);
                }
            }
        });

        quantitySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0));
        priceSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0));
        discountSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0));

        invoiceDatePicker.setValue(LocalDate.now());
        dueDatePicker.setValue(LocalDate.now());

        stockLabel.setVisible(false);
    }

    public void selectAction() throws IOException {
        try {
            Optional<Invoice> selection = SelectionDialog.showDialog("Račun", "Odabir računa", "invoice-selection-dialog.fxml");
            if (selection.isPresent()) {
                Optional<Invoice> selectedDocument = invoiceRepository.get(selection.get().getId());
                selectedDocument.ifPresent(this::setFields);
            }
        } catch (DatabaseException ex) {
            logger.error(ex.getMessage(), ex.getCause());
            MessageBox.show("Račun", "Greška u radu s bazom podataka", "Nije moguće dohvatiti odabrani račun!", ex);
        }
    }

    private void setFields(Invoice invoice) {
        document = Optional.of(invoice);
        invoiceIdField.setNumber(invoice.getId());
        invoiceDatePicker.setValue(invoice.getInvoiceDate());
        dueDatePicker.setValue(invoice.getDueDate());
        setTabTitle("Račun " + invoice.getId());

        selectedPartner = Optional.of(invoice.getPartner());
        partnerLabel.setText(invoice.getPartner().getName());

        for (ArticleTransaction transaction : invoice.getTransactions()) {
            if (transaction.getArticle() instanceof Asset asset) {
                try {
                    ArticleStock stock = articleRepository.getStock(asset, invoice.getInvoiceDate());
                    asset.setPurchasePrice(stock.getAveragePurchasePrice());
                } catch (DatabaseException e) {
                    asset.setPurchasePrice(BigDecimal.ZERO);
                }
            }
        }

        transactionTableView.getItems().clear();
        transactionTableView.getItems().addAll(FXCollections.observableArrayList(invoice.getTransactions()));

        paymentTableView.getItems().clear();
        paymentTableView.getItems().addAll(FXCollections.observableArrayList(invoice.getPayments()));

        deleteButton.setDisable(false);
    }

    public void selectPartner() throws IOException {
        Optional<Business> selection = SelectionDialog.showDialog("Partner", "Odabir partnera", "partner-selection-dialog.fxml");
        if (selection.isPresent()) {
            selectedPartner = selection;
            partnerLabel.setText(selection.get().getName());

            if(Optional.ofNullable(invoiceDatePicker.getValue()).isPresent()){
                dueDatePicker.setValue(invoiceDatePicker.getValue().plusDays(selection.get().getPaymentTerm()));
            }
        } else {
            selectedPartner = Optional.empty();
            partnerLabel.setText("-");
        }
    }

    public void selectArticle() throws IOException {
        Optional<Article> selection = SelectionDialog.showDialog("Artikli", "Odabir stavke", "article-selection-dialog.fxml");
        if (selection.isPresent()) {
            selectedArticle = selection;
            assetLabel.setText(selection.get().getName());
            cancelRowButton.setDisable(false);

            priceSpinner.getValueFactory().setValue(selection.get().getPrice().doubleValue());

            if (selection.get() instanceof Asset asset) {
                try {
                    setAssetStock(asset);
                } catch (DatabaseException e) {
                    throw new RuntimeException(e);
                }
            }
        } else resetRow();
    }

    private void setAssetStock(Asset asset) throws DatabaseException {
        LocalDate date = invoiceDatePicker.getValue();
        ArticleStock stock = articleRepository.getStock(asset, date);

        asset.setPurchasePrice(stock.getAveragePurchasePrice());

        stockLabel.setText("(stanje zaliha: " + stock.getStock() + ")");
        stockLabel.setVisible(true);
        selectedArticleStock = Optional.of(stock);
    }

    public void addRowToTable() {
        if (selectedArticle.isEmpty()) {
            MessageBox.show("Račun", "Artikl nije odabran", "Morate odabrati artikl da bi dodali stavku u račun!", Alert.AlertType.WARNING);
            return;
        }

        BigDecimal quantity = BigDecimal.valueOf(quantitySpinner.getValue()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal price = BigDecimal.valueOf(priceSpinner.getValue()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = BigDecimal.valueOf(discountSpinner.getValue()).setScale(2, RoundingMode.HALF_UP);

        StringBuilder validationError = new StringBuilder();

        if (quantity.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)))
            validationError.append("Količina ne smije biti 0!\n");
        if (price.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)))
            validationError.append("Cijena ne smije biti 0!\n");
        if (discount.compareTo(new BigDecimal("100.00")) > 0) validationError.append("Popust nije ispravan!\n");

        if (validationError.isEmpty()) {
            if (selectedArticleStock.isPresent()) {
                if (selectedArticleStock.get().getStock().compareTo(quantity) < 0) {
                    boolean confirmation = ConfirmationDialog.showDialog("Stanje zaliha", "Nedovoljne zalihe", "Trenutne zalihe nisu dovoljne! Želite li nastaviti s unosom?");
                    if (!confirmation) return;
                }
            }

            int index = transactionTableView.getSelectionModel().getSelectedIndex();
            ArticleTransaction transaction = new ArticleTransaction(selectedArticle.get(), quantity, discount, price);

            if (index == -1) transactionTableView.getItems().add(transaction);
            else transactionTableView.getItems().set(index, transaction);

            resetRow();
        } else {
            MessageBox.show("Račun", "Neispravno unesene vrijednosti zapisa!", validationError.toString(), Alert.AlertType.WARNING);
        }
    }

    public void deleteRow() {
        int index = transactionTableView.getSelectionModel().getSelectedIndex();

        if (index >= 0) {
            transactionTableView.getItems().remove(index);
            resetRow();
        }
    }

    public void resetRow() {
        selectedArticle = Optional.empty();
        assetLabel.setText("-");
        quantitySpinner.getValueFactory().setValue((double) 0);
        priceSpinner.getValueFactory().setValue((double) 0);
        discountSpinner.getValueFactory().setValue((double) 0);
        transactionTableView.getSelectionModel().clearSelection();
        addRowButton.setText("Dodaj stavku");
        deleteRowButton.setDisable(true);
        cancelRowButton.setDisable(true);

        selectedArticleStock = Optional.empty();
        stockLabel.setVisible(false);
    }

    public void setRow() {
        ArticleTransaction transaction = transactionTableView.getSelectionModel().getSelectedItem();

        if (Optional.ofNullable(transaction).isPresent()) {
            selectedArticle = Optional.of(transaction.getArticle());
            assetLabel.setText(transaction.getArticle().getName());
            quantitySpinner.getValueFactory().setValue(transaction.getQuantity().doubleValue());
            priceSpinner.getValueFactory().setValue(transaction.getPrice().doubleValue());
            discountSpinner.getValueFactory().setValue(transaction.getDiscount().doubleValue());
            addRowButton.setText("Ažuriraj stavku");
            deleteRowButton.setDisable(false);
            cancelRowButton.setDisable(false);

            if (transaction.getArticle() instanceof Asset asset) {
                try {
                    setAssetStock(asset);
                } catch (DatabaseException ex) {
                    asset.setPurchasePrice(BigDecimal.ZERO);
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    public void setPaymentRow() {
        InvoicePayment payment = paymentTableView.getSelectionModel().getSelectedItem();

        if (Optional.ofNullable(payment).isPresent()) {
            paymentDatePicker.setValue(payment.getDate());
            paymentAmountField.setNumber(payment.getAmount());

            addPaymentRowButton.setText("Ažuriraj stavku");
            deletePaymentRowButton.setDisable(false);
            cancelPaymentRowButton.setDisable(false);
        }
    }

    public void resetPaymentRow() {
        paymentTableView.getSelectionModel().clearSelection();
        paymentDatePicker.setValue(null);
        paymentAmountField.setNumber(new BigDecimal("0").setScale(2, RoundingMode.HALF_UP));
        addPaymentRowButton.setText("Dodaj stavku");
        deletePaymentRowButton.setDisable(true);
        cancelPaymentRowButton.setDisable(true);
    }

    public void deletePaymentRow() {
        int index = paymentTableView.getSelectionModel().getSelectedIndex();

        if (index >= 0) {
            paymentTableView.getItems().remove(index);
            resetPaymentRow();
        }
    }

    public void addPaymentRowToTable() {
        BigDecimal amount = paymentAmountField.getNumber();
        LocalDate date = paymentDatePicker.getValue();

        StringBuilder validationError = new StringBuilder();

        if (amount.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) || amount.compareTo(invoiceTotalAmount) > 0)
            validationError.append("Iznos uplate ne smije biti veći od iznosa računa!\n");
        if (Optional.ofNullable(date).isEmpty()) validationError.append("Datum uplate mora biti unesen!\n");

        if (validationError.isEmpty()) {
            int index = paymentTableView.getSelectionModel().getSelectedIndex();
            InvoicePayment payment = new InvoicePayment(date, amount);

            if (index == -1) paymentTableView.getItems().add(payment);
            else paymentTableView.getItems().set(index, payment);

            resetPaymentRow();
        } else {
            MessageBox.show("Račun-otpremnica", "Neispravnost uplata", validationError.toString(), Alert.AlertType.WARNING);
        }
    }

    public void saveAction() {
        LocalDate date = invoiceDatePicker.getValue();
        LocalDate dueDate = dueDatePicker.getValue();

        List<ArticleTransaction> transactions = transactionTableView.getItems().parallelStream().toList();
        BigDecimal amount = Invoice.getTotalAmountWithVAT(transactions);
        List<InvoicePayment> payments = paymentTableView.getItems().parallelStream().toList();
        BigDecimal paymentAmount = InvoicePayment.getTotalAmount(payments);

        StringBuilder validationError = new StringBuilder();

        if (selectedPartner.isEmpty()) {
            validationError.append("Partner nije odabran!\n");
        }
        if (Optional.ofNullable(date).isEmpty()) {
            validationError.append("Datum računa nije naveden!\n");
        }
        if (Optional.ofNullable(dueDate).isEmpty()) {
            validationError.append("Datum dospijeća računa nije naveden!\n");
        }
        if (selectedArticle.isPresent()) {
            validationError.append("Redak u tablici zapisa nije dovršen!!!\n");
        }
        if (transactions.isEmpty()) {
            validationError.append("Račun ne sadrži niti jedan zapis!\n");
        }
        if (paymentAmount.compareTo(amount) > 0) {
            validationError.append("Ukupan iznos plaćanja je veći od iznosa računa!\n");
        }

        if (validationError.isEmpty()) {
            InvoiceBuilder builder = new InvoiceBuilder();
            builder.setPartner(selectedPartner.get());
            builder.setInvoiceDate(date);
            builder.setDueDate(dueDate);
            builder.setTransactions(transactions);
            builder.setPayments(payments);

            boolean confirmation = ConfirmationDialog.showDialog("Račun", "Spremanje računa", "Želite li nastaviti sa spremanjem računa?");
            if (!confirmation) return;

            try {
                Long documentId = document.map(Entity::getId).orElse(invoiceIdField.getNumber());

                if (document.isPresent()) {
                    Invoice invoice = builder.setId(document.get().getId()).build();
                    invoiceRepository.update(documentId, invoice);
                    new HistoryWriterThread<>(new UpdatedChangeRecord<>(document.get(), invoice)).start();
                } else {
                    Invoice invoice = builder.build();

                    if (documentId.equals(0L)) {
                        documentId = invoiceRepository.save(invoice);
                    } else {
                        invoiceRepository.save(documentId, invoice);
                    }
                    new HistoryWriterThread<>(new CreatedChangeRecord<>(invoice)).start();
                }

                MessageBox.show("Račun-otpremnica", "Uspješno spremanje", "Račun " + documentId + " je uspješno spremljen u bazu podataka!");
                resetFields();
            } catch (DatabaseException ex) {
                logger.error(ex.getMessage(), ex.getCause());
                MessageBox.show("Račun", "Greška u radu s bazom podataka", "Spremanje računa nije uspjelo!", ex);
            }

        } else MessageBox.show("Račun", "Greška", validationError.toString(), Alert.AlertType.ERROR);
    }

    public void cancelEdit() {
        if (document.isEmpty() || transactionTableView.getItems().isEmpty()) return;

        boolean confirmation = ConfirmationDialog.showDialog("Račun", "Odustajanje", "Jeste li sigurni da želite odustati od uređivanja računa?");
        if (confirmation) {
            resetRow();
            resetFields();
            resetPaymentRow();
        }
    }

    public void deleteAction() {
        if (document.isPresent()) {
            try {
                boolean confirmation = ConfirmationDialog.showDialog("Račun-otpremnica", "Brisanje računa", "Jeste li sigurni da želite obrisati Račun-otpremnicu " + document.get().getId() + "?");
                if (confirmation) {
                    invoiceRepository.delete(document.get());
                    MessageBox.show("Račun-otpremnica", "Brisanje računa", "Račun-otpremnica " + document.get().getId() + " je uspješno obrisana iz baze podataka.");

                    resetRow();
                    resetFields();
                }
            } catch (DatabaseException ex) {
                logger.error(ex.getMessage(), ex.getCause());
                MessageBox.show("Račun-otpremnica", "Brisanje računa", "Račun-otpremnica " + document.get().getId() + " nije obrisana iz baze podataka!", ex);
            }
        }
    }

    private void resetFields() {
        invoiceIdField.setNumber(0L);
        invoiceDatePicker.setValue(null);
        setTabTitle("Račun");
        document = Optional.empty();

        selectedPartner = Optional.empty();
        partnerLabel.setText("-");
        invoiceDatePicker.setValue(LocalDate.now());
        dueDatePicker.setValue(LocalDate.now());

        transactionTableView.getItems().clear();
        paymentTableView.getItems().clear();
        deleteButton.setDisable(true);
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
