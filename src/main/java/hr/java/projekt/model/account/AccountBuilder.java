/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.model.account;

public class AccountBuilder {
    private String code;
    private String name;
    private AccountType type;
    private Long id;

    public AccountBuilder(){}

    public AccountBuilder(Long id){
        this.id = id;
    }
    public AccountBuilder setCode(String code) {
        this.code = code;
        return this;
    }

    public AccountBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public AccountBuilder setType(AccountType type) {
        this.type = type;
        return this;
    }

    public AccountBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public Account build() {
        return new Account(code, name, type);
    }
}