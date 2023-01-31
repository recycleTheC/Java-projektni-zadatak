package hr.java.projekt.model.company;

import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.history.WritableHistory;

import java.io.Serializable;

public class BasicCompanyData extends Business implements Serializable, WritableHistory {
    private String description;
    private Boolean registeredForVAT = false;

    public static final String FILE_PATH = "files/osnovni_podatci.txt";

    public BasicCompanyData() {
        super();
    }

    public BasicCompanyData(String name, String address, String postalCodeAndTown) {
        super(name, address, postalCodeAndTown);
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRegisteredForVAT() {
        return registeredForVAT;
    }

    public void setRegisteredForVAT(Boolean registeredForVAT) {
        this.registeredForVAT = registeredForVAT;
    }

    @Override
    public StringBuilder stringGenerator(){
        StringBuilder value = new StringBuilder("Osnovni podatci tvrtke\n");
        value.append("Naziv: ").append(this.getName()).append("\n");
        value.append("Adresa: ").append(this.getAddress()).append("\n");
        value.append("Poštanski broj i mjesto: ").append(this.getPostalCodeAndTown()).append("\n");
        value.append("U sustavu PDV-a: ").append(this.getRegisteredForVAT() ? "da" : "ne").append("\n");
        value.append("OIB: ").append(this.getOIB()).append("\n");
        value.append("IBAN: ").append(this.getIBAN()).append("\n\n");
        value.append("Opis: ").append(this.getDescription());

        return value;
    }

    @Override
    public String getIdentifier() {
        return this.getOIB();
    }
}
