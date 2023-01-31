package hr.java.projekt.controller;

import hr.java.projekt.model.history.ChangeHistoryRecordFiles;
import hr.java.projekt.model.history.CreatedChangeRecord;
import hr.java.projekt.model.history.UpdatedChangeRecord;
import hr.java.projekt.model.company.BasicCompanyData;
import hr.java.projekt.util.dialog.MessageBox;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private BasicCompanyData tvrtka;
    private static final Logger logger = LoggerFactory.getLogger(BasicCompanyDataController.class);

    public void initialize() {
        try {
            tvrtka = BasicCompanyDataFile.read();
        }
        catch (IOException | IBANValidationException | OIBValidationException ex){
            logger.error(ex.getMessage(), ex);
            MessageBox.show("Osnovni podatci", "KRITIČNA POGREŠKA", "Datoteka s osnovnim podatcima o tvrtki sadrži kritičnu pogrešku i nastavak rada nije moguć!\nPopravite datoteku ili prepišite sadržaj datoteke u nastavku!\n", ex);
            tvrtka = new BasicCompanyData();
        }

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
                checkOIBField();
            }
        });

        ibanField.focusedProperty().addListener((observableValue, unfocused, focused) -> {
            if(unfocused){
                checkIBANField();
            }
        });
    }
    public void checkOIBField(){
        String oib = oibField.getText();

        try {
            if(!oib.isBlank()) {
                OIBValidator.validate(oib);
                oibField.setStyle("-fx-text-fill: green;");
            }
        } catch (OIBValidationException e) {
            MessageBox.show("Validacija OIB-a", "Pogrešan OIB", e.getMessage(), Alert.AlertType.ERROR);
            oibField.setStyle("-fx-text-fill: red;");
            oibField.requestFocus();
        }
    }

    public void checkIBANField(){
        String iban = ibanField.getText();

        try {
            if(!iban.isBlank()){
                IBANValidator.validate(iban);
                ibanField.setStyle("-fx-text-fill: green;");
            }
        } catch (IBANValidationException e) {
            MessageBox.show("Validacija IBAN-a", "Pogrešan IBAN", e.getMessage(), Alert.AlertType.ERROR);
            ibanField.setStyle("-fx-text-fill: red;");
            ibanField.requestFocus();
        }
    }

    public void saveData(){
        BasicCompanyData oldData = tvrtka;
        String naziv = nazivField.getText();
        String adresa = adresaField.getText();
        String postanskiBrojMjesto = postanskiBrojMjestoField.getText();
        String iban = ibanField.getText();
        String oib = oibField.getText();
        String opis = ispisPodatciField.getText();

        StringBuilder ispisPogreske = new StringBuilder();

        if(naziv.isBlank()) {
            ispisPogreske.append("Nije upisan naziv tvrtke!\n");
        }
        if(adresa.isBlank()){
            ispisPogreske.append("Nije upisana adresa tvrtke!\n");
        }
        if(postanskiBrojMjesto.isBlank()){
            ispisPogreske.append("Nisu upisani poštanski broj i/ili mjesto tvrtke!\n");
        }
        if(oib.isBlank()){
            ispisPogreske.append("Nije upisan OIB tvrtke!\n");
        }
        if(oib.isBlank()){
            ispisPogreske.append("Nije upisan IBAN žiro računa tvrtke!\n");
        }

        if(ispisPogreske.isEmpty()){
            tvrtka = new BasicCompanyData(naziv, adresa, postanskiBrojMjesto);
            tvrtka.setDescription(opis);
            tvrtka.setRegisteredForVAT(uSustavuPDVaBox.getValue().compareTo("da") == 0);

            try {
                tvrtka.setOIB(oib);
                tvrtka.setIBAN(iban);

                BasicCompanyDataFile.write(tvrtka);

                if(oldData != null) ChangeHistoryRecordFiles.write(new UpdatedChangeRecord<>(oldData, tvrtka));
                else ChangeHistoryRecordFiles.write(new CreatedChangeRecord<>(tvrtka));

                MessageBox.show("Osnovni podatci", "Upis podataka", "Podatci su spremljeni!", Alert.AlertType.INFORMATION);
            }
            catch (IOException e){
                MessageBox.show("Osnovni podatci", "Upis podataka", "Podatci nisu spremljeni", Alert.AlertType.ERROR);
                logger.error(e.getMessage(), e.getCause());
            }
            catch (IBANValidationException | OIBValidationException ex){
                MessageBox.show("Osnovni podatci", "Upis podataka", "Podatci nisu spremljeni zbog pogreške u validaciji podataka!\n" + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
        else {
            MessageBox.show("Validacija podataka", "Nedostatak podataka", ispisPogreske.toString(), Alert.AlertType.ERROR);
        }
    }
}
