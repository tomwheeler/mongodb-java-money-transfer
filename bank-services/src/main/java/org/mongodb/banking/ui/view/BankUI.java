package org.mongodb.banking.ui.view;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import org.mongodb.banking.client.BankingApiClient;
import org.mongodb.banking.ui.model.BankDetailModel;
import org.mongodb.banking.ui.controller.BankListController;
import org.mongodb.banking.ui.model.BankListModel;

public class BankUI {
    
    private final BankListController controller;
    private final JPanel[] mainPanelHolder;
    
    // shown when there are no accounts available
    private JPanel placeholderPanel;

    public BankUI(String host, int port) {
        BankingApiClient client = new BankingApiClient(host, port);
        this.controller = new BankListController(client);
        mainPanelHolder = new JPanel[1];
    }
    
    public void display() {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }
    
    private void createAndShowGUI() {
        FlatLightLaf.setup();

        final JFrame frame = new JFrame("Bank Balance and Status GUI");
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 400);
        frame.setLocationRelativeTo(null);
        
        Image temporalLogo = Icons.getTemporalLogo();
        frame.setIconImage(temporalLogo);   // for title bar (in other desktop environments)
        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            taskbar.setIconImage(temporalLogo); // for Dock on macOS
        }

        final JPanel mainPanel = new JPanel();
        mainPanelHolder[0] = mainPanel;
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        JPanel addPanel = new JPanel();
        addPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 35, 20));
        addPanel.setLayout(new BorderLayout(10, 10));

        JButton addButton = new JButton("Add account");
        addButton.setFont(new Font("Arial", Font.BOLD, 18));
        addButton.setIcon(Icons.createBankIcon());
        addButton.addActionListener((ActionEvent e) -> {
            handleAddAccountButtonClick(frame);
        });
        addPanel.add(addButton, BorderLayout.WEST);
        
        JButton approveButton = new JButton("Approve a Transaction");
        approveButton.setFont(new Font("Arial", Font.BOLD, 18));
        approveButton.setIcon(Icons.createApproveIcon());
        approveButton.addActionListener((ActionEvent e) -> {
            handleApprovalButtonClick(frame);
        });

        addPanel.add(approveButton, BorderLayout.EAST);
        mainPanel.add(addPanel);

        for (String bankName : controller.getModel().getBankNames()) {
            addDetailPanel(bankName);
        }
        
        showOrHidePlaceholderPanel();
        
        BankListModel model = controller.getModel();
        model.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (evt.getNewValue() != null) {
                String bankName = (String)evt.getNewValue();
                SwingUtilities.invokeLater(() -> {
                    addDetailPanel(bankName);
                    showOrHidePlaceholderPanel();
                });
            } else {
                // remove panel
                String bankName = (String)evt.getOldValue();
                SwingUtilities.invokeLater(() -> {
                    removeDetailPanel(bankName);
                    showOrHidePlaceholderPanel();
                });
            }
        });
        
        JScrollPane scroller = new JScrollPane(mainPanel);
        frame.add(scroller);
        frame.pack();
        frame.setVisible(true);
    }

    private void addDetailPanel(String bankName) {
        BankDetailModel model = controller.getBankDetailModel(bankName);
        if (model == null) {
            return;
        }
        BankDetailView panel = new BankDetailView(model);
        mainPanelHolder[0].add(panel);
    }

    private void removeDetailPanel(String bankName) {
        if (bankName != null) {
            BankDetailView panel = findPanelByName(bankName);
            if (panel != null) {
                mainPanelHolder[0].remove(panel);
            }
        }
    }

    private void handleApprovalButtonClick(JFrame parent) {
        JTextField workflowIdField = new JTextField();
        JTextField managerNameField = new JTextField();
        Object[] message = {
            "Workflow ID:  ", workflowIdField,
            "Manager Name: ", managerNameField
        };

        int option = JOptionPane.showConfirmDialog(parent, message, "Approve", 
            JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) {
            return; // cancelled
        }

        final String workflowId = workflowIdField.getText();
        final String managerName = managerNameField.getText();
        if (workflowId != null && managerName != null) {
            Runnable r = () -> {
                controller.approvePendingTransfer(workflowId, managerName);
            };
            new Thread(r).start();
        }
    }
    
    void handleAddAccountButtonClick(JFrame frame) {
        ImageIcon icon = Icons.createBankIcon();
        String title = "Specify Account Name";
        String prompt = "Account Name";

        String accountName = (String)JOptionPane.showInputDialog(frame, 
            title, prompt, JOptionPane.OK_CANCEL_OPTION, icon, null, null);

        if (accountName == null || accountName.length() == 0) {
            return;
        }
        Runnable r = () -> {
            controller.getModel().addBankName(accountName);
        };
        new Thread(r).start();
    }
    
    private BankDetailView findPanelByName(String bankName){
        JPanel mainPanel = mainPanelHolder[0];
        if (mainPanel == null) {
            return null;
        }
            
        Component[] components = mainPanel.getComponents();
        for (Component component : components) {
            if (component instanceof BankDetailView) {
                BankDetailView detailPanel = (BankDetailView) component;
                if (detailPanel.getName().equals(bankName)) {
                    return detailPanel;
                }
            }
        }
        return null; 
    }

    private void showOrHidePlaceholderPanel() {
        if (controller.getModel().getBankNames().isEmpty()) {
            placeholderPanel = new JPanel();
            JLabel warningLabel = new JLabel();
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body><center>");
            sb.append("There are currently no accounts available.");
            sb.append("<br>Click the button above (on the left) to add them.");
            sb.append("</center><br></body></html>");
            warningLabel.setText(sb.toString());
            warningLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            placeholderPanel.add(warningLabel);
            
            mainPanelHolder[0].add(placeholderPanel);
        } else {
            if (placeholderPanel != null) {
                mainPanelHolder[0].remove(placeholderPanel);
                placeholderPanel = null;
            }
        }
        
        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(mainPanelHolder[0]);
        if (frame != null) {
            frame.pack();
        }
    }
}
