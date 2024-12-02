package org.mongodb.models;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionDetails that = (TransactionDetails) o;
        return amount == that.amount && Objects.equals(sender, that.sender) && Objects.equals(recipient, that.recipient) && Objects.equals(referenceId, that.referenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, recipient, referenceId, amount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("TransactionDetails {");
        sb.append(String.format("sender='%s', ", sender));
        sb.append(String.format("recipient='%s', ", recipient));
        sb.append(String.format("referenceId='%s', ", referenceId));
        sb.append(String.format("amount='%d'}", amount));

        return sb.toString();
    }
}
