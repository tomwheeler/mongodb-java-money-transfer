package org.mongodb.banking.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BankingApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(BankingApiClient.class);
    
    private static final String SUCCESS = "SUCCESS"; // service returns this to indicate success

    private final String hostname;
    private final int portNumber;
    
    /**
     * Creates a new instance that will access services on the specified 
     * host name and port number.
     * 
     * @param hostname host name on which the service is deployed
     * @param portNumber port number on which the service listed on that host
     */
    public BankingApiClient(String hostname, int portNumber) {
        this.hostname = hostname;
        this.portNumber = portNumber;
    }

    /**
     * Returns a list containing the name of every bank known by the service.
     */
    public List<String> getAllBankNames() {
        String url = String.format("http://%s:%d/api/listBanks", hostname, portNumber);

        List<String> banks = new ArrayList<>();
        try {
            String body = callService(url);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode bodyNode = mapper.readTree(body);
            JsonNode statusNode = bodyNode.get("status");
            if (! SUCCESS.equals(statusNode.asText())) {
                throw new IOException("Service returned status code: " + statusNode.asText());
            }
            
            JsonNode banksNode = bodyNode.get("banks");
            if (banksNode.isArray()) {
                for (JsonNode bankNode : banksNode) {
                    banks.add(bankNode.asText());
                }
            }
        } catch (IOException ex) {
            logger.error("Failed to retrieve list of bank names", ex);
        }
        
        return banks;
    }

    /**
     * Creates an account with the specified name and balance
     * @param bankName identifies the account to create, which must not already exist
     */
    public void createBank(String bankName, int initialBalance) {
        try {
            String name = URLEncoder.encode(bankName, "UTF-8");
            String baseUrl = "http://%s:%d/api/createBank?bankName=%s&initialBalance=%d";
            String url = String.format(baseUrl, hostname, portNumber, name, initialBalance);
            
            String body = callService(url);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode bodyNode = mapper.readTree(body);
            JsonNode statusNode = bodyNode.get("status");
            
            if (! SUCCESS.equals(statusNode.asText())) {
                String message = bodyNode.get("message").asText();
                throw new IOException("Service call unsuccessful, message is: " + message);
            }
        } catch (IOException ex) {
            logger.error("Failed to retrieve balance for {}", bankName, ex);
        }
    }
    
    /**
     * Return the current balance for the specified bank account.
     * @param bankName identifies the account for which the balance is being requested
     * @return the current balance of the specified account.
     */
    public int getBalance(String bankName) {
        int balance = -1;
        try {
            String name = URLEncoder.encode(bankName, "UTF-8");
            String url = String.format("http://%s:%d/api/balance?bankName=%s", hostname, portNumber, name);
            
            String body = callService(url);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode bodyNode = mapper.readTree(body);
            JsonNode statusNode = bodyNode.get("status");
            if (! SUCCESS.equals(statusNode.asText())) {
                throw new IOException("Service returned status code: " + statusNode.asText());
            }
            
            JsonNode balanceNode = bodyNode.get("balance");
            balance = balanceNode.asInt();
        } catch (IOException ex) {
            logger.error("Failed to retrieve balance for {}", bankName, ex);
        }
        
        return balance;
    }

    /**
     * Returns true if the service provided by the specified bank is currently
     * accepting transactions, or false otherwise.
     * @param bankName identifies the account for which the balance is being requested
     * @return a boolean indicating whether this account is available for new transactions
     */
    public boolean isAvailable(String bankName) {
        boolean available = false;
        try {
            String name = URLEncoder.encode(bankName, "UTF-8");
            String url = String.format("http://%s:%d/api/isAvailable?bankName=%s", hostname, portNumber, name);
            
            String body = callService(url);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode bodyNode = mapper.readTree(body);
            JsonNode statusNode = bodyNode.get("status");
            if (! SUCCESS.equals(statusNode.asText())) {
                throw new IOException("Service returned status code: " + statusNode.asText());
            }
            
            JsonNode availableNode = bodyNode.get("available");
            available = availableNode.asBoolean();
        } catch (IOException ex) {
            logger.error("Failed to retrieve availability status for {}", bankName, ex);
        }
        
        return available;
    }
    
    /**
     * Triggers the specified bank account to begin accepting transactions.
     * @param bankName identifies which account should begin accepting transactions
     */
    public void startBank(String bankName) {
        changeAvailability(bankName, true); 
    }

    /**
     * Triggers the specified bank account to stop accepting transactions.
     * @param bankName identifies which account should stop accepting transactions
     */
    public void stopBank(String bankName) {
        changeAvailability(bankName, false); 
    }
    
    private void changeAvailability(String bankName, boolean available) {
        try {
            String name = URLEncoder.encode(bankName, "UTF-8");
            String baseUrl = "http://%s:%d/api/setAvailable?bankName=%s&value=%s";
            String url = String.format(baseUrl, hostname, portNumber, name, available);
            
            String body = callService(url);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode bodyNode = mapper.readTree(body);
            JsonNode statusNode = bodyNode.get("status");
            if (! SUCCESS.equals(statusNode.asText())) {
                throw new IOException("Service returned status code: " + statusNode.asText());
            }
        } catch (IOException ex) {
            logger.error("Failed to change availability status for {}", bankName, ex);
        }
    }
    
    private String callService(String serviceUrl) throws IOException {
        String body = null;

        URI uri = URI.create(serviceUrl);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            body = response.body();
        } catch (InterruptedException ie) {
            throw new IOException("HTTP Client operation interrupted", ie);
        }

        return body;
    }
}
