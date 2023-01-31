package hr.java.projekt.util;

import hr.java.projekt.model.TaxRate;
import hr.java.projekt.model.account.AccountType;
import javafx.util.StringConverter;

import java.math.BigDecimal;

public class CustomStringConverters {
    public static final StringConverter<TaxRate> TaxRateStringConverter = new StringConverter<>() {
        @Override
        public String toString(TaxRate taxRate) {
            if(taxRate == null) return "";
            return taxRate.getDescription() + " (" + taxRate.getRate().multiply(BigDecimal.valueOf(100)).setScale(0) + "%)";
        }
        @Override
        public TaxRate fromString(String s) {
            if(s.contains(TaxRate.REGULAR_RATE.getDescription())) return TaxRate.REGULAR_RATE;
            else if(s.contains(TaxRate.LOWER_RATE.getDescription())) return TaxRate.LOWER_RATE;
            else if(s.contains(TaxRate.LOWEST_RATE.getDescription())) return TaxRate.LOWEST_RATE;
            else if(s.contains(TaxRate.TRANSIENT_RATE.getDescription())) return TaxRate.TRANSIENT_RATE;
            else return TaxRate.TAX_FREE;
        }
    };

    public static final StringConverter<AccountType> AccountTypeStringConverter = new StringConverter<>() {
        @Override
        public String toString(AccountType accountType) {
            if(accountType == null) return "";
            return accountType.getDescription();
        }

        @Override
        public AccountType fromString(String s) {
            return null;
        }
    };
}
