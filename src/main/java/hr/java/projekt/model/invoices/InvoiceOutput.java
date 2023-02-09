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
        if (staticAmount != null) this.staticAmount = staticAmount.setScale(2, RoundingMode.HALF_UP);
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
        StringBuilder builder = new StringBuilder("Račun-otpremnica ");
        builder.append(this.getId()).append("\n");
        builder.append("Datum računa: ").append(this.getInvoiceDate()).append("\n");
        builder.append("Datum dospijeća: ").append(this.getDueDate()).append("\n");
        builder.append("Datum isporuke: ").append(this.getDeliveryDate()).append("\n");
        builder.append("Iznos: ").append(this.getTotalAmountWithVAT()).append("\n\n");

        builder.append("*** Stavke: ***").append("\n");
        for (ArticleTransaction transaction : this.transactions) {
            builder.append(transaction.getArticle().getName()).append("; ");
            builder.append(transaction.getQuantity()).append(" ").append(transaction.getArticle().getMeasure()).append("; ");
            builder.append("cijena: ").append(transaction.getPrice()).append("; ");
            builder.append("popust: ").append(transaction.getDiscount()).append("\n");
        }
        builder.append("*** Kraj ***").append("\n\n");

        builder.append("***Plaćanja***").append("\n");
        for (InvoicePayment payment : this.payments) {
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
        return "Račun-otpremnica " + this.getId();
    }
}
