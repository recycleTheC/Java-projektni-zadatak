/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.model.invoices;

import hr.java.projekt.model.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class InvoicePayment extends Entity {
    private LocalDate date;
    private BigDecimal amount;

    public InvoicePayment(LocalDate date, BigDecimal amount) {
        this.date = date;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public InvoicePayment(Long id, LocalDate date, BigDecimal amount) {
        super(id);
        this.date = date;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal getTotalAmount(List<InvoicePayment> payments){
        BigDecimal result = new BigDecimal("0").setScale(2, RoundingMode.HALF_UP);

        for(InvoicePayment payment: payments){
            result = result.add(payment.getAmount());
        }

        return result;
    }
}
