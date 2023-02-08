package hr.java.projekt.model.articles;

import hr.java.projekt.model.Entity;
import hr.java.projekt.model.TaxRate;
import hr.java.projekt.model.history.WritableHistory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class Article extends Entity implements WritableHistory {
    private String code, name, measure;
    private BigDecimal price, priceWithVAT;
    private TaxRate taxRate;

    public Article(String code, String name, String measure, BigDecimal price, TaxRate taxRate) {
        super();
        this.code = code;
        this.name = name;
        this.measure = measure;
        this.taxRate = taxRate;
        this.setPrice(price);
    }

    public Article(Long id, String code, String name, String measure, BigDecimal price, TaxRate taxRate) {
        super(id);
        this.code = code;
        this.name = name;
        this.measure = measure;
        this.taxRate = taxRate;
        this.setPrice(price);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        this.priceWithVAT = price.add(price.multiply(taxRate.getRate())).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPriceWithVAT() {
        return priceWithVAT;
    }

    public void setPriceWithVAT(BigDecimal priceWithVAT) {
        this.priceWithVAT = priceWithVAT.setScale(2, RoundingMode.HALF_UP);
        this.price = priceWithVAT.divide(taxRate.getRate().add(BigDecimal.ONE), RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }

    public TaxRate getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(TaxRate taxRate) {
        this.taxRate = taxRate;
        this.priceWithVAT = price.add(price.multiply(taxRate.getRate()));
    }

    public abstract int getType();
    @Override
    public StringBuilder stringGenerator(){
        StringBuilder value = new StringBuilder("Artikl ");
        value.append(this.getCode()).append("\n");
        value.append("Naziv: ").append(this.getName()).append("\n");
        value.append("Cijena: ").append(this.getPrice()).append("\n");
        value.append("Stopa poreza: ").append(this.getTaxRate().getDescription()).append("\n");
        value.append("Cijena PDV: ").append(this.getPriceWithVAT()).append("\n");
        value.append("Mjerna jedinica: ").append(this.getMeasure()).append("\n");

        value.append("Tip artikla: ");
        if(this.getType() == Asset.TYPE) value.append("roba");
        else if(this.getType() == Service.TYPE) value.append("usluga");
        value.append("\n");

        return value;
    }

    @Override
    public String getIdentifier() {
        return this.code;
    }

    @Override
    public String getShortDescription() {
        return "Artikl " + this.getCode();
    }
}
