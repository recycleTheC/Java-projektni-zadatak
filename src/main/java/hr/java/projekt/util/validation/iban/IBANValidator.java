package hr.java.projekt.util.validation.iban;

import java.math.BigInteger;
import java.util.regex.Pattern;

public class IBANValidator {
    public static boolean validate(String iban) throws IBANValidationException {
        if(iban.length() != 21) throw new IBANValidationException("IBAN ima neispravnu duljinu!");

        if(!Pattern.matches("[A-Z]{2}[0-9]{3,21}", iban)) throw new IBANValidationException("IBAN nije u ispravnom formatu!");

        String kodZemlje = iban.substring(0,2);
        if(kodZemlje.compareTo("HR") != 0) throw new IBANValidationException("IBAN sadrži neispravnu ili stranu oznaku zemlje : " + kodZemlje);

        String kontrolniBroj = iban.substring(2,4);
        BigInteger upisaniKontrolniBroj = new BigInteger(kontrolniBroj);

        String ostatakIBANa = iban.substring(4,21) + "172700";
        BigInteger ostatak = new BigInteger(ostatakIBANa).mod(BigInteger.valueOf(97));
        BigInteger izracunatiKontrolniBroj = BigInteger.valueOf(97).add(BigInteger.ONE).subtract(ostatak);

        if(izracunatiKontrolniBroj.compareTo(upisaniKontrolniBroj) != 0) throw new IBANValidationException("Neispravan kontrolni broj IBAN-a!");

        return true;
    }
}
