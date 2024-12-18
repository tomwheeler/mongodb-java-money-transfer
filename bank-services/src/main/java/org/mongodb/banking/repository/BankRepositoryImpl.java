package org.mongodb.banking.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class BankRepositoryImpl implements BankRepository {

    private final MongoCollection<Document> accountsCollection;
    private final MongoCollection<Document> transactionsCollection;

    public BankRepositoryImpl(MongoDatabase database) {
        this.accountsCollection = database.getCollection("accounts");
        this.transactionsCollection = database.getCollection("transactions");
    }

    @Override
    public Document findAccountByBankName(String bankName) {
        return accountsCollection.find(eq("bankName", bankName)).first();
    }

    @Override
    public void updateBalance(String bankName, int newBalance) {
        accountsCollection.updateOne(eq("bankName", bankName), set("balance", newBalance));
    }

    @Override
    public void logTransaction(String type, int amount, String transactionId, String idempotencyKey, String bankName) {
        Document transaction = new Document("type", type)
            .append("amount", amount)
            .append("transactionId", transactionId)
            .append("idempotencyKey", idempotencyKey)
            .append("bankName", bankName);
        transactionsCollection.insertOne(transaction);
    }

    @Override
    public void createAccount(String bankName, int initialBalance) {
        if (bankName == null || bankName.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid bank name '" + bankName + "'");
        }

        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance must be >= 0");
        }

        accountsCollection.insertOne(new Document("bankName", bankName).append("balance", initialBalance));
    }

    @Override
    public boolean deleteAccount(String bankName) {
        if (bankName == null || bankName.trim().isEmpty()) {
            return false;
        }

        Bson query = eq("bankName", bankName);
        accountsCollection.deleteMany(query);

        return true;
    }

    @Override
    public List<String> getAllBankNames() {
        return StreamSupport.stream(accountsCollection.find().spliterator(), false)
            .map(doc -> doc.getString("bankName"))
            .collect(Collectors.toList());
    }
}
