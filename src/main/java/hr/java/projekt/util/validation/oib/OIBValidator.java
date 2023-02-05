package hr.java.projekt.util.validation.oib;

import hr.java.projekt.exceptions.OIBValidationException;

public class OIBValidator {
    public static boolean validate(String oib) {
        if (oib.length() != 11) throw new OIBValidationException("OIB nije ispravne duljine!");

        char[] digits = oib.toCharArray();
        int check = 10;

        for (int i = 0; i < 10; i++) {
            char digit = digits[i];
            if (digit < '0' || digit > '9')
                throw new OIBValidationException("OIB sadrži nedopuštene znakove! " + oib);

            check += digit - '0';
            check %= 10;

            if (check == 0) check = 10;

            check *= 2;
            check %= 11;
        }

        int checksum = 11 - check;
        checksum %= 10;

        if (checksum != (digits[10] - '0'))
            throw new OIBValidationException("Kontrolni broj OIB-a je neispravan! " + oib);

        return true;
    }
}
