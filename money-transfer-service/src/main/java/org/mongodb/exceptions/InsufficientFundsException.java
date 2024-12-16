package org.mongodb.exceptions;

/**
 * This exception is thrown when the client attempts to withdraw an amount
 * that exceeds the account balance.
 */
public class InsufficientFundsException extends AccountOperationException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
