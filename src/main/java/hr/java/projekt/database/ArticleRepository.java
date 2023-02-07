package hr.java.projekt.database;

import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.TaxRate;
import hr.java.projekt.model.articles.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArticleRepository implements Dao<Article> {
    @Override
    public Optional<Article> get(Long id) throws DatabaseException {
        Optional<Article> article = Optional.empty();

        try (Connection db = Database.connectToDatabase()) {
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

        try (Connection db = Database.connectToDatabase()) {
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

        try (Connection db = Database.connectToDatabase()) {
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
    public Long save(Article article) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("INSERT INTO ARTICLES (CODE, NAME, PRICE, TAX_RATE, TYPE, MEASURE, AVG_COST) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            query.setString(1, article.getCode());
            query.setString(2, article.getName());
            query.setBigDecimal(3, article.getPrice());
            query.setString(4, article.getTaxRate().toString());
            query.setInt(5, article.getType());
            query.setString(6, article.getMeasure());

            if (article instanceof Service service) query.setBigDecimal(7, service.getCostOfService());
            else query.setBigDecimal(7, null);

            query.executeUpdate();

            try (ResultSet generatedKeys = query.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Neuspješno kreiranje konta! ID nije dohvaćen.");
                }
            }
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška kod spremanja artikla '" + article.getCode() + "'", ex);
        }
    }

    @Override
    public void update(Long id, Article article) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("UPDATE ARTICLES SET CODE = ?, NAME = ?, PRICE = ?, TAX_RATE = ?, TYPE = ?, MEASURE = ?, AVG_COST = ? WHERE ID = ?");

            query.setString(1, article.getCode());
            query.setString(2, article.getName());
            query.setBigDecimal(3, article.getPrice());
            query.setString(4, article.getTaxRate().toString());
            query.setInt(5, article.getType());
            query.setString(6, article.getMeasure());

            if (article instanceof Service service) query.setBigDecimal(7, service.getCostOfService());
            else query.setBigDecimal(7, null);

            query.setLong(8, id);

            query.executeUpdate();
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška kod ažuriranja artikla '" + article.getCode() + "'", ex);
        }
    }

    @Override
    public void delete(Article article) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
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

        if (type.equals(Asset.TYPE)) {
            return new AssetBuilder(id).setCode(code).setName(name).setMeasure(measure).setPrice(price).setTaxRate(taxRate).setPurchasePrice(averageCost).build();
        } else if (type.equals(Service.TYPE)) {
            return new ServiceBuilder(id).setCode(code).setName(name).setMeasure(measure).setPrice(price).setTaxRate(taxRate).setCostOfService(averageCost).build();
        } else throw new DatabaseException("Greška kod čitanja artikala iz baze podataka!");
    }

    public List<ArticleStock> getStock(LocalDate date) throws DatabaseException {
        List<Article> articles = getMany().stream().filter(article -> article instanceof Asset).toList();
        List<ArticleStock> results = new ArrayList<>();

        for (Article article : articles) {
            results.add(getStock(article, date));
        }

        return results;
    }

    public ArticleStock getStock(Article article, LocalDate date) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement(
                    "SELECT " +
                            "(SUM(INPUT) - SUM(OUTPUT)) as \"STOCK\", " +
                            "CASEWHEN(SUM(INPUT) <> 0, (SUM(OWES) / (SUM(INPUT))), 0) as \"AVERAGE_PRICE\"" +
                            "FROM JOURNAL_STORAGE " +
                            "WHERE DATE <= ? AND ARTICLE_ID = ?" +
                            "GROUP BY ARTICLE_ID"
            );
            query.setDate(1, Date.valueOf(date));
            query.setLong(2, article.getId());

            ResultSet resultSet = query.executeQuery();

            if (resultSet.next()) {
                BigDecimal stock = resultSet.getBigDecimal("STOCK");
                BigDecimal averagePurchasePrice = resultSet.getBigDecimal("AVERAGE_PRICE");

                return new ArticleStock(article, stock, averagePurchasePrice);
            } else return new ArticleStock(article, BigDecimal.ZERO, BigDecimal.ZERO);
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Greška prilikom dohvaćanja zaliha!", e);
        }
    }
}
