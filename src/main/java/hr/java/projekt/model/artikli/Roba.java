package hr.java.projekt.model.artikli;

import hr.java.projekt.model.StopaPDVa;
import java.math.BigDecimal;

public class Roba extends Artikl {
    private BigDecimal nabavnaCijena;
    private BigDecimal stanjeZaliha;

    public Roba(String sifra, String naziv, String mjernaJedinica, BigDecimal cijena, BigDecimal cijenaPDV, StopaPDVa stopaPDVa, BigDecimal nabavnaCijena) {
        super(sifra, naziv, mjernaJedinica, cijena, cijenaPDV, stopaPDVa);
        this.nabavnaCijena = nabavnaCijena;
    }

    public BigDecimal getNabavnaCijena() {
        return nabavnaCijena;
    }

    public void setNabavnaCijena(BigDecimal nabavnaCijena) {
        this.nabavnaCijena = nabavnaCijena;
    }

    public BigDecimal getStanjeZaliha() {
        return stanjeZaliha;
    }

    public void setStanjeZaliha(BigDecimal stanjeZaliha) {
        this.stanjeZaliha = stanjeZaliha;
    }
}
