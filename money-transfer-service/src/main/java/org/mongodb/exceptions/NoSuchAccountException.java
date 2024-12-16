package org.mongodb.exceptions;

/**
 * This exception is thrown when the client attempts to perform an operation
 * on a non-existent account.
 */
public class NoSuchAccountException extends AccountOperationException {
    public NoSuchAccountException(String message) {
        super(message);
    }
}
