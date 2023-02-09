/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.model.invoices;

import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.inventory.ArticleTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class InvoiceBuilder {
    private Long id = null;
    private Business partner;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private List<ArticleTransaction> transactions;
    private List<InvoicePayment> payments;
    private BigDecimal amount;

    public InvoiceBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public InvoiceBuilder setPartner(Business partner) {
        this.partner = partner;
        return this;
    }

    public InvoiceBuilder setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
        return this;
    }

    public InvoiceBuilder setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public InvoiceBuilder setTransactions(List<ArticleTransaction> transactions) {
        this.transactions = transactions;
        return this;
    }

    public InvoiceBuilder setPayments(List<InvoicePayment> payments) {
        this.payments = payments;
        return this;
    }

    public InvoiceBuilder setAmount(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        return this;
    }

    public Invoice build() {
        return new Invoice(id, partner, invoiceDate, dueDate, transactions, payments, amount);
    }
}