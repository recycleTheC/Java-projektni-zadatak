package hr.java.projekt.controller;

import hr.java.projekt.model.company.BasicCompanyData;
import hr.java.projekt.util.files.BasicCompanyDataFile;
import hr.java.projekt.util.validation.iban.IBANValidationException;
import hr.java.projekt.util.validation.iban.IBANValidator;
import hr.java.projekt.util.validation.oib.OIBValidationException;
import hr.java.projekt.util.validation.oib.OIBValidator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.List;

public class BasicCompanyDataController {
    @FXML
    private TextField nazivField;
    @FXML
    private TextField adresaField;
    @FXML
    private TextField postanskiBrojMjestoField;
    @FXML
    private TextField oibField;
    @FXML
    private TextField ibanField;
    @FXML
    private TextArea ispisPodatciField;
    @FXML
    private ChoiceBox<String> uSustavuPDVaBox;

    BasicCompanyData tvrtka;

    public void initialize() throws IOException {
        tvrtka = BasicCompanyDataFile.read();

        nazivField.setText(tvrtka.getName());
        adresaField.setText(tvrtka.getAddress());
        postanskiBrojMjestoField.setText(tvrtka.getPostalCodeAndTown());
        oibField.setText(tvrtka.getOIB());
        ibanField.setText(tvrtka.getIBAN());
        ispisPodatciField.setText(tvrtka.getDescription());

        uSustavuPDVaBox.setItems(FXCollections.observableList(List.of("da", "ne")));
        uSustavuPDVaBox.setValue(tvrtka.getRegisteredForVAT() ? "da" : "ne");

        oibField.focusedProperty().addListener((observableValue, unfocused, focused) -> {
            if(unfocused){
                provjeriUpisaniOib();
            }
        });

        ibanField.focusedProperty().addListener((observableValue, unfocused, focused) -> {
            if(unfocused){
                provjeriUpisaniIBAN();
            }
        });
    }
    public void provjeriUpisaniOib(){
        String oib = oibField.getText();

        try {
            OIBValidator.validate(oib);
            oibField.setStyle("-fx-text-fill: green;");
        } catch (OIBValidationException e) {
            Alert invalidOIBAlert = new Alert(Alert.AlertType.ERROR);
            invalidOIBAlert.setTitle("Validacija OIB-a");
            invalidOIBAlert.setContentText(e.getMessage());
            invalidOIBAlert.setHeaderText("Pogrešan OIB");
            invalidOIBAlert.show();

            oibField.setStyle("-fx-text-fill: red;");
            oibField.requestFocus();
        }
    }

    public void provjeriUpisaniIBAN(){
        String iban = ibanField.getText();

        try {
            IBANValidator.validate(iban);
            ibanField.setStyle("-fx-text-fill: green;");
        } catch (IBANValidationException e) {
            Alert invalidOIBAlert = new Alert(Alert.AlertType.ERROR);
            invalidOIBAlert.setTitle("Validacija IBAN-a");
            invalidOIBAlert.setContentText(e.getMessage());
            invalidOIBAlert.setHeaderText("Pogrešan IBAN");
            invalidOIBAlert.show();

            ibanField.setStyle("-fx-text-fill: red;");
            ibanField.requestFocus();
        }
    }

    public void spremiPodatke(){
        String naziv = nazivField.getText();
        String adresa = adresaField.getText();
        String postanskiBrojMjesto = postanskiBrojMjestoField.getText();
        String iban = ibanField.getText();
        String oib = oibField.getText();
        String opis = ispisPodatciField.getText();

        boolean spremiPodatke = true, uspjeh;
        String ispisPogreske = "";

        if(naziv.isBlank()) {
            spremiPodatke = false;
            ispisPogreske += "Nije upisan naziv tvrtke!\n";
        }

        if(adresa.isBlank()){
            spremiPodatke = false;
            ispisPogreske += "Nije upisana adresa tvrtke!\n";
        }

        if(postanskiBrojMjesto.isBlank()){
            spremiPodatke = false;
            ispisPogreske += "Nisu upisani poštanski broj i/ili mjesto tvrtke!\n";
        }

        if(oib.isBlank()){
            spremiPodatke = false;
            ispisPogreske += "Nije upisan OIB tvrtke!\n";
        }

        if(oib.isBlank()){
            spremiPodatke = false;
            ispisPogreske += "Nije upisan IBAN žiro računa tvrtke!\n";
        }

        if(spremiPodatke){
            tvrtka = new BasicCompanyData(naziv, adresa, postanskiBrojMjesto);
            tvrtka.setDescription(opis);
            tvrtka.setRegisteredForVAT(uSustavuPDVaBox.getValue().compareTo("da") == 0);
            try {
                tvrtka.setOIB(oib);
                tvrtka.setIBAN(iban);

                BasicCompanyDataFile.write(tvrtka);
                uspjeh = true;
            }
            catch (IBANValidationException | OIBValidationException | IOException e){
                System.out.println(e.getMessage());
                uspjeh = false;
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validacija podataka");
            alert.setContentText(ispisPogreske);
            alert.setHeaderText("Nedostatak podataka");
            alert.show();
            uspjeh = false;
        }

        Alert alert = new Alert(uspjeh ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle("Osnovni podatci");
        alert.setContentText(uspjeh ? "Podatci su spremljeni!" : "Podatci nisu spremljeni");
        alert.setHeaderText("Upis podataka");
        alert.show();

    }
}
