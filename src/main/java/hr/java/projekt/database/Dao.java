package hr.java.projekt.database;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {
    Optional<T> get(Long id) throws DatabaseException;
    List<T> getMany() throws DatabaseException;
    void save(T t);
    void update(Long id, T t);
    void delete(T t);
}