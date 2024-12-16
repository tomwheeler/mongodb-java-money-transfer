package org.mongodb.banking.ui.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class BankDetailModel {
    
    public static final String PROP_NAME_AVAILABLE = "bankName";
    public static final String PROP_NAME_BALANCE = "balance";

    // flag used to indicate that the actual current balance is unknown
    public static final int FLAG_BALANCE_UNKNOWN = Integer.MIN_VALUE;
    
    private final PropertyChangeSupport pcs;

    private final String bankName; // immutable property, used as identifier
    private int balance;
    private boolean available;

    public BankDetailModel(String bankName, int currentBalance, boolean available) {
        this.bankName = bankName;
        this.balance = currentBalance;
        this.available = available;
        
        this.pcs = new PropertyChangeSupport(this);
    }
    
    public String getBankName() {
        return bankName;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int currentBalance) {
        if (this.balance == currentBalance) {
            return;
        }

        int oldBalance = this.balance;
        this.balance = currentBalance;
        pcs.firePropertyChange(PROP_NAME_BALANCE, oldBalance, bankName);
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        if (this.available == available) {
            return;
        }
        this.available = available;
        pcs.firePropertyChange(PROP_NAME_AVAILABLE, ! available, available);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("BankDetailModel {");
        builder.append("bankName=").append(bankName);
        builder.append(", balance=");
        if (balance == FLAG_BALANCE_UNKNOWN) {
            builder.append("UNKNOWN");
        } else {
            builder.append(balance);
        }
        builder.append(", available=").append(available);
        builder.append(" }");

        return builder.toString();
    }
}
