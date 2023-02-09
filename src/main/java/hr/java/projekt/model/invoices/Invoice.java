/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.model.invoices;

import hr.java.projekt.model.Entity;
import hr.java.projekt.model.TaxRate;
import hr.java.projekt.model.articles.Asset;
import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.history.WritableHistory;
import hr.java.projekt.model.inventory.ArticleTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Invoice extends Entity implements WritableHistory {
    protected Business partner;
    protected LocalDate invoiceDate, dueDate;
    protected List<ArticleTransaction> transactions;
    protected List<InvoicePayment> payments;
    protected BigDecimal staticAmount;

    public Invoice(Long id, Business partner, LocalDate invoiceDate, LocalDate dueDate, List<ArticleTransaction> transactions, List<InvoicePayment> payments) {
        super(id);
        this.partner = partner;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;

        if (transactions == null) this.transactions = new ArrayList<>();
        else this.transactions = transactions;

        if (payments == null) this.payments = new ArrayList<>();
        else this.payments = payments;
    }

    public Invoice(Business partner, LocalDate invoiceDate, LocalDate dueDate, List<ArticleTransaction> transactions, List<InvoicePayment> payments) {
        this(null, partner, invoiceDate, dueDate, transactions, payments);
    }

    public Invoice(Long id, Business partner, LocalDate invoiceDate, LocalDate dueDate, List<ArticleTransaction> transactions, List<InvoicePayment> payments, BigDecimal staticAmount) {
        this(id, partner, invoiceDate, dueDate, transactions, payments);
        if (staticAmount != null) this.staticAmount = staticAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public Invoice() {
    }

    public static BigDecimal getBasisAmountByTaxRate(List<ArticleTransaction> allTransactions, TaxRate rate) {
        BigDecimal amount = new BigDecimal("0").setScale(2, RoundingMode.HALF_UP);
        List<ArticleTransaction> transactions = allTransactions.parallelStream().filter(transaction -> transaction.getArticle().getTaxRate().equals(rate)).toList();

        for (ArticleTransaction transaction : transactions) {
            amount = amount.add(transaction.getTotal());
        }

        return amount;
    }

    public static BigDecimal getTaxAmountByRate(List<ArticleTransaction> allTransactions, TaxRate rate) {
        BigDecimal amount = new BigDecimal("0").setScale(2, RoundingMode.HALF_UP);
        List<ArticleTransaction> transactions = allTransactions.parallelStream().filter(transaction -> transaction.getArticle().getTaxRate().equals(rate)).toList();

        for (ArticleTransaction transaction : transactions) {
            amount = amount.add(transaction.getTotal().multiply(rate.getRate()));
        }

        return amount;
    }

    public static BigDecimal getTotalTaxAmount(List<ArticleTransaction> allTransactions) {
        BigDecimal amount = new BigDecimal("0").setScale(2, RoundingMode.HALF_UP);

        for (TaxRate rate : TaxRate.values()) {
            List<ArticleTransaction> transactions = allTransactions.parallelStream().filter(transaction -> transaction.getArticle().getTaxRate().equals(rate)).toList();

            for (ArticleTransaction transaction : transactions) {
                amount = amount.add(transaction.getTotal().multiply(rate.getRate()));
            }
        }

        return amount;
    }

    public static BigDecimal getTotalAmountWithVAT(List<ArticleTransaction> allTransactions) {
        BigDecimal amount = new BigDecimal("0").setScale(2, RoundingMode.HALF_UP);

        for (ArticleTransaction transaction : allTransactions) {
            amount = amount.add(transaction.getTotal().multiply(transaction.getArticle().getTaxRate().getRate().add(BigDecimal.ONE)));
        }

        return amount;
    }

    public static BigDecimal getTotalBasisAmount(List<ArticleTransaction> allTransactions) {
        BigDecimal amount = new BigDecimal("0").setScale(2, RoundingMode.HALF_UP);

        for (ArticleTransaction transaction : allTransactions) {
            amount = amount.add(transaction.getTotal());
        }

        return amount;
    }

    public BigDecimal getTotalTaxAmount() {
        return getTotalTaxAmount(this.transactions);
    }

    public BigDecimal getTotalAssetPurchasePrice() {
        List<ArticleTransaction> transactions = this.transactions.stream().filter(transaction -> transaction.getArticle() instanceof Asset).toList();
        BigDecimal sum = new BigDecimal("0").setScale(2, RoundingMode.HALF_UP);

        for (ArticleTransaction transaction : transactions) {
            Asset asset = (Asset) transaction.getArticle();
            sum = sum.add(asset.getPurchasePrice());
        }

        return sum;
    }

    public BigDecimal getBasisAmountByTaxRate(TaxRate rate) {
        return getBasisAmountByTaxRate(this.transactions, rate);
    }

    public BigDecimal getTotalBasisAmount() {
        return getTotalBasisAmount(this.transactions);
    }

    public BigDecimal getTaxAmountByRate(TaxRate rate) {
        return getTaxAmountByRate(this.transactions, rate);
    }

    public BigDecimal getTotalAmountWithVAT() {
        return getTotalAmountWithVAT(this.transactions);
    }

    public Business getPartner() {
        return partner;
    }

    public void setPartner(Business partner) {
        this.partner = partner;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<ArticleTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<ArticleTransaction> transactions) {
        this.transactions = transactions;
    }

    public List<InvoicePayment> getPayments() {
        return payments;
    }

    public void setPayments(List<InvoicePayment> payments) {
        this.payments = payments;
    }

    public void insertTransaction(ArticleTransaction transaction) {
        this.transactions.add(transaction);
    }

    public BigDecimal getStaticAmount() {
        return staticAmount;
    }

    @Override
    public StringBuilder stringGenerator() {
        StringBuilder builder = new StringBuilder("Račun ");
        builder.append(this.getId()).append("\n");
        builder.append("Datum računa: ").append(this.getInvoiceDate()).append("\n");
        builder.append("Datum dospijeća: ").append(this.getDueDate()).append("\n");
        builder.append("Iznos: ").append(this.getTotalAmountWithVAT()).append("\n\n");

        builder.append("*** Stavke: ***").append("\n");
        for(ArticleTransaction transaction: this.transactions){
            builder.append(transaction.getArticle().getName()).append("; ");
            builder.append(transaction.getQuantity()).append(" ").append(transaction.getArticle().getMeasure()).append("; ");
            builder.append("cijena: ").append(transaction.getPrice()).append("; ");
            builder.append("popust: ").append(transaction.getDiscount()).append("\n");
        }
        builder.append("*** Kraj ***").append("\n\n");

        builder.append("***Plaćanja***").append("\n");
        for(InvoicePayment payment : this.payments){
            builder.append(payment.getDate()).append("; ");
            builder.append(payment.getAmount()).append("\n");
        }
        builder.append("*** Kraj ***").append("\n\n");

        return builder;
    }

    @Override
    public String getIdentifier() {
        return this.getId().toString();
    }

    @Override
    public String getShortDescription() {
        return "Račun " + this.getId();
    }
}
