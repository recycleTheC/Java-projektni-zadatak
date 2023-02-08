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

public class InvoiceOutput extends Invoice {
    private LocalDate deliveryDate;
    public InvoiceOutput(Long id, Business partner, LocalDate invoiceDate, LocalDate dueDate, LocalDate deliveryDate, List<ArticleTransaction> transactions, List<InvoicePayment> payments) {
        super(id, partner, invoiceDate, dueDate, transactions, payments);
        this.deliveryDate = deliveryDate;
    }

    public InvoiceOutput(Business partner, LocalDate invoiceDate, LocalDate dueDate, LocalDate deliveryDate, List<ArticleTransaction> transactions, List<InvoicePayment> payments) {
        this(null, partner, invoiceDate, dueDate, deliveryDate, transactions, payments);
    }

    public InvoiceOutput(Long id, Business partner, LocalDate invoiceDate, LocalDate dueDate, LocalDate deliveryDate, List<ArticleTransaction> transactions, List<InvoicePayment> payments, BigDecimal staticAmount) {
        this(id, partner, invoiceDate, dueDate, deliveryDate, transactions, payments);
        if(staticAmount != null) this.staticAmount = staticAmount.setScale(2,RoundingMode.HALF_UP);
    }

    public InvoiceOutput() {
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @Override
    public StringBuilder stringGenerator() {
        return new StringBuilder("Račun otpremnica test");
    }

    @Override
    public String getIdentifier() {
        return this.getId().toString();
    }

    @Override
    public String getShortDescription() {
        return "Račun-otpremnica " + this.getId();
    }
}
