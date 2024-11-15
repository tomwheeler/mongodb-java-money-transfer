package org.mongodb;

public class TransactionDetails {

    private String sender;
    private String recipient;
    private String referenceId;
    private int amount;

    public TransactionDetails() {
    }

    public TransactionDetails(String sender, String recipient, String referenceId, int amount) {
        this.sender = sender;
        this.recipient = recipient;
        this.referenceId = referenceId;
        this.amount = amount;
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
