package org.mongodb.bankapi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides Java methods for interacting with a bank through an
 * HTTP API.
 */
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
     * Return the current balance for the specified bank account.
     * @param bankName identifies the account for which the balance is being requested
     * @return the current balance of the specified account.
     */
    public int getBalance(String bankName) throws Exception {
        String name = URLEncoder.encode(bankName, "UTF-8");
        String url = String.format("http://%s:%d/api/balance?bankName=%s", hostname, portNumber, name);

        String body = callService(url);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode bodyNode = mapper.readTree(body);
        JsonNode statusNode = bodyNode.get("status");
        if (! SUCCESS.equals(statusNode.asText())) {
            String message = bodyNode.get("message").asText();
            throw new IOException("balance retrieval call unsuccessful, message is: " + message);
        }

        JsonNode balanceNode = bodyNode.get("balance");
        int balance = balanceNode.asInt();

        return balance;
    }

    /**
     * Requests a deposit of the specified amount to the named account. The idempotency key is
     * used to identify duplicate requests. If this key matches one previously seen in this
     * session, this method will not perform the deposit. Instead, it will return the transaction
     * ID for that earlier transaction.
     *
     * @param bankName the account to which the money should be credited
     * @param amount the quantity by which the account balance should be increased
     * @param idempotencyKey a user-specified unique key for this request
     * @return the transaction ID for the deposit
     * @throws Exception if a problem is encountered during the request
     */
    public String deposit(String bankName, int amount, String idempotencyKey) throws Exception {
        String name = URLEncoder.encode(bankName, "UTF-8");
        String key = URLEncoder.encode(idempotencyKey, "UTF-8");
        String baseUrl = "http://%s:%d/api/deposit?bankName=%s&amount=%d&idempotencyKey=%s";
        String url = String.format(baseUrl, hostname, portNumber, name, amount, key);

        String body = callService(url);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode bodyNode = mapper.readTree(body);
        JsonNode statusNode = bodyNode.get("status");
        if (! SUCCESS.equals(statusNode.asText())) {
            String message = bodyNode.get("message").asText();
            throw new IOException("balance retrieval call unsuccessful, message is: " + message);
        }

        return bodyNode.get("transactionId").asText();
    }

    /**
     * Requests a withdrawal of the specified amount from the named account. The idempotency key is
     * used to identify duplicate requests. If this key matches one previously seen in this
     * session, this method will not perform the withdrawal. Instead, it will return the transaction
     * ID for that earlier transaction.
     *
     * @param bankName the account from which the money should be debited
     * @param amount the quantity by which the account balance should be decreased
     * @param idempotencyKey a user-specified unique key for this request
     * @return the transaction ID for the withdrawal
     * @throws Exception if a problem is encountered during the request
     */
    public String withdraw(String bankName,  int amount, String idempotencyKey) throws Exception {
        String name = URLEncoder.encode(bankName, "UTF-8");
        String key = URLEncoder.encode(idempotencyKey, "UTF-8");
        String baseUrl = "http://%s:%d/api/withdraw?bankName=%s&amount=%d&idempotencyKey=%s";
        String url = String.format(baseUrl, hostname, portNumber, name, amount, key);

        String body = callService(url);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode bodyNode = mapper.readTree(body);
        JsonNode statusNode = bodyNode.get("status");
        if (! SUCCESS.equals(statusNode.asText())) {
            String message = bodyNode.get("message").asText();
            throw new IOException("balance retrieval call unsuccessful, message is: " + message);
        }

        return bodyNode.get("transactionId").asText();
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