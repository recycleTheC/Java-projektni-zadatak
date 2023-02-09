/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.database;

import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.exceptions.LoginException;
import hr.java.projekt.model.operator.Operator;
import hr.java.projekt.model.operator.Role;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OperatorRepository implements Dao<Operator> {
    @Override
    public Optional<Operator> get(Long id) throws DatabaseException {
        Optional<Operator> operator = Optional.empty();

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("SELECT ID, USERNAME, NAME, ROLE FROM OPERATORS WHERE ID = ? LIMIT 1");
            ResultSet resultSet = query.executeQuery();

            if (resultSet.next()) {
                operator = Optional.of(mapResultSet(resultSet));
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException(e);
        }

        return operator;
    }

    @Override
    public List<Operator> getMany() throws DatabaseException {
        List<Operator> operators = new ArrayList<>();

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("SELECT ID, USERNAME, NAME, ROLE FROM OPERATORS");
            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()) {
                operators.add(mapResultSet(resultSet));
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException(e);
        }

        return operators;
    }

    @Override
    public Long save(Operator operator) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("INSERT INTO OPERATORS(USERNAME, NAME, ROLE, PASSWORD) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            query.setString(1, operator.getUsername());
            query.setString(2, operator.getName());
            query.setString(3, operator.getRole().name());
            query.setString(4, operator.getHashedPassword());

            query.executeUpdate();

            try (ResultSet generatedKeys = query.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    operator.setId(id);
                    return id;
                } else {
                    throw new SQLException("Neuspješno kreiranje dokumenta! ID nije dohvaćen.");
                }
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Pogreška u radu s bazom podataka", e);
        }
    }

    @Override
    public void update(Long id, Operator operator) throws DatabaseException {
        if (Optional.ofNullable(operator.getId()).isEmpty())
            operator.setId(id);

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("UPDATE OPERATORS SET USERNAME = ?, NAME = ?, ROLE = ?, PASSWORD = ? WHERE ID = ?");
            query.setString(1, operator.getUsername());
            query.setString(2, operator.getName());
            query.setString(3, operator.getRole().name());
            query.setString(4, operator.getHashedPassword());
            query.setLong(5, id);

            query.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške kod ažuriranja operatera", e);
        }
    }

    @Override
    public void delete(Operator operator) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("DELETE FROM OPERATORS WHERE ID = ?");
            query.setLong(1, operator.getId());

            query.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške kod brisanja operatera", e);
        }
    }

    @Override
    public Operator mapResultSet(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("ID");
        String name = resultSet.getString("NAME");
        String username = resultSet.getString("USERNAME");
        Role role = Role.valueOf(resultSet.getString("ROLE"));

        return new Operator(id, username, name, role);
    }

    public Operator login(String username, String password) throws DatabaseException, LoginException {
        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("SELECT ID, USERNAME, NAME, ROLE FROM OPERATORS WHERE USERNAME = ? AND PASSWORD = ? LIMIT 1");
            query.setString(1, username);
            query.setString(2, Operator.hashPassword(password));

            ResultSet resultSet = query.executeQuery();

            if (resultSet.next()) {
                return mapResultSet(resultSet);
            } else throw new LoginException("Korisnički podatci su neispravni");
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Pogreška u radu s bazom podataka", e);
        }
    }
}
