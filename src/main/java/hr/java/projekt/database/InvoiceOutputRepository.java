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
import hr.java.projekt.model.invoices.Invoice;
import hr.java.projekt.model.invoices.InvoiceOutput;
import hr.java.projekt.model.invoices.InvoiceOutputBuilder;
import hr.java.projekt.model.invoices.InvoicePayment;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceOutputRepository implements Dao<InvoiceOutput> {

    @Override
    public Optional<InvoiceOutput> get(Long id) throws DatabaseException {
        Optional<InvoiceOutput> invoice = Optional.empty();

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement documentQuery = db.prepareStatement("SELECT * FROM INVOICE_OUTPUT WHERE ID = ? LIMIT 1");
            documentQuery.setLong(1, id);

            ResultSet documentQueryResult = documentQuery.executeQuery();

            if (documentQueryResult.next()) {
                InvoiceOutput document = mapResultSet(documentQueryResult);

                PreparedStatement transactionsQuery = db.prepareStatement("SELECT * FROM INVOICE_OUTPUT_TRANSACTIONS WHERE INVOICE_OUTPUT_ID = ?");
                transactionsQuery.setLong(1, id);

                ResultSet transactionsQueryResult = transactionsQuery.executeQuery();

                while (transactionsQueryResult.next()) {
                    ArticleTransaction transaction = mapTransactionResultSet(transactionsQueryResult);
                    document.insertTransaction(transaction);
                }


                PreparedStatement paymentsQuery = db.prepareStatement("SELECT * FROM PAYMENTS WHERE INVOICE_OUTPUT_ID = ?");
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
    public List<InvoiceOutput> getMany() throws DatabaseException {
        List<InvoiceOutput> invoices = new ArrayList<>();

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("SELECT * FROM INVOICE_OUTPUT");

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
    public Long save(InvoiceOutput invoice) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            db.setAutoCommit(false);

            PreparedStatement insertDocumentQuery = db.prepareStatement("INSERT INTO INVOICE_OUTPUT(PARTNER_ID, INVOICE_DATE, DUE_DATE, DELIVERY_DATE, AMOUNT, OPERATOR_ID) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertDocumentQuery.setLong(1, invoice.getPartner().getId());
            insertDocumentQuery.setDate(2, Date.valueOf(invoice.getInvoiceDate()));
            insertDocumentQuery.setDate(3, Date.valueOf(invoice.getDueDate()));
            insertDocumentQuery.setDate(4, Date.valueOf(invoice.getDeliveryDate()));
            insertDocumentQuery.setBigDecimal(5, invoice.getTotalAmountWithVAT());
            insertDocumentQuery.setLong(6, MainApplication.operator.getId());
            insertDocumentQuery.executeUpdate();

            try (ResultSet generatedKeys = insertDocumentQuery.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long documentId = generatedKeys.getLong(1);

                    invoice.setId(documentId);
                    saveInvoiceOutputRows(db, documentId, invoice.getTransactions());
                    saveIntoStorageJournal(db, documentId, invoice.getDeliveryDate(), invoice.getTransactions());
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

    public void save(Long documentId, InvoiceOutput invoice) throws DatabaseException {
        if (Optional.ofNullable(invoice.getId()).isEmpty())
            invoice.setId(documentId);

        try (Connection db = Database.connectToDatabase()) {
            db.setAutoCommit(false);

            PreparedStatement insertDocumentQuery = db.prepareStatement("INSERT INTO INVOICE_OUTPUT(ID, PARTNER_ID, INVOICE_DATE, DUE_DATE, DELIVERY_DATE, AMOUNT, OPERATOR_ID) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertDocumentQuery.setLong(1, invoice.getId());
            insertDocumentQuery.setLong(2, invoice.getPartner().getId());
            insertDocumentQuery.setDate(3, Date.valueOf(invoice.getInvoiceDate()));
            insertDocumentQuery.setDate(4, Date.valueOf(invoice.getDueDate()));
            insertDocumentQuery.setDate(5, Date.valueOf(invoice.getDeliveryDate()));
            insertDocumentQuery.setBigDecimal(6, invoice.getTotalAmountWithVAT());
            insertDocumentQuery.setLong(7, MainApplication.operator.getId());
            insertDocumentQuery.executeUpdate();

            saveInvoiceOutputRows(db, documentId, invoice.getTransactions());
            saveIntoStorageJournal(db, documentId, invoice.getDeliveryDate(), invoice.getTransactions());
            createFinancialTransaction(db, invoice);
            savePayments(db, documentId, invoice.getPayments());

            db.commit();
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške pri spremanju računa!", e);
        }
    }

    private void createFinancialTransaction(Connection db, Invoice invoice) throws SQLException {
        Optional<String> buyerAccount = MainApplication.getAccountParameter(AccountParameters.INVOICE_OUTPUT_BUYER);
        Optional<String> vatAccount = MainApplication.getAccountParameter(AccountParameters.INVOICE_OUTPUT_VAT);
        Optional<String> stockAccount = MainApplication.getAccountParameter(AccountParameters.INVOICE_OUTPUT_STOCK);
        Optional<String> revenueAccount = MainApplication.getAccountParameter(AccountParameters.INVOICE_OUTPUT_REVENUE);
        Optional<String> purchasePriceAccount = MainApplication.getAccountParameter(AccountParameters.INVOICE_OUTPUT_PURCHASE_PRICE);
        String description = "Račun-otpremnica " + invoice.getId();

        if (buyerAccount.isEmpty() || stockAccount.isEmpty() || vatAccount.isEmpty() || revenueAccount.isEmpty() || purchasePriceAccount.isEmpty())
            throw new SQLException("Parametri dokumenta nisu ispravni!");

        PreparedStatement createPartnerFinancialTransactionQuery = db.prepareStatement("INSERT INTO JOURNAL_FINANCIAL SET ACCOUNT_CODE = ?, DATE = ?, OWES = ?, DESCRIPTION = ?, INVOICE_OUTPUT_ID = ?, PARTNER_ID = ?, DUE_DATE = ?");
        createPartnerFinancialTransactionQuery.setString(1, buyerAccount.get());
        createPartnerFinancialTransactionQuery.setDate(2, Date.valueOf(invoice.getInvoiceDate()));
        createPartnerFinancialTransactionQuery.setBigDecimal(3, invoice.getTotalAmountWithVAT());
        createPartnerFinancialTransactionQuery.setString(4, "Račun-otpremnica " + invoice.getId());
        createPartnerFinancialTransactionQuery.setLong(5, invoice.getId());
        createPartnerFinancialTransactionQuery.setLong(6, invoice.getPartner().getId());
        createPartnerFinancialTransactionQuery.setDate(7, Date.valueOf(invoice.getDueDate()));
        createPartnerFinancialTransactionQuery.executeUpdate();

        PreparedStatement owesTransactionQuery = db.prepareStatement("INSERT INTO JOURNAL_FINANCIAL SET ACCOUNT_CODE = ?, DATE = ?, OWES = ?, DESCRIPTION = ?, INVOICE_OUTPUT_ID = ?");
        PreparedStatement claimsTransactionQuery = db.prepareStatement("INSERT INTO JOURNAL_FINANCIAL SET ACCOUNT_CODE = ?, DATE = ?, CLAIMS = ?, DESCRIPTION = ?, INVOICE_OUTPUT_ID = ?");

        BigDecimal purchasePrice = invoice.getTotalAssetPurchasePrice();
        BigDecimal revenue = invoice.getTotalBasisAmount();

        executeNonAnalyticalQuery(claimsTransactionQuery, vatAccount.get(), invoice.getInvoiceDate(), invoice.getTotalTaxAmount(), description, invoice.getId());
        executeNonAnalyticalQuery(claimsTransactionQuery, stockAccount.get(), invoice.getInvoiceDate(), purchasePrice, description, invoice.getId());
        executeNonAnalyticalQuery(owesTransactionQuery, purchasePriceAccount.get(), invoice.getInvoiceDate(), purchasePrice, description, invoice.getId());
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

    private void saveIntoStorageJournal(Connection db, Long documentId, LocalDate deliveryDate, List<ArticleTransaction> transactions) throws SQLException {
        int affectedRows = 0;

        for (ArticleTransaction transaction : transactions) {
            PreparedStatement insertQuery = db.prepareStatement("INSERT INTO JOURNAL_STORAGE(ARTICLE_ID, INVOICE_OUTPUT_ID, OUTPUT, CLAIMS, DATE) VALUES (?, ?, ?, ?, ?)");
            insertQuery.setLong(1, transaction.getArticle().getId());
            insertQuery.setLong(2, documentId);
            insertQuery.setBigDecimal(3, transaction.getQuantity());
            insertQuery.setBigDecimal(4, transaction.getTotal());
            insertQuery.setDate(5, Date.valueOf(deliveryDate));
            affectedRows += insertQuery.executeUpdate();
        }

        if (affectedRows != transactions.size())
            throw new SQLException("Zapisi nisu uspješno uneseni u bazu podataka!");
    }

    private void saveInvoiceOutputRows(Connection db, Long documentId, List<ArticleTransaction> transactions) throws SQLException {
        int affectedRows = 0;

        for (ArticleTransaction transaction : transactions) {
            PreparedStatement insertQuery = db.prepareStatement("INSERT INTO INVOICE_OUTPUT_TRANSACTIONS(INVOICE_OUTPUT_ID, ARTICLE_ID, QUANTITY, DISCOUNT, PRICE) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
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
            PreparedStatement insertQuery = db.prepareStatement("INSERT INTO PAYMENTS(INVOICE_OUTPUT_ID, DATE, AMOUNT) VALUES (?, ?, ?)");
            insertQuery.setLong(1, documentId);
            insertQuery.setDate(2, Date.valueOf(payment.getDate()));
            insertQuery.setBigDecimal(3, payment.getAmount());
            affectedRows += insertQuery.executeUpdate();
        }

        if (affectedRows != payments.size())
            throw new SQLException("Zapisi uplata nisu uspješno uneseni u bazu podataka!");
    }

    @Override
    public void update(Long id, InvoiceOutput invoice) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()) {
            db.setAutoCommit(false);

            PreparedStatement updateDocumentQuery = db.prepareStatement("UPDATE INVOICE_OUTPUT SET INVOICE_DATE = ?, DUE_DATE = ?, DELIVERY_DATE = ?, AMOUNT = ?, PARTNER_ID = ? WHERE ID = ?");
            updateDocumentQuery.setDate(1, Date.valueOf(invoice.getInvoiceDate()));
            updateDocumentQuery.setDate(2, Date.valueOf(invoice.getDueDate()));
            updateDocumentQuery.setDate(3, Date.valueOf(invoice.getDeliveryDate()));
            updateDocumentQuery.setBigDecimal(4, invoice.getTotalAmountWithVAT());
            updateDocumentQuery.setLong(5, invoice.getPartner().getId());
            updateDocumentQuery.setLong(6, id);
            updateDocumentQuery.executeUpdate();

            updateFinancialTransaction(db, invoice);
            updateStorageJournal(db, id, invoice.getDeliveryDate(), invoice.getTransactions());
            updateInvoiceOutputRows(db, id, invoice.getTransactions());
            updatePayments(db, id, invoice.getPayments());

            db.commit();
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Došlo je do pogreške pri spremanju računa!", e);
        }
    }

    private void updateFinancialTransaction(Connection db, Invoice invoice) throws SQLException {
        PreparedStatement deleteOldFinancialTransactionsQuery = db.prepareStatement("DELETE FROM JOURNAL_FINANCIAL WHERE INVOICE_OUTPUT_ID = ?");
        deleteOldFinancialTransactionsQuery.setLong(1, invoice.getId());
        deleteOldFinancialTransactionsQuery.executeUpdate();

        createFinancialTransaction(db, invoice);
    }

    private void updateInvoiceOutputRows(Connection db, Long documentId, List<ArticleTransaction> transactions) throws SQLException {
        PreparedStatement deleteOldFinancialTransactionsQuery = db.prepareStatement("DELETE FROM INVOICE_OUTPUT_TRANSACTIONS WHERE INVOICE_OUTPUT_ID = ?");
        deleteOldFinancialTransactionsQuery.setLong(1, documentId);
        deleteOldFinancialTransactionsQuery.executeUpdate();

        saveInvoiceOutputRows(db, documentId, transactions);
    }

    private void updateStorageJournal(Connection db, Long documentId, LocalDate deliveryDate, List<ArticleTransaction> transactions) throws SQLException {
        PreparedStatement deleteOldFinancialTransactionsQuery = db.prepareStatement("DELETE FROM JOURNAL_STORAGE WHERE INVOICE_OUTPUT_ID = ?");
        deleteOldFinancialTransactionsQuery.setLong(1, documentId);
        deleteOldFinancialTransactionsQuery.executeUpdate();

        saveIntoStorageJournal(db, documentId, deliveryDate, transactions);
    }

    private void updatePayments(Connection db, Long id, List<InvoicePayment> payments) throws SQLException {
        PreparedStatement deleteOldFinancialTransactionsQuery = db.prepareStatement("DELETE FROM PAYMENTS WHERE INVOICE_OUTPUT_ID = ?");
        deleteOldFinancialTransactionsQuery.setLong(1, id);
        deleteOldFinancialTransactionsQuery.executeUpdate();

        savePayments(db, id, payments);
    }

    @Override
    public void delete(InvoiceOutput invoice) throws DatabaseException {
        try (Connection db = Database.connectToDatabase()){
            db.setAutoCommit(false);

            PreparedStatement deleteQuery = db.prepareStatement("DELETE FROM INVOICE_OUTPUT WHERE ID = ?");
            deleteQuery.setLong(1, invoice.getId());
            deleteQuery.executeUpdate();

            db.commit();
        } catch (SQLException | IOException ex) {
            throw new DatabaseException("Došlo je do pogreške kod brisanja računa!", ex);
        }
    }

    @Override
    public InvoiceOutput mapResultSet(ResultSet resultSet) throws SQLException, DatabaseException {
        InvoiceOutputBuilder builder = new InvoiceOutputBuilder();

        builder.setId(resultSet.getLong("ID"));
        builder.setInvoiceDate(resultSet.getDate("INVOICE_DATE").toLocalDate());
        builder.setDueDate(resultSet.getDate("DUE_DATE").toLocalDate());
        builder.setDeliveryDate(resultSet.getDate("DELIVERY_DATE").toLocalDate());
        builder.setAmount(resultSet.getBigDecimal("AMOUNT"));

        Optional<Business> partner = new BusinessRepository().get(resultSet.getLong("PARTNER_ID"));
        partner.ifPresent(builder::setPartner);

        return builder.build();
    }

    public List<InvoiceOutput> getDueInvoices() throws DatabaseException {
        List<InvoiceOutput> invoices = new ArrayList<>();

        try (Connection db = Database.connectToDatabase()) {
            PreparedStatement query = db.prepareStatement("SELECT ID, PARTNER_ID, INVOICE_DATE, DUE_DATE, DELIVERY_DATE, (AMOUNT-IFNULL((SELECT IFNULL(SUM(AMOUNT), 0) FROM PAYMENTS WHERE INVOICE_OUTPUT_ID = INVOICE_OUTPUT.ID GROUP BY INVOICE_OUTPUT_ID), 0)) as \"AMOUNT\" FROM INVOICE_OUTPUT WHERE DUE_DATE <= CURRENT_DATE");
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
