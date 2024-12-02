package org.mongodb.banking;

import org.bson.Document;
import org.mongodb.banking.repository.BankRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BankService {
    private final BankRepository repository;
    private final Set<String> unavailableBanks = new HashSet<>(); // Tracks "offline" banks

    public BankService(BankRepository repository) {
        this.repository = repository;
    }

    public void stopBank(String bankName) {
        unavailableBanks.add(bankName);
    }

    public void startBank(String bankName) {
        unavailableBanks.remove(bankName);
    }

    private void checkAvailability(String bankName) {
        if (unavailableBanks.contains(bankName)) {
            throw new IllegalStateException("Bank " + bankName + " is currently unavailable.");
        }
    }

    public void createBank(String bankName, int initialBalance) {
        checkAvailability(bankName);
        Document account = repository.findAccountByBankName(bankName);
        if (account != null) {
            throw new IllegalArgumentException("Bank with name " + bankName + " already exists.");
        }
        repository.createAccount(bankName, initialBalance);
    }

    public int getBalance(String bankName) {
        checkAvailability(bankName);
        Document account = repository.findAccountByBankName(bankName);
        if (account == null) {
            throw new IllegalArgumentException("Bank with name " + bankName + " does not exist.");
        }
        return account.getInteger("balance");
    }

    public String deposit(String bankName, int amount, String idempotencyKey) {
        checkAvailability(bankName);
        Bank bank = new Bank(bankName, repository);
        return bank.deposit(amount, idempotencyKey);
    }

    public String withdraw(String bankName, int amount, String idempotencyKey) {
        checkAvailability(bankName);
        Bank bank = new Bank(bankName, repository);
        return bank.withdraw(amount, idempotencyKey);
    }

    /**
     * Fetches all bank names from the repository.
     */
    public List<String> getAllBankNames() {
        return repository.getAllBankNames();
    }

}
