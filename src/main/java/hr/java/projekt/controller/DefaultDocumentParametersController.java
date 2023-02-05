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
    private List<Account> accounts;

    @FXML
    private void initialize() {
        AccountRepository accountRepository = new AccountRepository();
        try {
            accounts = accountRepository.getMany();
            assetInputStockComboBox.setItems(FXCollections.observableArrayList(accounts));
            assetInputSupplierComboBox.setItems(FXCollections.observableArrayList(accounts));

            Optional<String> assetInputStockParameter = MainApplication.getAccountParameter(AccountParameters.ASSET_INPUT_STOCK);
            setSelectedAccount(assetInputStockComboBox, assetInputStockParameter);

            Optional<String> assetInputSupplierParameter = MainApplication.getAccountParameter(AccountParameters.ASSET_INPUT_SUPPLIER);
            setSelectedAccount(assetInputSupplierComboBox, assetInputSupplierParameter);
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

        if (Optional.ofNullable(assetInputStockComboBox.getValue()).isPresent())
            parameters.setProperty(AccountParameters.ASSET_INPUT_STOCK.name(), assetInputStockComboBox.getValue().getCode());

        if (Optional.ofNullable(assetInputSupplierComboBox.getValue()).isPresent())
            parameters.setProperty(AccountParameters.ASSET_INPUT_SUPPLIER.name(), assetInputSupplierComboBox.getValue().getCode());

        parameters.save(new FileOutputStream(parametersPath.toFile()), null);

        MainApplication.loadParameters();
    }

    private void setSelectedAccount(ComboBox<Account> comboBox, Optional<String> parameter) {
        if (parameter.isPresent()) {
            String code = parameter.get();
            Optional<Account> selectedAccount = accounts.parallelStream().filter(account -> account.getCode().equals(code)).findFirst();
            selectedAccount.ifPresent(comboBox::setValue);
        }
    }
}
