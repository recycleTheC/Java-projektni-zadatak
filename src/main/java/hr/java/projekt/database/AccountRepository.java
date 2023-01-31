/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.database;

import hr.java.projekt.model.account.Account;
import hr.java.projekt.model.account.AccountBuilder;
import hr.java.projekt.model.account.AccountType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountRepository implements Dao<Account> {
    @Override
    public Optional<Account> get(Long id) throws DatabaseException {
        Optional<Account> account = Optional.empty();

        try(Connection db = Database.connectToDatabase()){
            PreparedStatement query = db.prepareStatement("SELECT * FROM ACCOUNTS WHERE ID = ? LIMIT 1");
            query.setLong(1, id);

            ResultSet resultSet = query.executeQuery();

            if(resultSet.next()){
                account = Optional.of(mapResultSet(resultSet));
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Greška pri radu s bazom podataka!", e);
        }

        return account;
    }

    public Optional<Account> get(String code) throws DatabaseException {
        Optional<Account> account = Optional.empty();

        try(Connection db = Database.connectToDatabase()){
            PreparedStatement query = db.prepareStatement("SELECT * FROM ACCOUNTS WHERE CODE = ? LIMIT 1");
            query.setString(1, code);

            ResultSet resultSet = query.executeQuery();

            if(resultSet.next()){
                account = Optional.of(mapResultSet(resultSet));
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Greška pri radu s bazom podataka!", e);
        }

        return account;
    }

    @Override
    public List<Account> getMany() throws DatabaseException {
        List<Account> accounts = new ArrayList<>();

        try(Connection db = Database.connectToDatabase()){
            PreparedStatement query = db.prepareStatement("SELECT * FROM ACCOUNTS");
            ResultSet resultSet = query.executeQuery();

            while(resultSet.next()){
                accounts.add(mapResultSet(resultSet));
            }
        }
        catch (SQLException | IOException e) {
            throw new DatabaseException("Greška pri radu s bazom podataka!", e);
        }

        return accounts;
    }

    @Override
    public void save(Account account) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()){
            PreparedStatement query = db.prepareStatement("INSERT INTO ACCOUNTS(NAME, TYPE, CODE) VALUES (?, ?, ?)");

            query.setString(1, account.getName());
            query.setString(2, account.getType().toString());
            query.setString(3, account.getCode());

            query.executeUpdate();
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška kod spremanja konta!", ex);
        }
    }

    @Override
    public void update(Long id, Account account) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()){
            PreparedStatement query = db.prepareStatement("UPDATE ACCOUNTS SET CODE = ?, NAME = ?, TYPE = ? WHERE ID = ?");

            query.setString(1, account.getCode());
            query.setString(2, account.getName());
            query.setString(3, account.getType().toString());
            query.setLong(4, id);

            query.executeUpdate();
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška kod ažuriranja konta '" + account.getCode() + "'", ex);
        }
    }

    @Override
    public void delete(Account account) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()){
            PreparedStatement query = db.prepareStatement("DELETE FROM ACCOUNTS WHERE ID = ?");
            query.setLong(1, account.getId());
            query.executeUpdate();
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška kod brisanja konta '" + account.getCode() + "'", ex);
        }
    }

    @Override
    public Account mapResultSet(ResultSet resultSet) throws SQLException {
        AccountBuilder builder = new AccountBuilder(resultSet.getLong("ID"));
        builder.setName(resultSet.getString("NAME"));
        builder.setType(AccountType.valueOf(resultSet.getString("TYPE")));
        builder.setCode(resultSet.getString("CODE"));
        return builder.build();
    }
}
