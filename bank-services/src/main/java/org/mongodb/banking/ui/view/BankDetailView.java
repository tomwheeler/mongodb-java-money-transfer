package org.mongodb.banking.ui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Currency;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.mongodb.banking.ui.model.BankDetailModel;

/**
 * This is a GUI component that displays details for a given bank account. It 
 * also allows one to change the online/offline status of that account, enabling
 * one to simulate an outage on a per-account basis.
 */
public class BankDetailView extends JPanel {
    
    private static final Font TITLE_PANEL_FONT = new Font("Arial", Font.PLAIN, 20);
    private static final Font UI_ELEMENT_FONT = new Font("Arial", Font.PLAIN, 18);

    // shown for the current balance when the account is unavailable
    private static final String BALANCE_UNKNOWN_TEXT = "UNKNOWN";
    
    private final BankDetailModel model;
    
    private JPanel titledPanel;              // outer panel, titled with the account name
    private TitledBorder titledBorder;       // outer panel, titled with the account name
    private JPanel balancePanel;             // inner panel on the left
    private JPanel statusPanel;              // inner panel on the right
    private JLabel balanceFieldLabel;        // label that identifies the balance value
    private JLabel balanceValueLabel;        // label that contains the balance value
    private JLabel statusValueLabel;         // Label that shows the current online/offline status
    private JButton bankStatusButton;        // Button used to toggle that status

    public BankDetailView() {
        this(new BankDetailModel("Unknown", -1, false));
    }
    
    public BankDetailView(BankDetailModel model) {
        this.model = model;
        setName(model.getBankName());

        model.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            refresh();
        });
        
        layoutComponents();
        refresh();
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        
        // set up outer container
        titledPanel = new JPanel();
        titledBorder = BorderFactory.createTitledBorder(null, model.getBankName(), 0, 0, TITLE_PANEL_FONT);
        titledPanel.setBorder(titledBorder);
        titledPanel.setLayout(new BorderLayout(3, 3));
        add(titledPanel, BorderLayout.NORTH);
 
        // set up inner container on left
        balancePanel = new JPanel();
        titledPanel.add(balancePanel, BorderLayout.WEST);
        
        balanceFieldLabel = new JLabel("Balance: ");
        balanceFieldLabel.setFont(UI_ELEMENT_FONT);
        // indent the balance field a little so the title above it stands out
        balancePanel.setBorder(BorderFactory.createEmptyBorder(3, 50, 0, 0)); 
        balancePanel.add(balanceFieldLabel, BorderLayout.EAST);
        
        balanceValueLabel = new JLabel("{BALANCE}");
        balanceValueLabel.setFont(UI_ELEMENT_FONT);
        balancePanel.add(balanceValueLabel, BorderLayout.EAST);

        // set up inner container on right
        statusPanel = new JPanel();
        titledPanel.add(statusPanel, BorderLayout.EAST);
        
        statusValueLabel = new JLabel("{STATUS}");
        statusValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 10)); 
        statusValueLabel.setFont(UI_ELEMENT_FONT);
        statusPanel.add(statusValueLabel, BorderLayout.WEST);

        bankStatusButton = new JButton("{CONTROL}");
        statusValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 50)); 
        bankStatusButton.setFont(UI_ELEMENT_FONT);
        statusPanel.add(bankStatusButton, BorderLayout.EAST);
        
        bankStatusButton.addActionListener((ActionEvent evt) -> {
            new Thread(() -> {
                if (model.isAvailable()) {
                    model.setAvailable(false);
                } else {
                    model.setAvailable(true);
                }
                SwingUtilities.invokeLater(() -> {
                    refresh();
                });
            }).start();
        });
        
        refresh();
    }

    /**
     * Updates the UI to reflect the current status.
     */
    private void refresh() {
        SwingUtilities.invokeLater(() -> {
            if (model.getBalance() == BankDetailModel.FLAG_BALANCE_UNKNOWN) {
                balanceValueLabel.setText(BALANCE_UNKNOWN_TEXT);
            } else {
                // show the amount using the user's currency symbol
                Locale locale = Locale.getDefault();
                Currency curr = Currency.getInstance(locale);
                String symbol = curr.getSymbol();
                if (symbol == null) {
                    symbol = "$";
                }
                String labelText = String.format("%s%d", symbol, model.getBalance());
                balanceValueLabel.setText(labelText);
            }
            
            if (model.isAvailable()) {
                statusValueLabel.setText("Available");
                bankStatusButton.setText("Stop");
                statusValueLabel.setIcon(Icons.createAvailableIcon());
            } else {
                statusValueLabel.setText("Unavailable");
                statusValueLabel.setIcon(Icons.createUnavailableIcon());
                bankStatusButton.setText("Start");
            }
            
            titledBorder.setTitle(model.getBankName());

            statusPanel.getParent().revalidate();
            statusPanel.getParent().repaint();
        });
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(575, 80);
    }
}