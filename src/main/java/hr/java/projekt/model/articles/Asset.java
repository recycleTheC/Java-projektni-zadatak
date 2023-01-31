package hr.java.projekt.model.articles;

import hr.java.projekt.model.TaxRate;
import java.math.BigDecimal;

public class Asset extends Article {
    public static Integer TYPE = 1;
    private BigDecimal purchasePrice;
    private BigDecimal inventory;


    public Asset(String code, String name, String measure, BigDecimal price, TaxRate taxRate) {
        super(code, name, measure, price, taxRate);
    }

    public Asset(Long id, String code, String name, String measure, BigDecimal price, TaxRate taxRate, BigDecimal purchasePrice) {
        super(id, code, name, measure, price, taxRate);
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getInventory() {
        return inventory;
    }

    public void setInventory(BigDecimal stanjeZaliha) {
        this.inventory = stanjeZaliha;
    }

    public int getType(){ return 1;}
}
