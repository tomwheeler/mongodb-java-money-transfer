package org.mongodb.activities;

import io.temporal.activity.Activity;

import org.mongodb.bankapi.BankingApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the operations that are used to carry out the money transfer.
 * This class includes two Activities, each of which make a request to a
 * banking service. One does this for the withdrawal, while the other does
 * it for the deposit.
 */
public class AccountActivitiesImpl implements AccountActivities {
    private static final Logger logger = LoggerFactory.getLogger(AccountActivitiesImpl.class);

    // used to interact with the banking service (does not use Temporal APIs).
    private final BankingApiClient client;

    public AccountActivitiesImpl() {
        client = new BankingApiClient("localhost", 8080);
    }

    @Override
    public String withdraw(String account, int amount, String referenceId) {
        logger.info("Starting withdraw operation");

        String transactionId;
        try {
            transactionId = client.withdraw(account, amount, referenceId);
        } catch (Exception e) {
            logger.error("Withdraw operation failed", e);
            throw Activity.wrap(e);
        }

        logger.info("Withdraw operation complete. Transaction ID is {}", transactionId);
        return transactionId;
    }

    @Override
    public String deposit(String account, int amount, String referenceId) {
        logger.info("Starting deposit operation");

        String transactionId;
        try {
            transactionId = client.deposit(account, amount, referenceId);
        } catch (Exception e) {
            logger.error("Deposit operation failed", e);
            throw Activity.wrap(e);
        }

        logger.info("Deposit operation complete. Transaction ID is {}", transactionId);
        return transactionId;
    }
}
