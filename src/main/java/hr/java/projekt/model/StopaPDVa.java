package hr.java.projekt.model;

import java.math.BigDecimal;

public enum StopaPDVa {
    REDOVNA_STOPA("redovna stopa", new BigDecimal("0.25")),
    SNIZENA_STOPA("snižena stopa", new BigDecimal("0.13")),
    NAJNIZA_STOPA("najniža stopa", new BigDecimal("0.05")),
    PROLAZNA_STAVKA("prolazna stavka", BigDecimal.ZERO),
    OSLOBODJENO("odlobođeno PDV-a", BigDecimal.ZERO);
    private final String opis;
    private final BigDecimal stopa;

    StopaPDVa(String opis, BigDecimal stopa) {
        this.opis = opis;
        this.stopa = stopa;
    }

    /**
     * Metoda vraća opis odabrane stope
     * @return opis stope
     */
    public String getOpis() {
        return opis;
    }

    /**
     * Metoda vraća decimalnu vrijednost (npr. 0.25) za odabranu stopu PDV-a
     * @return decimalna vrijednost postotka stope
     */
    public BigDecimal getStopa() {
        return stopa;
    }
}
