/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.model.invoices;

import java.util.Comparator;

public class InvoiceByDueDateComparator implements Comparator<Invoice> {

    @Override
    public int compare(Invoice o1, Invoice o2) {
        return o1.getDueDate().compareTo(o2.getDueDate());
    }
}
