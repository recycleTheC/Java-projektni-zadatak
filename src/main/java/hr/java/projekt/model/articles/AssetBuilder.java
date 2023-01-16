package hr.java.projekt.model.articles;

import hr.java.projekt.model.TaxRate;

import java.math.BigDecimal;

public class AssetBuilder {
    private String code;
    private String name;
    private String measure;
    private BigDecimal price;
    private TaxRate taxRate;
    private BigDecimal purchasePrice;
    private Long id;
    public AssetBuilder(Long id) {
        this.id = id;
    }

    public AssetBuilder setCode(String code) {
        this.code = code;
        return this;
    }

    public AssetBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public AssetBuilder setMeasure(String measure) {
        this.measure = measure;
        return this;
    }

    public AssetBuilder setPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public AssetBuilder setTaxRate(TaxRate taxRate) {
        this.taxRate = taxRate;
        return this;
    }

    public AssetBuilder setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
        return this;
    }
    public Asset build() {
        return new Asset(id, code, name, measure, price, taxRate, purchasePrice);
    }
}