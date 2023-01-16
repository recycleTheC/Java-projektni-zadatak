package hr.java.projekt.model.company;

import hr.java.projekt.model.business.Business;

public class BasicCompanyData extends Business {
    private String description;
    private Boolean registeredForVAT;

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
}
