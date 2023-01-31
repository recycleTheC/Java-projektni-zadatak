/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.model.account;

import hr.java.projekt.model.Entity;

public class Account extends Entity {
    private String code, name;
    private AccountType type;

    public Account(String code, String name, AccountType type) {
        super();
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public Account(Long id, String code, String name, AccountType type) {
        super(id);
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }
}
