package hr.java.projekt.model;

import java.math.BigDecimal;

public enum TaxRate {
    REGULAR_RATE("redovna stopa", new BigDecimal("0.25")),
    LOWER_RATE("snižena stopa", new BigDecimal("0.13")),
    LOWEST_RATE("najniža stopa", new BigDecimal("0.05")),
    TRANSIENT_RATE("prolazna stavka", BigDecimal.ZERO),
    TAX_FREE("oslobođeno PDV-a", BigDecimal.ZERO);
    private final String description;
    private final BigDecimal rate;

    TaxRate(String description, BigDecimal rate) {
        this.description = description;
        this.rate = rate;
    }

    /**
     * Metoda vraća opis odabrane stope
     * @return opis stope
     */
    public String getDescription() {
        return description;
    }

    /**
     * Metoda vraća decimalnu vrijednost (npr. 0.25) za odabranu stopu PDV-a
     * @return decimalna vrijednost postotka stope
     */
    public BigDecimal getRate() {
        return rate;
    }
}
