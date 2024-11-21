package org.mongodb;

public class TransactionDetails {

    private String sender;
    private String recipient;
    private String referenceId;
    private int amount;
    private String idempotencyKey;

    public TransactionDetails() {
    }

    public TransactionDetails(String sender, String recipient, String referenceId, int amount, String idempotencyKey) {
        this.sender = sender;
        this.recipient = recipient;
        this.referenceId = referenceId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public int getAmount() {
        return amount;
    }
}
