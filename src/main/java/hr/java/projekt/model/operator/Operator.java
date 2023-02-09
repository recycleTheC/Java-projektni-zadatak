/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.model.operator;

import hr.java.projekt.model.Entity;
import hr.java.projekt.model.history.WritableHistory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Operator extends Entity implements WritableHistory {
    private String username, name, password;
    private Role role;

    public Operator(String username, String name, Role role) {
        this.username = username;
        this.name = name;
        this.role = role;
    }

    public Operator(Long id, String username, String name, Role role) {
        super(id);
        this.username = username;
        this.name = name;
        this.role = role;
    }

    public Operator(String username, String name, String password, Role role) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public Operator(Long id, String username, String name, String password, Role role) {
        super(id);
        this.username = username;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public static String hashPassword(String password) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashPassword(this.password);
    }

    @Override
    public StringBuilder stringGenerator() {
        StringBuilder builder = new StringBuilder("Operater ");
        builder.append(this.getId()).append("\n");
        builder.append(this.getName()).append(" - ").append(this.getUsername()).append("\n");
        builder.append(this.getRole().getType()).append("\n");
        return builder;
    }

    @Override
    public String getIdentifier() {
        return this.getId().toString();
    }

    @Override
    public String getShortDescription() {
        return "Operater " + "(" + this.getUsername() + ")";
    }
}
