package hr.java.projekt.database;

import hr.java.projekt.app.MainApplication;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.account.AccountParameters;
import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.business.BusinessBuilder;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

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
            PreparedStatement query = db.prepareStatement("INSERT INTO PARTNERS (NAME, ADDRESS, POSTAL, OIB, IBAN, PAYMENT_TERM) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            query.setString(1, business.getName());
            query.setString(2, business.getAddress());
            query.setString(3, business.getPostalCodeAndTown());
            query.setString(4, business.getOIB());
            query.setString(5, business.getIBAN());
            query.setLong(6, business.getPaymentTerm());

            query.executeUpdate();

            try (ResultSet generatedKeys = query.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    business.setId(id);
                    return id;
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
        if(Optional.ofNullable(business.getId()).isEmpty()){
            business.setId(id);
        }

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("UPDATE PARTNERS SET NAME = ?, ADDRESS = ?, POSTAL = ?, OIB = ?, IBAN = ?, PAYMENT_TERM = ? WHERE ID = ?");

            query.setString(1, business.getName());
            query.setString(2, business.getAddress());
            query.setString(3, business.getPostalCodeAndTown());
            query.setString(4, business.getOIB());
            query.setString(5, business.getIBAN());
            query.setLong(6, business.getPaymentTerm());
            query.setLong(7, id);

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
        builder.setPaymentTerm(resultSet.getLong("PAYMENT_TERM"));
        return builder.build();
    }

    public Double getTurnoverForPartner(Long partnerId) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            Optional<String> partnerAccount = MainApplication.getAccountParameter(AccountParameters.INVOICE_OUTPUT_BUYER);
            PreparedStatement query = db.prepareStatement("SELECT SUM(OWES) as \"SUM\" FROM JOURNAL_FINANCIAL WHERE PARTNER_ID = ? AND ACCOUNT_CODE = ? AND OWES <> 0 AND CLAIMS = 0 AND (DATE BETWEEN ? AND ?) GROUP BY PARTNER_ID");

            query.setLong(1, partnerId);
            query.setString(2, partnerAccount.get());
            LocalDate baseDate = LocalDate.now();
            query.setDate(3, Date.valueOf(baseDate.withDayOfMonth(1)));
            query.setDate(4, Date.valueOf(baseDate.with(lastDayOfMonth())));

            ResultSet resultSet = query.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("SUM");
            }
            else return (double) 0;
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške pri dohvatu računa!", e);
        }
    }
}
