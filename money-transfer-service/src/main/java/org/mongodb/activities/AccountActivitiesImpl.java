package org.mongodb.activities;

import io.temporal.activity.Activity;

import org.mongodb.bankapi.BankingApiClient;
import org.mongodb.models.TransactionDetails;
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

    // used to make HTTP requests to the banking service. It does not use Temporal APIs.
    private final BankingApiClient client;

    public AccountActivitiesImpl() {
        client = new BankingApiClient("localhost", 8080);
    }

    @Override
    public String withdraw(TransactionDetails input) {
        logger.info("Starting withdraw operation");

        String transactionId = null;
        try {
            transactionId = client.withdraw(input.getSender(), input.getAmount(), input.getReferenceId());
        } catch (Exception e) {
            logger.error("Withdraw operation failed", e);
            throw Activity.wrap(e);
        }

        logger.info("Withdrawal operation complete. Transaction ID is {}", transactionId);
        return transactionId;
    }

    @Override
    public String deposit(TransactionDetails input) {
        logger.info("Starting deposit operation");

        String transactionId = null;
        try {
            transactionId = client.deposit(input.getRecipient(), input.getAmount(), input.getReferenceId());
        } catch (Exception e) {
            logger.error("Withdraw operation failed", e);
            throw Activity.wrap(e);
        }

        logger.info("Deposit operation complete. Transaction ID is {}", transactionId);
        return transactionId;
    }
}
