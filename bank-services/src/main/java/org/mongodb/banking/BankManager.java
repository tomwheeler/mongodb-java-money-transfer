package org.mongodb.banking;

import org.mongodb.banking.repository.BankRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankManager {

    private static final Logger logger = LoggerFactory.getLogger(BankManager.class);

    private final Map<String, BankService> banks = new HashMap<>();
    private final BankRepository repository;

    public BankManager(BankRepository repository) {
        logger.debug("Creating BankManager instance");

        this.repository = repository;
    }

    public BankService getOrCreateBank(String bankName) {
        return banks.computeIfAbsent(bankName, name -> new BankService(repository));
    }

    List<String> getAllBankNames() {
        return repository.getAllBankNames();
    }

}