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
                Integer type = result.getInt("TYPE");
                String code = result.getString("CODE");
                String name = result.getString("NAME");
                String measure = result.getString("MEASURE");
                BigDecimal price = result.getBigDecimal("PRICE");
                BigDecimal averageCost = result.getBigDecimal("AVG_COST");
                TaxRate taxRate = TaxRate.valueOf(result.getString("TAX_RATE"));

                if(type.equals(Asset.TYPE)){
                    article = Optional.of(new AssetBuilder(id).setCode(code).setName(name).setMeasure(measure).setPrice(price).setTaxRate(taxRate).setPurchasePrice(averageCost).build());
                }
                else if(type.equals(Service.TYPE)){
                    article = Optional.of(new ServiceBuilder(id).setCode(code).setName(name).setMeasure(measure).setPrice(price).setTaxRate(taxRate).setCostOfService(averageCost).build());
                }
                else throw new DatabaseException("Greška kod čitanja artikala iz baze podataka!");
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
                Long id = result.getLong("ID");
                Integer type = result.getInt("TYPE");
                String code = result.getString("CODE");
                String name = result.getString("NAME");
                String measure = result.getString("MEASURE");
                BigDecimal price = result.getBigDecimal("PRICE");
                BigDecimal averageCost = result.getBigDecimal("AVG_COST");
                TaxRate taxRate = TaxRate.valueOf(result.getString("TAX_RATE"));

                if(type.equals(Asset.TYPE)){
                    articles.add(new AssetBuilder(id).setCode(code).setName(name).setMeasure(measure).setPrice(price).setTaxRate(taxRate).setPurchasePrice(averageCost).build());
                }
                else if(type.equals(Service.TYPE)){
                    articles.add(new ServiceBuilder(id).setCode(code).setName(name).setMeasure(measure).setPrice(price).setTaxRate(taxRate).setCostOfService(averageCost).build());
                }
                else throw new DatabaseException("Greška kod čitanja artikala iz baze podataka!");
            }
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Greška pri radu s bazom podataka!", ex);
        }

        return articles;
    }

    @Override
    public void save(Article article) {

    }

    @Override
    public void update(Long id, Article artikl) {

    }

    @Override
    public void delete(Article artikl) {

    }
}
