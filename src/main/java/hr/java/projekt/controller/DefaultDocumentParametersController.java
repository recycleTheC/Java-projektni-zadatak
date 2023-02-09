/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.controller;

import hr.java.projekt.app.MainApplication;
import hr.java.projekt.database.AccountRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.account.Account;
import hr.java.projekt.model.account.AccountParameters;
import hr.java.projekt.util.dialog.MessageBox;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class DefaultDocumentParametersController {
    private static final Logger logger = LoggerFactory.getLogger(DefaultDocumentParametersController.class);
    @FXML
    private ComboBox<Account> assetInputStockComboBox;
    @FXML
    private ComboBox<Account> assetInputSupplierComboBox;
    @FXML
    private ComboBox<Account> invoiceOutputBuyerComboBox;
    @FXML
    private ComboBox<Account> invoiceOutputVATComboBox;
    @FXML
    private ComboBox<Account> invoiceOutputStockComboBox;
    @FXML
    private ComboBox<Account> invoiceOutputPurchaseComboBox;
    @FXML
    private ComboBox<Account> invoiceOutputRevenueComboBox;
    @FXML
    private ComboBox<Account> invoiceBuyerComboBox;
    @FXML
    private ComboBox<Account> invoiceVATComboBox;
    @FXML
    private ComboBox<Account> invoiceRevenueComboBox;
    private List<Account> accounts;

    @FXML
    private void initialize() {
        AccountRepository accountRepository = new AccountRepository();
        try {
            accounts = accountRepository.getMany();

            assetInputStockComboBox.setItems(FXCollections.observableArrayList(accounts));
            assetInputSupplierComboBox.setItems(FXCollections.observableArrayList(accounts));
            invoiceOutputBuyerComboBox.setItems(FXCollections.observableArrayList(accounts));
            invoiceOutputVATComboBox.setItems(FXCollections.observableArrayList(accounts));
            invoiceOutputStockComboBox.setItems(FXCollections.observableArrayList(accounts));
            invoiceOutputPurchaseComboBox.setItems(FXCollections.observableArrayList(accounts));
            invoiceOutputRevenueComboBox.setItems(FXCollections.observableArrayList(accounts));
            invoiceBuyerComboBox.setItems(FXCollections.observableArrayList(accounts));
            invoiceVATComboBox.setItems(FXCollections.observableArrayList(accounts));
            invoiceRevenueComboBox.setItems(FXCollections.observableArrayList(accounts));

            Optional<String> assetInputStockParameter = MainApplication.getAccountParameter(AccountParameters.ASSET_INPUT_STOCK);
            setSelectedAccount(assetInputStockComboBox, assetInputStockParameter);

            Optional<String> assetInputSupplierParameter = MainApplication.getAccountParameter(AccountParameters.ASSET_INPUT_SUPPLIER);
            setSelectedAccount(assetInputSupplierComboBox, assetInputSupplierParameter);

            Optional<String> invoiceOutputBuyerParameter = MainApplication.getAccountParameter(AccountParameters.INVOICE_OUTPUT_BUYER);
            setSelectedAccount(invoiceOutputBuyerComboBox, invoiceOutputBuyerParameter);

            Optional<String> invoiceOutputStockParameter = MainApplication.getAccountParameter(AccountParameters.INVOICE_OUTPUT_STOCK);
            setSelectedAccount(invoiceOutputStockComboBox, invoiceOutputStockParameter);

            Optional<String> invoiceOutputPurchaseParameter = MainApplication.getAccountParameter(AccountParameters.INVOICE_OUTPUT_PURCHASE_PRICE);
            setSelectedAccount(invoiceOutputPurchaseComboBox, invoiceOutputPurchaseParameter);

            Optional<String> invoiceOutputRevenueParameter = MainApplication.getAccountParameter(AccountParameters.INVOICE_OUTPUT_REVENUE);
            setSelectedAccount(invoiceOutputRevenueComboBox, invoiceOutputRevenueParameter);

            Optional<String> invoiceOutputVATParameter = MainApplication.getAccountParameter(AccountParameters.INVOICE_OUTPUT_VAT);
            setSelectedAccount(invoiceOutputVATComboBox, invoiceOutputVATParameter);

            Optional<String> invoiceBuyerParameter = MainApplication.getAccountParameter(AccountParameters.INVOICE_BUYER);
            setSelectedAccount(invoiceBuyerComboBox, invoiceBuyerParameter);

            Optional<String> invoiceVATParameter = MainApplication.getAccountParameter(AccountParameters.INVOICE_VAT);
            setSelectedAccount(invoiceVATComboBox, invoiceVATParameter);

            Optional<String> invoiceRevenueParameter = MainApplication.getAccountParameter(AccountParameters.INVOICE_REVENUE);
            setSelectedAccount(invoiceRevenueComboBox, invoiceRevenueParameter);
        } catch (DatabaseException ex) {
            logger.error(ex.getMessage(), ex.getCause());
            MessageBox.show("Parametri dokumenata", "Greška pri radu s bazom podataka", "Dogodila se pogreška pri dohvatu kontnog plana iz baze podataka!", ex);
        }
    }

    @FXML
    private void save() throws IOException {
        Path parametersPath = Path.of("files/parameters.properties");
        Properties parameters = new Properties();
        parameters.load(new FileInputStream(parametersPath.toFile()));

        setParametersValue(parameters, AccountParameters.ASSET_INPUT_STOCK, assetInputStockComboBox);
        setParametersValue(parameters, AccountParameters.ASSET_INPUT_SUPPLIER, assetInputSupplierComboBox);
        setParametersValue(parameters, AccountParameters.INVOICE_OUTPUT_BUYER, invoiceOutputBuyerComboBox);
        setParametersValue(parameters, AccountParameters.INVOICE_OUTPUT_VAT, invoiceOutputVATComboBox);
        setParametersValue(parameters, AccountParameters.INVOICE_OUTPUT_STOCK, invoiceOutputStockComboBox);
        setParametersValue(parameters, AccountParameters.INVOICE_OUTPUT_PURCHASE_PRICE, invoiceOutputPurchaseComboBox);
        setParametersValue(parameters, AccountParameters.INVOICE_OUTPUT_REVENUE, invoiceOutputRevenueComboBox);
        setParametersValue(parameters, AccountParameters.INVOICE_BUYER, invoiceBuyerComboBox);
        setParametersValue(parameters, AccountParameters.INVOICE_VAT, invoiceVATComboBox);
        setParametersValue(parameters, AccountParameters.INVOICE_REVENUE, invoiceRevenueComboBox);

        parameters.save(new FileOutputStream(parametersPath.toFile()), null);
        MessageBox.show("Parametri dokumenata", "Spremanje parametara", "Parametri uspješno spremljeni");

        MainApplication.loadParameters();
    }

    private void setParametersValue(Properties properties, AccountParameters parameter, ComboBox<Account> selectedAccount){
        if (Optional.ofNullable(selectedAccount.getValue()).isPresent())
            properties.setProperty(parameter.name(), selectedAccount.getValue().getCode());
    }

    private void setSelectedAccount(ComboBox<Account> comboBox, Optional<String> parameter) {
        if (parameter.isPresent()) {
            String code = parameter.get();
            Optional<Account> selectedAccount = accounts.parallelStream().filter(account -> account.getCode().equals(code)).findFirst();
            selectedAccount.ifPresent(comboBox::setValue);
        }
    }
}
