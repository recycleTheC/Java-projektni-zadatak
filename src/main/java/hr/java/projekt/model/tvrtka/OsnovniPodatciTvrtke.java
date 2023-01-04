package hr.java.projekt.model.tvrtka;

import hr.java.projekt.util.validation.iban.IBANValidationException;
import hr.java.projekt.util.validation.iban.IBANValidator;
import hr.java.projekt.util.validation.oib.OIBValidationException;
import hr.java.projekt.util.validation.oib.OIBValidator;
import javafx.fxml.FXML;

public class OsnovniPodatciTvrtke {
    private String naziv, adresa, mjestoPostanskiBroj, oib, iban, opis;

    public static String PUTANJA_DATOTEKE = "files/osnovni_podatci.txt";

    public OsnovniPodatciTvrtke() {
    }

    public OsnovniPodatciTvrtke(String naziv, String adresa, String mjestoPostanskiBroj) {
        this.naziv = naziv;
        this.adresa = adresa;
        this.mjestoPostanskiBroj = mjestoPostanskiBroj;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public String getMjestoPostanskiBroj() {
        return mjestoPostanskiBroj;
    }

    public void setMjestoPostanskiBroj(String mjestoPostanskiBroj) {
        this.mjestoPostanskiBroj = mjestoPostanskiBroj;
    }

    public String getOIB() {
        return oib;
    }

    public void setOIB(String oib) throws OIBValidationException {
        if(OIBValidator.validate(oib)){
            this.oib = oib;
        }
    }

    public String getIBAN() {
        return iban;
    }

    public void setIBAN(String iban) throws IBANValidationException {
        if(IBANValidator.validate(iban)){
            this.iban = iban;
        }
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }
}
