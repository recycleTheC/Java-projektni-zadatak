/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.model.inventory.assetinput;

import hr.java.projekt.model.Entity;
import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.history.WritableHistory;
import hr.java.projekt.model.inventory.ArticleTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AssetInput extends Entity implements WritableHistory {
    private List<ArticleTransaction> transactions;
    private Business supplier;
    private LocalDate inputDate, invoiceDate;
    private BigDecimal amount;
    private String invoiceId;

    public AssetInput(Long id, Business supplier, LocalDate inputDate, String invoiceId, LocalDate invoiceDate, BigDecimal amount) {
        super(id);
        this.transactions = new ArrayList<>();
        this.supplier = supplier;
        this.inputDate = inputDate;
        this.invoiceDate = invoiceDate;
        this.amount = amount;
        this.invoiceId = invoiceId;
    }

    public AssetInput(Business supplier, LocalDate inputDate, String invoiceId, LocalDate invoiceDate, BigDecimal amount) {
        this(null, supplier, inputDate, invoiceId, invoiceDate, amount);
    }

    public List<ArticleTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<ArticleTransaction> transactions) {
        this.transactions = transactions;
    }

    public void insertTransaction(ArticleTransaction transaction){
        this.transactions.add(transaction);
    }

    public Business getSupplier() {
        return supplier;
    }

    public void setSupplier(Business supplier) {
        this.supplier = supplier;
    }

    public LocalDate getInputDate() {
        return inputDate;
    }

    public void setInputDate(LocalDate inputDate) {
        this.inputDate = inputDate;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public static BigDecimal getTransactionsTotalAmount(List<ArticleTransaction> transactions){
        BigDecimal sum = new BigDecimal("0");

        for(ArticleTransaction transaction : transactions){
            sum = sum.add(transaction.getTotal());
        }

        return sum;
    }

    public BigDecimal getTransactionsTotalAmount(){
        return getTransactionsTotalAmount(this.transactions);
    }

    @Override
    public StringBuilder stringGenerator() {
        StringBuilder builder = new StringBuilder("Primka ");
        builder.append(this.getId()).append("\n");
        builder.append("Dobavljač: ").append(this.getSupplier().getOIB()).append("\n");
        builder.append("Račun: ").append(this.getInvoiceId()).append(", ").append(this.getInvoiceDate()).append("\n");
        builder.append("Iznos: ").append(this.getAmount()).append("\n");
        builder.append("***Stavke:***").append("\n");
        this.getTransactions().forEach(t ->
                builder.append(t.getArticle().getName()).append(", ")
                        .append(t.getQuantity()).append(", ")
                        .append(t.getPrice()).append(", ")
                        .append(t.getDiscount().multiply(BigDecimal.valueOf(100))).append("%, ")
                        .append(t.getTotal()).append("\n")
        );
        builder.append("***Kraj***").append("\n");
        return builder;
    }

    @Override
    public String getIdentifier() {
        return this.getId().toString();
    }
}
