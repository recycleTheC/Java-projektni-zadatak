/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.model.inventory.assetinput;

import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.inventory.ArticleTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AssetInputBuilder {
    private Long id = null;
    private Business supplier;
    private LocalDate inputDate;
    private String invoiceId;
    private LocalDate invoiceDate;
    private BigDecimal amount;
    private List<ArticleTransaction> transactions;

    public AssetInputBuilder(){
        this.transactions = new ArrayList<>();
    }

    public AssetInputBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public AssetInputBuilder setSupplier(Business supplier) {
        this.supplier = supplier;
        return this;
    }

    public AssetInputBuilder setInputDate(LocalDate inputDate) {
        this.inputDate = inputDate;
        return this;
    }

    public AssetInputBuilder setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
        return this;
    }

    public AssetInputBuilder setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
        return this;
    }

    public AssetInputBuilder setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public AssetInputBuilder setTransactions(List<ArticleTransaction> transactions) {
        this.transactions = transactions;
        return this;
    }
    public AssetInput build() {
        AssetInput object = new AssetInput(id, supplier, inputDate, invoiceId, invoiceDate, amount);
        object.setTransactions(transactions);
        return object;
    }
}