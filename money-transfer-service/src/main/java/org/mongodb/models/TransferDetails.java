package org.mongodb.models;

import java.util.Objects;

/**
 * This class holds input data for a money transfer. It identifies the sender,
 * recipient, and amount of the transfer, as well as a reference ID that uniquely
 * identifies this transfer.
 */
public class TransferDetails {

    private String sender;
    private String recipient;
    private String referenceId;
    private int amount;

    public TransferDetails() {
    }

    public TransferDetails(String sender, String recipient, int amount, String referenceId) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.referenceId = referenceId;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public int getAmount() {
        return amount;
    }

    public String getReferenceId() {
        return referenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferDetails that = (TransferDetails) o;
        return amount == that.amount && Objects.equals(sender, that.sender) && Objects.equals(recipient, that.recipient) && Objects.equals(referenceId, that.referenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, recipient, referenceId, amount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("TransferDetails {");
        sb.append(String.format("sender='%s', ", sender));
        sb.append(String.format("recipient='%s', ", recipient));
        sb.append(String.format("amount='%d', ", amount));
        sb.append(String.format("referenceId='%s'}", referenceId));

        return sb.toString();
    }
}
