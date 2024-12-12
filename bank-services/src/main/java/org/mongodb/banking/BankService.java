package org.mongodb.banking;

import org.bson.Document;
import org.mongodb.banking.repository.BankRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankService {
    
    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    private final BankRepository repository;
    private final Map<String, Bank> banks = new HashMap<>();
    private final Set<String> unavailableBanks = new HashSet<>(); // Tracks "offline" banks

    public BankService(BankRepository repository) {
        logger.debug("Creating new BankService instance");
        this.repository = repository;
    }

    public void stopBank(String bankName) {
        logger.info("Stopping bank " + bankName);
        unavailableBanks.add(bankName);
    }

    public void startBank(String bankName) {
        logger.info("Starting bank " + bankName);
        unavailableBanks.remove(bankName);
    }

    public boolean isAvailable(String bankName) {
        return !unavailableBanks.contains(bankName);
    }

    private void ensureAvailability(String bankName) {
        if (!isAvailable(bankName)) {
            logger.warn("Operation attempted on '" + bankName + "', but it is unavailable");
            throw new IllegalStateException("Bank '" + bankName + "' is currently unavailable.");
        }
    }

    public void createBank(String bankName, int initialBalance) {
        logger.info("Attempting to create bank '" + bankName + "' with balance: " + initialBalance);

        Document account = repository.findAccountByBankName(bankName);
        if (account != null) {
            throw new IllegalArgumentException("Bank with name '" + bankName + "' already exists.");
        }
        repository.createAccount(bankName, initialBalance);
    }

    public boolean deleteBank(String bankName) {
        logger.info("Attempting to delete bank '" + bankName + "'");

        Document account = repository.findAccountByBankName(bankName);
        if (account != null) {
            throw new IllegalArgumentException("Bank with name '" + bankName + "' does not exist.");
        }
        return repository.deleteAccount(bankName);
    }

    public int getBalance(String bankName) {
        logger.info("Getting balance for bank '" + bankName + "'");

        ensureAvailability(bankName);
        Document account = repository.findAccountByBankName(bankName);
        if (account == null) {
            throw new IllegalArgumentException("Bank with name '" + bankName + "' does not exist.");
        }
        return account.getInteger("balance");
    }

    public String deposit(String bankName, int amount, String idempotencyKey) {
        logger.info("Attempting deposit to bank '" + bankName + "' for " + amount);
        ensureAvailability(bankName);
        Bank bank = getBank(bankName);
        return bank.deposit(amount, idempotencyKey);
    }

    public String withdraw(String bankName, int amount, String idempotencyKey) {
        logger.info("Attempting withdraw from bank '" + bankName + "' for " + amount);

        ensureAvailability(bankName);
        Bank bank = getBank(bankName);
        return bank.withdraw(amount, idempotencyKey);
    }

    /**
     * Fetches all bank names from the repository.
     */
    public List<String> getAllBankNames() {
        return repository.getAllBankNames();
    }

    private Bank getBank(String bankName) {
        if (!repository.getAllBankNames().contains(bankName)) {
            throw new IllegalArgumentException("Bank with name '" + bankName + "' does not exist.");
        }

        Bank bank = banks.get(bankName);

        // if it exists in the repository, but not in the map, then it was added
        // to the repository in a previous session. Since the bank instance is
        // no longer cached, we must create a new instance and cache it.
        if (bank == null) {
            bank = new Bank(bankName, repository);
            banks.put(bankName, bank);
        }

        return bank;
    }
}
