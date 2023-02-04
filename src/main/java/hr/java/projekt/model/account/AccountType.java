/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.model.account;

public enum AccountType {
    NO_ANALYTICS("nema analitike"),
    SUPPLIERS("dobavljači"),
    BUYERS("kupci"),
    STORAGE("robno");

    private final String description;
    AccountType(String type) {
        this.description = type;
    }

    public String getDescription() {
        return description;
    }
}
