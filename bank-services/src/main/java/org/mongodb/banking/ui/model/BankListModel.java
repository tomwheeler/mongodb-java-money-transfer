package org.mongodb.banking.ui.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BankListModel {
    
    public static final String PROP_NAME_BANK_NAMES = "bankNames";
    
    private final PropertyChangeSupport pcs;
    private final List<String> bankNames;

    public BankListModel(String... names) {
        bankNames = new ArrayList<>();
        bankNames.addAll(Arrays.asList(names));

        pcs = new PropertyChangeSupport(this);
    }
    
    public List<String> getBankNames() {
        return bankNames;
    }
    
    public void addBankName(String bankName) {
        if (bankName == null || bankNames.contains(bankName)) {
            return;
        }

        bankNames.add(bankName);
        pcs.firePropertyChange(PROP_NAME_BANK_NAMES, null, bankName);
    }

    public void removeBankName(String bankName) {
        if (bankName == null || ! bankNames.contains(bankName)) {
            return;
        }
        
        bankNames.remove(bankName);
        pcs.firePropertyChange(PROP_NAME_BANK_NAMES, bankName, null);
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

        builder.append("BankListModel {");
        builder.append("bankNames=[");
        
        for (Iterator<String> i = bankNames.iterator(); i.hasNext();) {
            builder.append(i.next());
            if (i.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append("]");

        return builder.toString();
    }
}
