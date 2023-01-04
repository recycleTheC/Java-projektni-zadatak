package hr.java.projekt.model.artikli;

import hr.java.projekt.model.Entitet;
import hr.java.projekt.model.StopaPDVa;

import java.math.BigDecimal;

public abstract class Artikl extends Entitet {
    private String naziv, mjernaJedinica;
    private BigDecimal cijena, cijenaPDV;
    private StopaPDVa stopaPDVa;

    public Artikl(String sifra, String naziv, String mjernaJedinica, BigDecimal cijena, BigDecimal cijenaPDV, StopaPDVa stopaPDVa) {
        super(sifra);
        this.naziv = naziv;
        this.mjernaJedinica = mjernaJedinica;
        this.cijena = cijena;
        this.cijenaPDV = cijenaPDV;
        this.stopaPDVa = stopaPDVa;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getMjernaJedinica() {
        return mjernaJedinica;
    }

    public void setMjernaJedinica(String mjernaJedinica) {
        this.mjernaJedinica = mjernaJedinica;
    }

    public BigDecimal getCijena() {
        return cijena;
    }

    public void setCijena(BigDecimal cijena) {
        this.cijena = cijena;
        this.cijenaPDV = cijena.add(cijena.multiply(stopaPDVa.getStopa()));
    }

    public BigDecimal getCijenaPDV() {
        return cijenaPDV;
    }

    public void setCijenaPDV(BigDecimal cijenaPDV) {
        this.cijenaPDV = cijenaPDV;
        this.cijena = cijena.divide(stopaPDVa.getStopa().add(BigDecimal.ONE));
    }

    public StopaPDVa getStopaPDVa() {
        return stopaPDVa;
    }

    public void setStopaPDVa(StopaPDVa stopaPDVa) {
        this.stopaPDVa = stopaPDVa;
        setCijena(this.cijena);
    }
}
