package hr.java.projekt.controller;

import hr.java.projekt.model.tvrtka.OsnovniPodatciTvrtke;
import hr.java.projekt.util.files.OsnovniPodatciTvrtkeFile;
import hr.java.projekt.util.validation.iban.IBANValidationException;
import hr.java.projekt.util.validation.iban.IBANValidator;
import hr.java.projekt.util.validation.oib.OIBValidationException;
import hr.java.projekt.util.validation.oib.OIBValidator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

public class OsnovniPodatciController {
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

    OsnovniPodatciTvrtke tvrtka;

    public void initialize() throws IOException {
        tvrtka = OsnovniPodatciTvrtkeFile.reader();

        nazivField.setText(tvrtka.getNaziv());
        adresaField.setText(tvrtka.getAdresa());
        postanskiBrojMjestoField.setText(tvrtka.getMjestoPostanskiBroj());
        oibField.setText(tvrtka.getOIB());
        ibanField.setText(tvrtka.getIBAN());
        ispisPodatciField.setText(tvrtka.getOpis());

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

        Boolean spremiPodatke = true, uspjeh = false;
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
            tvrtka = new OsnovniPodatciTvrtke(naziv, adresa, postanskiBrojMjesto);
            tvrtka.setOpis(opis);
            try {
                tvrtka.setOIB(oib);
                tvrtka.setIBAN(iban);

                OsnovniPodatciTvrtkeFile.writer(tvrtka);
            }
            catch (IBANValidationException | OIBValidationException | IOException e){
                System.out.println(e.getMessage());
                uspjeh = false;
            }

            uspjeh = true;
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
