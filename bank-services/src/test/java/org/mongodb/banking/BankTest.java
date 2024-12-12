package org.mongodb.banking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.banking.config.MongodbConfig;
import org.mongodb.banking.repository.BankRepository;
import org.mongodb.banking.repository.BankRepositoryImpl;

import static org.junit.Assert.assertEquals;

public class BankTest {

    private BankRepository repo;
    private Bank bank;
    private final String name = "Alice";
    private final int initialBalance = 5000;

    @Before
    public void setUp() throws Exception {
        repo = new BankRepositoryImpl(MongodbConfig.getDatabase("banking", "mongodb://127.0.0.1:27017"));

        repo.createAccount(name, initialBalance);
        bank = new Bank(name, repo);
    }

    @After
    public void tearDown() throws Exception {
        repo.deleteAccount(name);
    }

    @Test
    public void getName() {
        assertEquals("Alice", bank.getName());
    }

    @Test
    public void getBalance() {
        assertEquals(initialBalance, bank.getBalance());
    }

    @Test
    public void deposit() {
        int depositAmount = 500;

        bank.deposit(depositAmount, "abc123");
        assertEquals(initialBalance + depositAmount, bank.getBalance());
    }

    @Test
    public void depositDuplicateIgnored() {
        int depositAmount = 500;

        bank.deposit(depositAmount, "abc123"); // should be processed
        bank.deposit(depositAmount, "abc123"); // should be ignored
        bank.deposit(depositAmount, "abc123"); // should also be ignored

        assertEquals(initialBalance + depositAmount, bank.getBalance());
    }

    @Test
    public void withdraw() {
        int withdrawAmount = 250;

        bank.withdraw(withdrawAmount, "xyz789");
        assertEquals(initialBalance - withdrawAmount, bank.getBalance());
    }

    @Test
    public void withdrawDuplicateIgnored() {
        int withdrawAmount = 250;

        bank.withdraw(withdrawAmount, "xyz789");   // should be processed
        bank.withdraw(withdrawAmount, "xyz789");   // should be ignored
        bank.withdraw(withdrawAmount, "xyz789");   // should also be ignored

        assertEquals(initialBalance - withdrawAmount, bank.getBalance());
    }
}