package hr.java.projekt.model.business;

import hr.java.projekt.util.validation.iban.IBANValidationException;
import hr.java.projekt.util.validation.iban.IBANValidator;
import hr.java.projekt.util.validation.oib.OIBValidationException;
import hr.java.projekt.util.validation.oib.OIBValidator;

public class BusinessBuilder {
    private Long id;
    private String name;
    private String address;
    private String postalCodeAndTown;
    private String oib;
    private String iban;

    public BusinessBuilder(Long id) {
        this.id = id;
    }

    public BusinessBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public BusinessBuilder setAddress(String address) {
        this.address = address;
        return this;
    }

    public BusinessBuilder setPostalCodeAndTown(String postalCodeAndTown) {
        this.postalCodeAndTown = postalCodeAndTown;
        return this;
    }

    public BusinessBuilder setOIB(String oib) throws OIBValidationException {
        if(OIBValidator.validate(oib)){
            this.iban = iban;
        }
        return this;
    }

    public BusinessBuilder setIBAN(String iban) throws IBANValidationException {
        if(IBANValidator.validate(iban)){
            this.iban = iban;
        }
        return this;
    }

    public Business build() {
        return new Business(id, name, address, postalCodeAndTown, oib, iban);
    }
}