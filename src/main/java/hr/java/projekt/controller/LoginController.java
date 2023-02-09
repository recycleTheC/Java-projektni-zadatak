/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.controller;

import hr.java.projekt.app.MainApplication;
import hr.java.projekt.database.OperatorRepository;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.exceptions.LoginException;
import hr.java.projekt.model.operator.Operator;
import hr.java.projekt.util.dialog.MessageBox;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginController {
    private static Logger logger = LoggerFactory.getLogger(LoginController.class);
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private OperatorRepository operatorRepository;

    @FXML
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        StringBuilder validationError = new StringBuilder();

        if (username.isBlank()) {
            validationError.append("Morate navesti korisničko ime!\n");
        }

        if (password.isBlank()) {
            validationError.append("Lozinka nije navedena!\n");
        }

        if (validationError.isEmpty()) {
            try {
                Operator operator = new OperatorRepository().login(username, password);
                MainApplication.operator = operator;
                MainApplication.startMain();
            } catch (DatabaseException | LoginException | IOException e) {
                logger.error(e.getMessage(), e);
                MessageBox.show("Prijava", "Pogreška", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
        else MessageBox.show("Prijava", "Pogreška", validationError.toString(), Alert.AlertType.WARNING);
    }
}
