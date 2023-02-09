/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.database;

import hr.java.projekt.app.MainApplication;
import hr.java.projekt.exceptions.DatabaseException;
import hr.java.projekt.model.account.AccountParameters;
import hr.java.projekt.model.articles.Article;
import hr.java.projekt.model.business.Business;
import hr.java.projekt.model.inventory.ArticleTransaction;
import hr.java.projekt.model.invoices.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceRepository implements Dao<Invoice> {

    @Override
    public Optional<Invoice> get(Long id) throws DatabaseException {
        Optional<Invoice> invoice = Optional.empty();

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement documentQuery = db.prepareStatement("SELECT * FROM INVOICE WHERE ID = ? LIMIT 1");
            documentQuery.setLong(1, id);

            ResultSet documentQueryResult = documentQuery.executeQuery();

            if (documentQueryResult.next()) {
                Invoice document = mapResultSet(documentQueryResult);

                PreparedStatement transactionsQuery = db.prepareStatement("SELECT * FROM INVOICE_TRANSACTIONS WHERE INVOICE_ID = ?");
                transactionsQuery.setLong(1, id);

                ResultSet transactionsQueryResult = transactionsQuery.executeQuery();

                while (transactionsQueryResult.next()) {
                    ArticleTransaction transaction = mapTransactionResultSet(transactionsQueryResult);
                    document.insertTransaction(transaction);
                }


                PreparedStatement paymentsQuery = db.prepareStatement("SELECT * FROM PAYMENTS WHERE INVOICE_ID = ?");
                paymentsQuery.setLong(1, id);

                ResultSet paymentsQueryResult = paymentsQuery.executeQuery();

                List<InvoicePayment> payments = new ArrayList<>();
                while (paymentsQueryResult.next()) {
                    payments.add(mapInvoicePaymentResultSet(paymentsQueryResult));
                }
                document.setPayments(payments);

                invoice = Optional.of(document);
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Greška pri dohvaćanju računa!", e);
        }

        return invoice;
    }

    private ArticleTransaction mapTransactionResultSet(ResultSet resultSet) throws SQLException, DatabaseException {
        Long articleId = resultSet.getLong("ARTICLE_ID");
        BigDecimal quantity = resultSet.getBigDecimal("QUANTITY");
        BigDecimal discount = resultSet.getBigDecimal("DISCOUNT");
        BigDecimal price = resultSet.getBigDecimal("PRICE");

        Optional<Article> article = new ArticleRepository().get(articleId);

        return new ArticleTransaction(article.get(), quantity, discount, price);
    }

    private InvoicePayment mapInvoicePaymentResultSet(ResultSet resultSet) throws SQLException {
        LocalDate date = resultSet.getDate("DATE").toLocalDate();
        BigDecimal amount = resultSet.getBigDecimal("AMOUNT").setScale(2, RoundingMode.HALF_UP);

        return new InvoicePayment(date, amount);
    }

    @Override
    public List<Invoice> getMany() throws DatabaseException {
        List<Invoice> invoices = new ArrayList<>();

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("SELECT * FROM INVOICE");

            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()) {
                invoices.add(mapResultSet(resultSet));
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške pri dohvatu računa!", e);
        }

        return invoices;
    }

    @Override
    public Long save(Invoice invoice) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            db.setAutoCommit(false);

            PreparedStatement insertDocumentQuery = db.prepareStatement("INSERT INTO INVOICE(PARTNER_ID, INVOICE_DATE, DUE_DATE, AMOUNT, OPERATOR_ID) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertDocumentQuery.setLong(1, invoice.getPartner().getId());
            insertDocumentQuery.setDate(2, Date.valueOf(invoice.getInvoiceDate()));
            insertDocumentQuery.setDate(3, Date.valueOf(invoice.getDueDate()));
            insertDocumentQuery.setBigDecimal(4, invoice.getTotalAmountWithVAT());
            insertDocumentQuery.setLong(5, MainApplication.operator.getId());
            insertDocumentQuery.executeUpdate();

            try (ResultSet generatedKeys = insertDocumentQuery.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long documentId = generatedKeys.getLong(1);

                    invoice.setId(documentId);
                    saveInvoiceOutputRows(db, documentId, invoice.getTransactions());
                } else {
                    throw new SQLException("Neuspješno kreiranje dokumenta! ID nije dohvaćen.");
                }
            }

            createFinancialTransaction(db, invoice);

            db.commit();
            return invoice.getId();
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške pri spremanju računa!", e);
        }
    }

    public void save(Long documentId, Invoice invoice) throws DatabaseException {
        if (Optional.ofNullable(invoice.getId()).isEmpty())
            invoice.setId(documentId);

        try (Connection db = Database.connectToDatabase()) {
            db.setAutoCommit(false);

            PreparedStatement insertDocumentQuery = db.prepareStatement("INSERT INTO INVOICE(ID, PARTNER_ID, INVOICE_DATE, DUE_DATE, AMOUNT, OPERATOR_ID) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertDocumentQuery.setLong(1, invoice.getId());
            insertDocumentQuery.setLong(2, invoice.getPartner().getId());
            insertDocumentQuery.setDate(3, Date.valueOf(invoice.getInvoiceDate()));
            insertDocumentQuery.setDate(4, Date.valueOf(invoice.getDueDate()));
            insertDocumentQuery.setBigDecimal(5, invoice.getTotalAmountWithVAT());
            insertDocumentQuery.setLong(6, MainApplication.operator.getId());
            insertDocumentQuery.executeUpdate();

            saveInvoiceOutputRows(db, documentId, invoice.getTransactions());
            createFinancialTransaction(db, invoice);
            savePayments(db, documentId, invoice.getPayments());

            db.commit();
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške pri spremanju računa!", e);
        }
    }

    private void createFinancialTransaction(Connection db, Invoice invoice) throws SQLException {
        Optional<String> buyerAccount = MainApplication.getAccountParameter(AccountParameters.INVOICE_BUYER);
        Optional<String> vatAccount = MainApplication.getAccountParameter(AccountParameters.INVOICE_VAT);
        Optional<String> revenueAccount = MainApplication.getAccountParameter(AccountParameters.INVOICE_REVENUE);
        String description = "Račun " + invoice.getId();

        if (buyerAccount.isEmpty() || vatAccount.isEmpty() || revenueAccount.isEmpty())
            throw new SQLException("Parametri dokumenta nisu ispravni!");

        PreparedStatement createPartnerFinancialTransactionQuery = db.prepareStatement("INSERT INTO JOURNAL_FINANCIAL SET ACCOUNT_CODE = ?, DATE = ?, OWES = ?, DESCRIPTION = ?, INVOICE_ID = ?, PARTNER_ID = ?, DUE_DATE = ?");
        createPartnerFinancialTransactionQuery.setString(1, buyerAccount.get());
        createPartnerFinancialTransactionQuery.setDate(2, Date.valueOf(invoice.getInvoiceDate()));
        createPartnerFinancialTransactionQuery.setBigDecimal(3, invoice.getTotalAmountWithVAT());
        createPartnerFinancialTransactionQuery.setString(4, description);
        createPartnerFinancialTransactionQuery.setLong(5, invoice.getId());
        createPartnerFinancialTransactionQuery.setLong(6, invoice.getPartner().getId());
        createPartnerFinancialTransactionQuery.setDate(7, Date.valueOf(invoice.getDueDate()));
        createPartnerFinancialTransactionQuery.executeUpdate();

        PreparedStatement owesTransactionQuery = db.prepareStatement("INSERT INTO JOURNAL_FINANCIAL SET ACCOUNT_CODE = ?, DATE = ?, OWES = ?, DESCRIPTION = ?, INVOICE_ID = ?");
        PreparedStatement claimsTransactionQuery = db.prepareStatement("INSERT INTO JOURNAL_FINANCIAL SET ACCOUNT_CODE = ?, DATE = ?, CLAIMS = ?, DESCRIPTION = ?, INVOICE_ID = ?");
        BigDecimal revenue = invoice.getTotalBasisAmount();

        executeNonAnalyticalQuery(claimsTransactionQuery, vatAccount.get(), invoice.getInvoiceDate(), invoice.getTotalTaxAmount(), description, invoice.getId());
        executeNonAnalyticalQuery(claimsTransactionQuery, revenueAccount.get(), invoice.getInvoiceDate(), revenue, description, invoice.getId());
    }

    private void executeNonAnalyticalQuery(PreparedStatement statement, String accountCode, LocalDate date, BigDecimal amount, String description, Long id) throws SQLException {
        statement.setString(1, accountCode);
        statement.setDate(2, Date.valueOf(date));
        statement.setBigDecimal(3, amount);
        statement.setString(4, description);
        statement.setLong(5, id);
        statement.executeUpdate();
    }

    private void saveInvoiceOutputRows(Connection db, Long documentId, List<ArticleTransaction> transactions) throws SQLException {
        int affectedRows = 0;

        for (ArticleTransaction transaction : transactions) {
            PreparedStatement insertQuery = db.prepareStatement("INSERT INTO INVOICE_TRANSACTIONS(INVOICE_ID, ARTICLE_ID, QUANTITY, DISCOUNT, PRICE) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
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

    private void savePayments(Connection db, Long documentId, List<InvoicePayment> payments) throws SQLException {
        int affectedRows = 0;

        for (InvoicePayment payment : payments) {
            PreparedStatement insertQuery = db.prepareStatement("INSERT INTO PAYMENTS(INVOICE_ID, DATE, AMOUNT) VALUES (?, ?, ?)");
            insertQuery.setLong(1, documentId);
            insertQuery.setDate(2, Date.valueOf(payment.getDate()));
            insertQuery.setBigDecimal(3, payment.getAmount());
            affectedRows += insertQuery.executeUpdate();
        }

        if (affectedRows != payments.size())
            throw new SQLException("Zapisi uplata nisu uspješno uneseni u bazu podataka!");
    }

    @Override
    public void update(Long id, Invoice invoice) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            db.setAutoCommit(false);

            PreparedStatement updateDocumentQuery = db.prepareStatement("UPDATE INVOICE SET INVOICE_DATE = ?, DUE_DATE = ?, AMOUNT = ?, PARTNER_ID = ? WHERE ID = ?");
            updateDocumentQuery.setDate(1, Date.valueOf(invoice.getInvoiceDate()));
            updateDocumentQuery.setDate(2, Date.valueOf(invoice.getDueDate()));
            updateDocumentQuery.setBigDecimal(3, invoice.getTotalAmountWithVAT());
            updateDocumentQuery.setLong(4, invoice.getPartner().getId());
            updateDocumentQuery.setLong(5, id);
            updateDocumentQuery.executeUpdate();

            updateFinancialTransaction(db, invoice);
            updateInvoiceOutputRows(db, id, invoice.getTransactions());
            updatePayments(db, id, invoice.getPayments());

            db.commit();
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške pri spremanju računa!", e);
        }
    }

    private void updateFinancialTransaction(Connection db, Invoice invoice) throws SQLException {
        PreparedStatement deleteOldFinancialTransactionsQuery = db.prepareStatement("DELETE FROM JOURNAL_FINANCIAL WHERE INVOICE_ID = ?");
        deleteOldFinancialTransactionsQuery.setLong(1, invoice.getId());
        deleteOldFinancialTransactionsQuery.executeUpdate();

        createFinancialTransaction(db, invoice);
    }

    private void updateInvoiceOutputRows(Connection db, Long documentId, List<ArticleTransaction> transactions) throws SQLException {
        PreparedStatement deleteOldFinancialTransactionsQuery = db.prepareStatement("DELETE FROM INVOICE_TRANSACTIONS WHERE INVOICE_ID = ?");
        deleteOldFinancialTransactionsQuery.setLong(1, documentId);
        deleteOldFinancialTransactionsQuery.executeUpdate();

        saveInvoiceOutputRows(db, documentId, transactions);
    }

    private void updatePayments(Connection db, Long id, List<InvoicePayment> payments) throws SQLException {
        PreparedStatement deleteOldFinancialTransactionsQuery = db.prepareStatement("DELETE FROM PAYMENTS WHERE INVOICE_ID = ?");
        deleteOldFinancialTransactionsQuery.setLong(1, id);
        deleteOldFinancialTransactionsQuery.executeUpdate();

        savePayments(db, id, payments);
    }

    @Override
    public void delete(Invoice invoice) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()){
            db.setAutoCommit(false);

            PreparedStatement deleteQuery = db.prepareStatement("DELETE FROM INVOICE WHERE ID = ?");
            deleteQuery.setLong(1, invoice.getId());
            deleteQuery.executeUpdate();

            db.commit();
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Došlo je do pogreške kod brisanja računa!", ex);
        }
    }

    @Override
    public Invoice mapResultSet(ResultSet resultSet) throws SQLException, DatabaseException {
        InvoiceBuilder builder = new InvoiceBuilder();

        builder.setId(resultSet.getLong("ID"));
        builder.setInvoiceDate(resultSet.getDate("INVOICE_DATE").toLocalDate());
        builder.setDueDate(resultSet.getDate("DUE_DATE").toLocalDate());
        builder.setAmount(resultSet.getBigDecimal("AMOUNT"));

        Optional<Business> partner = new BusinessRepository().get(resultSet.getLong("PARTNER_ID"));
        partner.ifPresent(builder::setPartner);

        return builder.build();
    }

    public List<Invoice> getDueInvoices() throws DatabaseException {
        List<Invoice> invoices = new ArrayList<>();

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("SELECT ID, PARTNER_ID, INVOICE_DATE, DUE_DATE, (AMOUNT-IFNULL((SELECT IFNULL(SUM(AMOUNT), 0) FROM PAYMENTS WHERE INVOICE_ID = INVOICE.ID GROUP BY INVOICE_ID), 0)) as \"AMOUNT\" FROM INVOICE WHERE DUE_DATE <= CURRENT_DATE");
            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()){
                invoices.add(mapResultSet(resultSet));
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške pri dohvatu računa!", e);
        }

        return invoices;
    }
}
