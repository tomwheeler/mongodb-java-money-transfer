package org.mongodb.exceptions;

/**
 * Represents a business-level failure encountered by the client during
 * an operation on a bank account. This type of exception is used for
 * the general case, while its subclasses are used for specific ones.
 */
public class AccountOperationException extends RuntimeException {
    public AccountOperationException(String message) {
        super(message);
    }
}
