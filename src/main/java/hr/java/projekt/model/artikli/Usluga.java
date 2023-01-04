package hr.java.projekt.model.artikli;

import hr.java.projekt.model.StopaPDVa;
import java.math.BigDecimal;

public class Usluga extends Artikl {
    private BigDecimal trosakObavljanja;

    public Usluga(String sifra, String naziv, String mjernaJedinica, BigDecimal cijena, BigDecimal cijenaPDV, StopaPDVa stopaPDVa) {
        super(sifra, naziv, mjernaJedinica, cijena, cijenaPDV, stopaPDVa);
    }

    public BigDecimal getTrosakObavljanja() {
        return trosakObavljanja;
    }

    public void setTrosakObavljanja(BigDecimal trosakObavljanja) {
        this.trosakObavljanja = trosakObavljanja;
    }
}
