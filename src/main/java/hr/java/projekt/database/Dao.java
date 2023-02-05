/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.database;

import hr.java.projekt.exceptions.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Dao<T> {
    Optional<T> get(Long id) throws DatabaseException;

    List<T> getMany() throws DatabaseException;

    Long save(T t) throws DatabaseException;

    void update(Long id, T t) throws DatabaseException;

    void delete(T t) throws DatabaseException;

    T mapResultSet(ResultSet resultSet) throws SQLException, DatabaseException;
}