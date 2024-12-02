package org.mongodb.banking.repository;

import org.bson.Document;

import java.util.List;

public interface BankRepository {
    Document findAccountByBankName(String bankName);

    void updateBalance(String bankName, int newBalance);

    void logTransaction(String type, int amount, String transactionId, String idempotencyKey, String bankName);

    void createAccount(String bankName, int initialBalance);

    List<String> getAllBankNames();
}
