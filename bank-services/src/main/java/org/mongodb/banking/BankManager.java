package org.mongodb.banking;

import org.mongodb.banking.repository.BankRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankManager {
    private final Map<String, BankService> banks = new HashMap<>();
    private final BankRepository repository;

    public BankManager(BankRepository repository) {
        this.repository = repository;
    }

    public BankService getOrCreateBank(String bankName) {
        return banks.computeIfAbsent(bankName, name -> new BankService(repository));
    }

    List<String> getAllBankNames() {
        return repository.getAllBankNames();
    }

}
