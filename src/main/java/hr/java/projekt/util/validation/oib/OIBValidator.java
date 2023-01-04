package hr.java.projekt.util.validation.oib;

public class OIBValidator {
    public static boolean validate(String oib) throws OIBValidationException {
        if(oib.length() != 11) throw new OIBValidationException("OIB nije ispravne duljine!");

        char[] znamenke = oib.toCharArray();
        int provjera = 10;

        for(int i = 0; i < 10; i++) {
            char znamenka = znamenke[i];
            if(znamenka < '0' || znamenka > '9') throw new OIBValidationException("OIB sadrži nedopuštene znakove!");

            provjera += znamenka - '0';
            provjera %= 10;

            if(provjera == 0) provjera = 10;

            provjera *= 2;
            provjera %= 11;
        }

        int kontrolni = 11 - provjera;
        kontrolni %= 10;

        if(kontrolni != (znamenke[10] - '0')) throw new OIBValidationException("Kontrolni broj OIB-a je neispravan!");

        return true;
    }
}
