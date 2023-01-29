package hr.java.projekt.model.articles;

import hr.java.projekt.model.TaxRate;

import java.math.BigDecimal;

public class ServiceBuilder {
    private String code;
    private String name;
    private String measure;
    private BigDecimal price;
    private TaxRate taxRate;
    private BigDecimal costOfService;
    private Long id;

    public ServiceBuilder setCode(String code) {
        this.code = code;
        return this;
    }

    public ServiceBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ServiceBuilder setMeasure(String measure) {
        this.measure = measure;
        return this;
    }

    public ServiceBuilder setPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public ServiceBuilder setTaxRate(TaxRate taxRate) {
        this.taxRate = taxRate;
        return this;
    }

    public ServiceBuilder setCostOfService(BigDecimal costOfService) {
        this.costOfService = costOfService;
        return this;
    }

    public ServiceBuilder(Long id) {
        this.id = id;
    }

    public ServiceBuilder() {
    }

    public Service build() {
        return new Service(id, code, name, measure, price, taxRate, costOfService);
    }
}