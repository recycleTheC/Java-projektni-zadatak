/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.database;

import hr.java.projekt.app.MainApplication;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.account.AccountParameters;
import hr.java.projekt.model.articles.Article;
import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.inventory.assetinput.AssetInput;
import hr.java.projekt.model.inventory.assetinput.AssetInputBuilder;
import hr.java.projekt.model.inventory.assetinput.AssetInputTransaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssetInputRepository implements Dao<AssetInput> {
    @Override
    public Optional<AssetInput> get(Long id) throws DatabaseException {
        Optional<AssetInput> assetInput = Optional.empty();

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement documentQuery = db.prepareStatement("SELECT * FROM ASSET_INPUT WHERE ID = ? LIMIT 1");
            documentQuery.setLong(1, id);

            ResultSet documentQueryResult = documentQuery.executeQuery();

            if (documentQueryResult.next()) {
                AssetInput document = mapResultSet(documentQueryResult);

                PreparedStatement transactionsQuery = db.prepareStatement("SELECT * FROM ASSET_INPUT_TRANSACTIONS WHERE ASSET_INPUT_ID = ?");
                transactionsQuery.setLong(1, id);

                ResultSet transactionsQueryResult = transactionsQuery.executeQuery();

                while (transactionsQueryResult.next()) {
                    AssetInputTransaction transaction = mapTransactionResultSet(transactionsQueryResult);
                    document.insertTransaction(transaction);
                }

                assetInput = Optional.of(document);
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Greška pri dohvaćanju primke!", e);
        }

        return assetInput;
    }

    @Override
    public List<AssetInput> getMany() throws DatabaseException {
        List<AssetInput> assetInputList = new ArrayList<>();

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("SELECT * FROM ASSET_INPUT");
            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()) {
                assetInputList.add(mapResultSet(resultSet));
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException(e);
        }

        return assetInputList;
    }

    @Override
    public Long save(AssetInput assetInput) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            Long documentId;
            db.setAutoCommit(false);

            PreparedStatement insertDocumentQuery = db.prepareStatement("INSERT INTO ASSET_INPUT(DATE, AMOUNT, INVOICE_ID, INVOICE_DATE, PARTNER_ID) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertDocumentQuery.setDate(1, Date.valueOf(assetInput.getInputDate()));
            insertDocumentQuery.setBigDecimal(2, assetInput.getAmount());
            insertDocumentQuery.setString(3, assetInput.getInvoiceId());
            insertDocumentQuery.setDate(4, Date.valueOf(assetInput.getInvoiceDate()));
            insertDocumentQuery.setLong(5, assetInput.getSupplier().getId());
            insertDocumentQuery.executeUpdate();

            try (ResultSet generatedKeys = insertDocumentQuery.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    documentId = generatedKeys.getLong(1);

                    assetInput.setId(documentId);
                    saveAssetInputRows(db, documentId, assetInput.getTransactions());
                    saveIntoStorageJournal(db, documentId, assetInput.getInputDate(), assetInput.getTransactions());
                } else {
                    throw new SQLException("Neuspješno kreiranje dokumenta! ID nije dohvaćen.");
                }
            }

            createFinancialTransaction(db, assetInput);

            db.commit();
            return documentId;
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške pri spremanju primke!", e);
        }
    }

    public void save(Long id, AssetInput assetInput) throws DatabaseException {
        if (Optional.ofNullable(assetInput.getId()).isEmpty())
            assetInput.setId(id);

        try (Connection db = Database.connectToDatabase()) {
            db.setAutoCommit(false);

            PreparedStatement insertDocumentQuery = db.prepareStatement("INSERT INTO ASSET_INPUT(ID, DATE, AMOUNT, INVOICE_ID, INVOICE_DATE, PARTNER_ID) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertDocumentQuery.setLong(1, id);
            insertDocumentQuery.setDate(2, Date.valueOf(assetInput.getInputDate()));
            insertDocumentQuery.setBigDecimal(3, assetInput.getAmount());
            insertDocumentQuery.setString(4, assetInput.getInvoiceId());
            insertDocumentQuery.setDate(5, Date.valueOf(assetInput.getInvoiceDate()));
            insertDocumentQuery.setLong(6, assetInput.getSupplier().getId());
            insertDocumentQuery.executeUpdate();

            saveAssetInputRows(db, id, assetInput.getTransactions());
            saveIntoStorageJournal(db, id, assetInput.getInputDate(), assetInput.getTransactions());
            createFinancialTransaction(db, assetInput);

            db.commit();
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške pri spremanju primke!", e);
        }
    }

    private void saveAssetInputRows(Connection db, Long documentId, List<AssetInputTransaction> transactions) throws SQLException {
        int affectedRows = 0;

        for (AssetInputTransaction transaction : transactions) {
            PreparedStatement insertQuery = db.prepareStatement("INSERT INTO ASSET_INPUT_TRANSACTIONS(ASSET_INPUT_ID, ARTICLE_ID, QUANTITY, DISCOUNT, PRICE) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertQuery.setLong(1, documentId);
            insertQuery.setLong(2, transaction.getArticle().getId());
            insertQuery.setBigDecimal(3, transaction.getQuantity());
            insertQuery.setBigDecimal(4, transaction.getDiscount());
            insertQuery.setBigDecimal(5, transaction.getPrice());
            affectedRows += insertQuery.executeUpdate();

            try (ResultSet generatedKeys = insertQuery.getGeneratedKeys()) {
                if (generatedKeys.next()) transaction.setId(generatedKeys.getLong(1));
                else throw new SQLException("Neuspješno kreiranje dokumenta! ID nije dohvaćen.");
            }
        }

        if (affectedRows != transactions.size())
            throw new SQLException("Zapisi nisu uspješno uneseni u bazu podataka!");
    }

    private void updateAssetInputRows(Connection db, Long documentId, List<AssetInputTransaction> transactions) throws SQLException {
        PreparedStatement deleteTransactionsQuery = db.prepareStatement("DELETE FROM ASSET_INPUT_TRANSACTIONS WHERE ASSET_INPUT_ID = ?");
        deleteTransactionsQuery.setLong(1, documentId);
        deleteTransactionsQuery.executeUpdate();

        saveAssetInputRows(db, documentId, transactions);
    }

    private void saveIntoStorageJournal(Connection db, Long assetInputId, LocalDate date, List<AssetInputTransaction> transactions) throws SQLException {
        int affectedRows = 0;

        for (AssetInputTransaction transaction : transactions) {
            PreparedStatement insertQuery = db.prepareStatement("INSERT INTO JOURNAL_STORAGE(ARTICLE_ID, ASSET_INPUT_ID, INPUT, OWES, DATE) VALUES (?, ?, ?, ?, ?)");
            insertQuery.setLong(1, transaction.getArticle().getId());
            insertQuery.setLong(2, assetInputId);
            insertQuery.setBigDecimal(3, transaction.getQuantity());
            insertQuery.setBigDecimal(4, transaction.getTotal());
            insertQuery.setDate(5, Date.valueOf(date));
            affectedRows += insertQuery.executeUpdate();
        }

        if (affectedRows != transactions.size())
            throw new SQLException("Zapisi nisu uspješno uneseni u bazu podataka!");
    }

    private void updateStorageJournal(Connection db, Long assetInputId, LocalDate date, List<AssetInputTransaction> transactions) throws SQLException {
        PreparedStatement deleteQuery = db.prepareStatement("DELETE FROM JOURNAL_STORAGE WHERE ASSET_INPUT_ID = ?");
        deleteQuery.setLong(1, assetInputId);
        deleteQuery.executeUpdate();

        saveIntoStorageJournal(db, assetInputId, date, transactions);
    }

    @Override
    public void update(Long id, AssetInput assetInput) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            db.setAutoCommit(false);

            PreparedStatement updateDocumentQuery = db.prepareStatement("UPDATE ASSET_INPUT SET DATE = ?, AMOUNT = ?, PARTNER_ID = ?, INVOICE_ID = ?, INVOICE_DATE = ? WHERE ID = ?");
            updateDocumentQuery.setDate(1, Date.valueOf(assetInput.getInputDate()));
            updateDocumentQuery.setBigDecimal(2, assetInput.getAmount());
            updateDocumentQuery.setLong(3, assetInput.getSupplier().getId());
            updateDocumentQuery.setString(4, assetInput.getInvoiceId());
            updateDocumentQuery.setDate(5, Date.valueOf(assetInput.getInvoiceDate()));
            updateDocumentQuery.setLong(6, id);
            updateDocumentQuery.executeUpdate();

            updateFinancialTransaction(db, assetInput);
            updateStorageJournal(db, id, assetInput.getInputDate(), assetInput.getTransactions());
            updateAssetInputRows(db, id, assetInput.getTransactions());

            db.commit();
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške pri spremanju primke!", e);
        }
    }

    private void createFinancialTransaction(Connection db, AssetInput assetInput) throws SQLException {
        Optional<String> stockAccount = MainApplication.getAccountParameter(AccountParameters.ASSET_INPUT_STOCK);
        Optional<String> supplierAccount = MainApplication.getAccountParameter(AccountParameters.ASSET_INPUT_SUPPLIER);

        if (supplierAccount.isEmpty() || stockAccount.isEmpty())
            throw new SQLException("Parametri dokumenta nisu ispravni!");

        PreparedStatement createStorageFinancialTransactionQuery = db.prepareStatement("INSERT INTO JOURNAL_FINANCIAL SET ACCOUNT_CODE = ?, DATE = ?, OWES = ?, DESCRIPTION = ?, ASSET_INPUT_ID = ?");
        createStorageFinancialTransactionQuery.setString(1, stockAccount.get());
        createStorageFinancialTransactionQuery.setDate(2, Date.valueOf(assetInput.getInputDate()));
        createStorageFinancialTransactionQuery.setBigDecimal(3, assetInput.getTransactionsTotalAmount());
        createStorageFinancialTransactionQuery.setString(4, "Primka " + assetInput.getId());
        createStorageFinancialTransactionQuery.setLong(5, assetInput.getId());
        createStorageFinancialTransactionQuery.executeUpdate();

        PreparedStatement createPartnerFinancialTransactionQuery = db.prepareStatement("INSERT INTO JOURNAL_FINANCIAL SET ACCOUNT_CODE = ?, DATE = ?, CLAIMS = ?, DESCRIPTION = ?, ASSET_INPUT_ID = ?, PARTNER_ID = ?");
        createPartnerFinancialTransactionQuery.setString(1, supplierAccount.get());
        createPartnerFinancialTransactionQuery.setDate(2, Date.valueOf(assetInput.getInputDate()));
        createPartnerFinancialTransactionQuery.setBigDecimal(3, assetInput.getTransactionsTotalAmount());
        createPartnerFinancialTransactionQuery.setString(4, "Primka " + assetInput.getId());
        createPartnerFinancialTransactionQuery.setLong(5, assetInput.getId());
        createPartnerFinancialTransactionQuery.setLong(6, assetInput.getSupplier().getId());
        createPartnerFinancialTransactionQuery.executeUpdate();
    }

    private void updateFinancialTransaction(Connection db, AssetInput assetInput) throws SQLException {
        PreparedStatement deleteOldFinancialTransactionsQuery = db.prepareStatement("DELETE FROM JOURNAL_FINANCIAL WHERE ASSET_INPUT_ID = ?");
        deleteOldFinancialTransactionsQuery.setLong(1, assetInput.getId());
        deleteOldFinancialTransactionsQuery.executeUpdate();

        createFinancialTransaction(db, assetInput);
    }

    @Override
    public void delete(AssetInput assetInput) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            db.setAutoCommit(false);

            PreparedStatement transactionsQuery = db.prepareStatement("DELETE FROM ASSET_INPUT_TRANSACTIONS WHERE ASSET_INPUT_ID = ?");
            transactionsQuery.setLong(1, assetInput.getId());
            transactionsQuery.executeUpdate();

            PreparedStatement documentQuery = db.prepareStatement("DELETE FROM ASSET_INPUT WHERE ID = ?");
            documentQuery.setLong(1, assetInput.getId());
            documentQuery.executeUpdate();

            db.commit();
        } catch (SQLException | IOException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public AssetInput mapResultSet(ResultSet resultSet) throws SQLException, DatabaseException {
        AssetInputBuilder builder = new AssetInputBuilder();
        builder.setId(resultSet.getLong("ID"));
        builder.setInputDate(resultSet.getDate("DATE").toLocalDate());
        builder.setAmount(resultSet.getBigDecimal("AMOUNT"));
        builder.setInvoiceId(resultSet.getString("INVOICE_ID"));
        builder.setInvoiceDate(resultSet.getDate("INVOICE_DATE").toLocalDate());

        BusinessRepository businessRepository = new BusinessRepository();
        Optional<Business> partner = businessRepository.get(resultSet.getLong("PARTNER_ID"));
        partner.ifPresent(builder::setSupplier);

        return builder.build();
    }

    public AssetInputTransaction mapTransactionResultSet(ResultSet resultSet) throws SQLException, DatabaseException {
        Long articleId = resultSet.getLong("ARTICLE_ID");
        BigDecimal quantity = resultSet.getBigDecimal("QUANTITY");
        BigDecimal discount = resultSet.getBigDecimal("DISCOUNT");
        BigDecimal price = resultSet.getBigDecimal("PRICE");

        Optional<Article> article = new ArticleRepository().get(articleId);

        return new AssetInputTransaction(article.get(), quantity, discount, price);
    }
}
