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

public class InvoiceOutputBuilder {
    private Long id = null;
    private Business partner;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private LocalDate deliveryDate;
    private List<ArticleTransaction> transactions;
    private List<InvoicePayment> payments;
    private BigDecimal amount;

    public InvoiceOutputBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public InvoiceOutputBuilder setPartner(Business partner) {
        this.partner = partner;
        return this;
    }

    public InvoiceOutputBuilder setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
        return this;
    }

    public InvoiceOutputBuilder setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public InvoiceOutputBuilder setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
        return this;
    }

    public InvoiceOutputBuilder setTransactions(List<ArticleTransaction> transactions) {
        this.transactions = transactions;
        return this;
    }

    public InvoiceOutputBuilder setPayments(List<InvoicePayment> payments) {
        this.payments = payments;
        return this;
    }

    public InvoiceOutputBuilder setAmount(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        return this;
    }

    public InvoiceOutput build() {
        return new InvoiceOutput(id, partner, invoiceDate, dueDate, deliveryDate, transactions, payments, amount);
    }
}