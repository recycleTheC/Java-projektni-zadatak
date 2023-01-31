package hr.java.projekt.model.articles;

import hr.java.projekt.model.TaxRate;
import java.math.BigDecimal;

public class Service extends Business {
    public static Integer TYPE = 2;
    private BigDecimal costOfService;
    public Service(String code, String name, String measure, BigDecimal price, TaxRate taxRate, BigDecimal costOfService) {
        super(code, name, measure, price, taxRate);
        this.costOfService = costOfService;
    }

    public Service(Long id, String code, String name, String measure, BigDecimal price, TaxRate taxRate, BigDecimal costOfService) {
        super(id, code, name, measure, price, taxRate);
        this.costOfService = costOfService;
    }

    public BigDecimal getCostOfService() {
        return costOfService;
    }

    public void setCostOfService(BigDecimal costOfService) {
        this.costOfService = costOfService;
    }

    public int getType(){ return 2; }
}
