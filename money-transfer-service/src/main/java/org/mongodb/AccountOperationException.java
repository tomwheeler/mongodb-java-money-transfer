package org.mongodb;

/**
 * Represents a business-level failure encountered during an operation on
 * a bank account. For example, the application will throw this error during
 * an attempt to withdraw an amount beyond the account balance. This is in
 * contrast to, for example, the IOException that's thrown if the application
 * cannot connect to the banking service at all.
 */
public class AccountOperationException extends Exception {

    public AccountOperationException() {
        super();
    }

    public AccountOperationException(String message) {
        super(message);
    }

    public AccountOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountOperationException(Throwable cause) {
        super(cause);
    }
}
