package hr.java.projekt.model.business;

import hr.java.projekt.model.Entity;
import hr.java.projekt.model.history.WritableHistory;
import hr.java.projekt.exceptions.IBANValidationException;
import hr.java.projekt.util.validation.iban.IBANValidator;
import hr.java.projekt.exceptions.OIBValidationException;
import hr.java.projekt.util.validation.oib.OIBValidator;

public class Business extends Entity implements WritableHistory {
    private String name, address, postalCodeAndTown, oib, iban;
    private Long paymentTerm;

    public Business() {
        super();
    }

    Business(Long id, String name, String address, String postalCodeAndTown, String oib, String iban) {
        super(id);
        this.name = name;
        this.address = address;
        this.postalCodeAndTown = postalCodeAndTown;
        this.oib = oib;
        this.iban = iban;
    }

    public Business(String name, String address, String postalCodeAndTown) {
        super();
        this.name = name;
        this.address = address;
        this.postalCodeAndTown = postalCodeAndTown;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCodeAndTown() {
        return postalCodeAndTown;
    }

    public void setPostalCodeAndTown(String postalCodeAndTown) {
        this.postalCodeAndTown = postalCodeAndTown;
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
    public Long getPaymentTerm() {
        return paymentTerm;
    }

    public void setPaymentTerm(Long paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    @Override
    public StringBuilder stringGenerator() {
        StringBuilder builder = new StringBuilder("Partner ");
        builder.append(this.getId()).append("\n");
        builder.append("Naziv: ").append(this.getName()).append("\n");
        builder.append("Adresa: ").append(this.getAddress()).append("\n");
        builder.append("Poštanski broj, mjesto: ").append(this.getPostalCodeAndTown()).append("\n");
        builder.append("OIB: ").append(this.getOIB()).append("\n");
        builder.append("IBAN: ").append(this.getIBAN()).append("\n");
        builder.append("Odgoda plaćanja: ").append(this.getPaymentTerm()).append("\n");
        return builder;
    }

    @Override
    public String getIdentifier() {
        return this.getId().toString();
    }

    @Override
    public String getShortDescription() {
        return "Partner " + this.getId().toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
