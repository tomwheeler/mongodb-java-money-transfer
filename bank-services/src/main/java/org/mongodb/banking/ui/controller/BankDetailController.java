package org.mongodb.banking.ui.controller;

import java.beans.PropertyChangeEvent;
import org.mongodb.banking.client.BankingApiClient;
import org.mongodb.banking.ui.model.BankDetailModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankDetailController {

    private static final Logger logger = LoggerFactory.getLogger(BankDetailController.class);
    
    private final String name;
    private final BankDetailModel model;
    private final BankingApiClient client;
    
    public BankDetailController(String bankName, BankingApiClient bankClient) {
        name = bankName;

        logger.debug("Creating BankDetailController for {}", bankName);
        int balance = bankClient.getBalance(name);
        boolean available = bankClient.isAvailable(name);
        
        model = new BankDetailModel(name, balance, available);
        client = bankClient;

        model.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (BankDetailModel.PROP_NAME_AVAILABLE.equals(evt.getPropertyName())) {
                if (model.isAvailable()) {
                    bankClient.startBank(name);
                } else {
                    bankClient.stopBank(name);
                }
            }
        });
    }

    void refresh() {
        logger.trace("BankDetailController is refreshing model for {}", name);

        int newBalance = client.getBalance(name);
        if (model.getBalance() != newBalance) {
            model.setBalance(newBalance);
        }

        boolean newAvailable = client.isAvailable(name);
        if (model.isAvailable() != newAvailable) {
            model.setAvailable(newAvailable);
        }
    }

    public BankDetailModel getModel() {
        return model;
    }
}