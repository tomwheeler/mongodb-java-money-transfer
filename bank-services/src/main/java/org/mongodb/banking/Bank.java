package org.mongodb.banking;

import org.mongodb.banking.exceptions.InsufficientFundsException;
import org.mongodb.banking.repository.BankRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Bank {
    private final String name;
    private int balance;
    private final Map<String, String> requests = new HashMap<>();
    private final BankRepository repository;

    public Bank(String name, BankRepository repository) {
        this.name = name;
        this.repository = repository;

        var account = repository.findAccountByBankName(name);
        if (account == null) {
            repository.createAccount(name, 0);
            this.balance = 0;
        } else {
            this.balance = account.getInteger("balance");
        }
    }

    public String getName() {
        return name;
    }

    public synchronized int getBalance() {
        return balance;
    }

    public synchronized String deposit(int amount, String idempotencyKey) {
        if (amount < 1) {
            throw new IllegalArgumentException("Invalid deposit amount: " + amount);
        }

        if (requests.containsKey(idempotencyKey)) {
            return requests.get(idempotencyKey);
        }

        balance += amount;
        String txID = generateTransactionID("D", 10);
        requests.put(idempotencyKey, txID);

        repository.updateBalance(name, balance);
        repository.logTransaction("deposit", amount, txID, idempotencyKey, name);
        return txID;
    }

    public synchronized String withdraw(int amount, String idempotencyKey) {
        if (amount < 1) {
            throw new IllegalArgumentException("Invalid withdrawal amount: " + amount);
        }

        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient funds: balance=" + balance + ", withdrawal=" + amount);
        }

        if (requests.containsKey(idempotencyKey)) {
            return requests.get(idempotencyKey);
        }

        balance -= amount;
        String txID = generateTransactionID("W", 10);
        requests.put(idempotencyKey, txID);

        repository.updateBalance(name, balance);
        repository.logTransaction("withdraw", amount, txID, idempotencyKey, name);
        return txID;
    }

    private String generateTransactionID(String prefix, int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder(prefix);
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }
}
