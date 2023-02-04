package hr.java.projekt.database;

import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.business.BusinessBuilder;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BusinessRepository implements Dao<Business> {
    @Override
    public Optional<Business> get(Long id) throws DatabaseException {
        Optional<Business> business = Optional.empty();

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("SELECT * FROM PARTNERS WHERE ID = ? LIMIT 1");
            query.setLong(1, id);

            ResultSet resultSet = query.executeQuery();

            if (resultSet.next()) {
                business = Optional.of(mapResultSet(resultSet));
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Greška pri radu s bazom podataka!", e);
        }

        return business;
    }

    @Override
    public List<Business> getMany() throws DatabaseException {
        List<Business> partners = new ArrayList<>();

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("SELECT * FROM PARTNERS");
            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()) {
                partners.add(mapResultSet(resultSet));
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Greška pri radu s bazom podataka!", e);
        }

        return partners;
    }

    @Override
    public Long save(Business business) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("INSERT INTO PARTNERS (NAME, ADDRESS, POSTAL, OIB, IBAN) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            query.setString(1, business.getName());
            query.setString(2, business.getAddress());
            query.setString(3, business.getPostalCodeAndTown());
            query.setString(4, business.getOIB());
            query.setString(5, business.getIBAN());

            query.executeUpdate();

            try (ResultSet generatedKeys = query.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Neuspješno kreiranje konta! ID nije dohvaćen.");
                }
            }
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška kod spremanja partnera", ex);
        }
    }

    @Override
    public void update(Long id, Business business) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("UPDATE PARTNERS SET NAME = ?, ADDRESS = ?, POSTAL = ?, OIB = ?, IBAN = ? WHERE ID = ?");

            query.setString(1, business.getName());
            query.setString(2, business.getAddress());
            query.setString(3, business.getPostalCodeAndTown());
            query.setString(4, business.getOIB());
            query.setString(5, business.getIBAN());
            query.setLong(6, id);

            query.executeUpdate();
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška kod ažuriranja partnera '" + business.getId() + "'", ex);
        }
    }

    @Override
    public void delete(Business business) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("DELETE FROM PARTNERS WHERE ID = ?");
            query.setLong(1, business.getId());
            query.executeUpdate();
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška kod brisanja partnera '" + business.getId() + "'", ex);
        }
    }

    @Override
    public Business mapResultSet(ResultSet resultSet) throws SQLException {
        BusinessBuilder builder = new BusinessBuilder(resultSet.getLong("ID"));
        builder.setName(resultSet.getString("NAME"));
        builder.setAddress(resultSet.getString("ADDRESS"));
        builder.setPostalCodeAndTown(resultSet.getString("POSTAL"));
        builder.setUncheckedOIB(resultSet.getString("OIB"));
        builder.setUncheckedIBAN(resultSet.getString("IBAN"));
        return builder.build();
    }
}
