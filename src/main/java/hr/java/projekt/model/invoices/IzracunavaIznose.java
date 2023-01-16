package hr.java.projekt.model.invoices;

import hr.java.projekt.model.TaxRate;

import java.math.BigDecimal;

public interface IzracunavaIznose {
    /**
     * Metoda za izračun ukupnog iznosa računa po stavkama
     * @return iznos računa
     */
    BigDecimal izracunajUkupanIznos();

    /**
     * Metoda za izračun osnovice po odabranoj stopi PDV-a
     * @return iznos osnovice po stopi PDV-a
     */
    BigDecimal izracunOsnovicePoStopi(TaxRate stopa);

    /**
     * Metoda za izračun poreza po odabranoj stopi PDV-a
     * @return iznos poreza po stopi PDV-a
     */
    BigDecimal izracunPorezaPoStopi(TaxRate stopa);
}
