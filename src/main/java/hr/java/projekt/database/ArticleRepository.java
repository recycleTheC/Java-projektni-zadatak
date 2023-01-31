package hr.java.projekt.database;

import hr.java.projekt.model.TaxRate;
import hr.java.projekt.model.articles.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArticleRepository implements Dao<Article> {
    @Override
    public Optional<Article> get(Long id) throws DatabaseException {
        Optional<Article> article = Optional.empty();

        try (Connection db = Database.connectToDatabase()){
            PreparedStatement query = db.prepareStatement("SELECT * FROM ARTICLES WHERE ID = ? LIMIT 1");
            query.setLong(1, id);

            ResultSet result = query.executeQuery();

            while (result.next()) {
                article = Optional.of(mapResultSet(result));
            }
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška pri radu s bazom podataka!", ex);
        }

        return article;
    }

    public Optional<Article> get(String code) throws DatabaseException {
        Optional<Article> article = Optional.empty();

        try (Connection db = Database.connectToDatabase()){
            PreparedStatement query = db.prepareStatement("SELECT * FROM ARTICLES WHERE LOWER(CODE) = ? LIMIT 1");
            query.setString(1, code.toLowerCase());

            ResultSet result = query.executeQuery();

            while (result.next()) {
                article = Optional.of(mapResultSet(result));
            }
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška pri radu s bazom podataka!", ex);
        }

        return article;
    }

    @Override
    public List<Article> getMany() throws DatabaseException {
        List<Article> articles = new ArrayList<>();

        try (Connection db = Database.connectToDatabase()){
            PreparedStatement query = db.prepareStatement("SELECT * FROM ARTICLES");
            ResultSet result = query.executeQuery();

            while (result.next()) {
                articles.add(mapResultSet(result));
            }
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška pri radu s bazom podataka!", ex);
        }

        return articles;
    }

    @Override
    public void save(Article article) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()){
            PreparedStatement query = db.prepareStatement("INSERT INTO ARTICLES (CODE, NAME, PRICE, TAX_RATE, TYPE, MEASURE, AVG_COST) VALUES (?, ?, ?, ?, ?, ?, ?)");

            query.setString(1, article.getCode());
            query.setString(2, article.getName());
            query.setBigDecimal(3, article.getPrice());
            query.setString(4, article.getTaxRate().toString());
            query.setInt(5, article.getType());
            query.setString(6, article.getMeasure());

            if(article instanceof Service service) query.setBigDecimal(7, service.getCostOfService());
            else query.setBigDecimal(7, null);

            query.executeUpdate();
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška kod spremanja artikla '" + article.getCode() + "'", ex);
        }
    }

    @Override
    public void update(Long id, Article article) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()){
            PreparedStatement query = db.prepareStatement("UPDATE ARTICLES SET CODE = ?, NAME = ?, PRICE = ?, TAX_RATE = ?, TYPE = ?, MEASURE = ?, AVG_COST = ? WHERE ID = ?");

            query.setString(1, article.getCode());
            query.setString(2, article.getName());
            query.setBigDecimal(3, article.getPrice());
            query.setString(4, article.getTaxRate().toString());
            query.setInt(5, article.getType());
            query.setString(6, article.getMeasure());

            if(article instanceof Service service) query.setBigDecimal(7, service.getCostOfService());
            else query.setBigDecimal(7, null);

            query.setLong(8, id);

            query.executeUpdate();
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška kod ažuriranja artikla '" + article.getCode() + "'", ex);
        }
    }

    @Override
    public void delete(Article article) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()){
            PreparedStatement query = db.prepareStatement("DELETE FROM ARTICLES WHERE ID = ?");
            query.setLong(1, article.getId());
            query.executeUpdate();
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška kod brisanja artikla '" + article.getCode() + "'", ex);
        }
    }

    @Override
    public Article mapResultSet(ResultSet resultSet) throws SQLException, DatabaseException {
        Long id = resultSet.getLong("ID");
        Integer type = resultSet.getInt("TYPE");
        String code = resultSet.getString("CODE");
        String name = resultSet.getString("NAME");
        String measure = resultSet.getString("MEASURE");
        BigDecimal price = resultSet.getBigDecimal("PRICE");
        BigDecimal averageCost = resultSet.getBigDecimal("AVG_COST");
        TaxRate taxRate = TaxRate.valueOf(resultSet.getString("TAX_RATE"));

        if(type.equals(Asset.TYPE)){
            return new AssetBuilder(id).setCode(code).setName(name).setMeasure(measure).setPrice(price).setTaxRate(taxRate).setPurchasePrice(averageCost).build();
        }
        else if(type.equals(Service.TYPE)){
            return new ServiceBuilder(id).setCode(code).setName(name).setMeasure(measure).setPrice(price).setTaxRate(taxRate).setCostOfService(averageCost).build();
        }
        else throw new DatabaseException("Greška kod čitanja artikala iz baze podataka!");
    }
}
