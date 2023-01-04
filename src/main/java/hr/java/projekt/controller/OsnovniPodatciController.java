package hr.java.projekt.controller;

import hr.java.projekt.util.validation.oib.OIBValidationException;
import hr.java.projekt.util.validation.oib.OIBValidator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class OsnovniPodatciController {
    @FXML
    private TextField oibField;

    public void initialize(){
        oibField.focusedProperty().addListener((observableValue, unfocused, focused) -> {
            if(unfocused){
                provjeriUpisaniOib();
            }
        });
    }
    public void provjeriUpisaniOib(){
        String oib = oibField.getText();

        try {
            OIBValidator.validate(oib);
        } catch (OIBValidationException e) {
            Alert invalidOIBAlert = new Alert(Alert.AlertType.ERROR);
            invalidOIBAlert.setTitle("Validacija OIB-a");
            invalidOIBAlert.setContentText(e.getMessage());
            invalidOIBAlert.setHeaderText("Pogrešan OIB");
            invalidOIBAlert.show();
        }
    }
}
