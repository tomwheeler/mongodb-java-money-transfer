package org.mongodb.banking.exceptions;

/**
 * Exception thrown when the amount requested in the withdrawal exceeds
 * the balance of the source account.
 */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
