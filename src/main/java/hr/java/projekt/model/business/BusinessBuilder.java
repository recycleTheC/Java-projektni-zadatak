package hr.java.projekt.model.business;

import hr.java.projekt.exceptions.IBANValidationException;
import hr.java.projekt.util.validation.iban.IBANValidator;
import hr.java.projekt.exceptions.OIBValidationException;
import hr.java.projekt.util.validation.oib.OIBValidator;

public class BusinessBuilder {
    private Long id, paymentTerm;
    private String name;
    private String address;
    private String postalCodeAndTown;
    private String oib;
    private String iban;

    public BusinessBuilder(Long id) {
        this.id = id;
    }
    public BusinessBuilder() {}

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
            this.oib = oib;
        }
        return this;
    }

    public BusinessBuilder setUncheckedOIB(String oib) {
        this.oib = oib;
        return this;
    }

    public BusinessBuilder setIBAN(String iban) throws IBANValidationException {
        if(IBANValidator.validate(iban)){
            this.iban = iban;
        }
        return this;
    }

    public BusinessBuilder setUncheckedIBAN(String iban) {
        this.iban = iban;
        return this;
    }

    public BusinessBuilder setPaymentTerm(Long term) {
        this.paymentTerm = term;
        return this;
    }
    public Business build() {
        Business business = new Business(id, name, address, postalCodeAndTown, oib, iban);
        business.setPaymentTerm(paymentTerm);
        return business;
    }
}