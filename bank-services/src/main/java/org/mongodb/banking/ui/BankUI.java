package org.mongodb.banking.ui;


import org.mongodb.banking.BankService;
import org.mongodb.banking.config.MongodbConfig;
import org.mongodb.banking.repository.BankRepositoryImpl;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BankUI {

    private final BankService bankService;
    private final java.util.Map<String, JLabel> balanceLabels = new java.util.HashMap<>();
    private final JPanel mainPanel = new JPanel();
    private final JFrame frame = new JFrame("Banking App - GUI");

    public BankUI(BankService bankService, List<String> initialBankNames) {
        this.bankService = bankService;
        SwingUtilities.invokeLater(() -> createAndShowGUI(initialBankNames));
        startUpdatingBanksAndBalances();
    }

    private void createAndShowGUI(List<String> bankNames) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);

        mainPanel.setLayout(new GridLayout(0, 1));

        for (String bankName : bankNames) {
            addBankToUI(bankName);
        }

        frame.add(new JScrollPane(mainPanel));
        frame.setVisible(true);
    }

    private void addBankToUI(String bankName) {
        if (balanceLabels.containsKey(bankName)) return;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));

        JLabel balanceLabel = new JLabel("Balance: Loading...");
        balanceLabels.put(bankName, balanceLabel);

        JLabel statusLabel = new JLabel("Status: Online");
        JButton toggleButton = new JButton("Stop Bank");

        toggleButton.addActionListener(e -> {
            try {
                if ("Stop Bank".equals(toggleButton.getText())) {
                    System.out.println("Stopping bank: " + bankName);
                    bankService.stopBank(bankName);
                    toggleButton.setText("Start Bank");
                    statusLabel.setText("Status: Offline");
                } else {
                    System.out.println("Starting bank: " + bankName);
                    bankService.startBank(bankName);
                    toggleButton.setText("Stop Bank");
                    statusLabel.setText("Status: Online");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error toggling bank: " + ex.getMessage());
            }
        });

        panel.add(new JLabel("Bank: " + bankName));
        panel.add(balanceLabel);
        panel.add(statusLabel);
        panel.add(toggleButton);

        mainPanel.add(panel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void startUpdatingBanksAndBalances() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    updateBanks();
                    updateBalances();
                });
            }
        }, 0, 5000);
    }

    private void updateBanks() {
        // Fetch the latest bank names from the database
        List<String> currentBankNames = bankService.getAllBankNames();

        // Add new banks to the UI
        for (String bankName : currentBankNames) {
            addBankToUI(bankName);
        }

        // Remove banks that no longer exist in the database
        balanceLabels.keySet().removeIf(bankName -> {
            if (!currentBankNames.contains(bankName)) {
                removeBankFromUI(bankName);
                return true;
            }
            return false;
        });
    }

    private void removeBankFromUI(String bankName) {
        // Find the panel associated with the bank and remove it
        Component[] components = mainPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                Component[] labels = panel.getComponents();
                for (Component label : labels) {
                    if (label instanceof JLabel && ((JLabel) label).getText().contains("Bank: " + bankName)) {
                        mainPanel.remove(panel);
                        mainPanel.revalidate();
                        mainPanel.repaint();
                        break;
                    }
                }
            }
        }
        balanceLabels.remove(bankName); // Remove the bank's balance label from the map
    }


    private void updateBalances() {
        for (String bankName : balanceLabels.keySet()) {
            JLabel balanceLabel = balanceLabels.get(bankName);
            try {
                int balance = bankService.getBalance(bankName);
                balanceLabel.setText("Balance: $" + balance);
            } catch (Exception e) {
                balanceLabel.setText("Balance: N/A (Offline)");
            }
        }
    }

    public static void main(String[] args) {
        try {
            BankRepositoryImpl repository = new BankRepositoryImpl(MongodbConfig.getDatabase());
            BankService bankService = new BankService(repository);

            List<String> bankNames = bankService.getAllBankNames();

            if (bankNames.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No banks found in the database!");
            }

            new BankUI(bankService, bankNames);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error initializing banking app: " + e.getMessage());
        }
    }
}
