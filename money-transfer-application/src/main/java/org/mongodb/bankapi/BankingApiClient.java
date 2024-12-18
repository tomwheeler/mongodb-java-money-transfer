package org.mongodb.bankapi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides Java methods for interacting with a bank through an
 * HTTP API.
 */
public class BankingApiClient {

    private static final Logger logger = LoggerFactory.getLogger(BankingApiClient.class);

    private final String hostname;
    private final int portNumber;
    private final MessageParser parser;

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
        parser = new MessageParser();
    }

    /**
     * Return the current balance for the specified bank account. In addition to the IOException,
     * it may throw NoSuchAccountException (if the account is unknown) or AccountOperationException,
     * for other types of application-level failures.
     *
     * @param bankName identifies the account for which the balance is being requested
     * @throws IOException if it encounters failure while making the call or parsing the response
     * @return the current balance of the specified account.
     */
    public int getBalance(String bankName) throws IOException {
        String name = URLEncoder.encode(bankName, StandardCharsets.UTF_8);
        String url = String.format("http://%s:%d/api/balance?bankName=%s", hostname, portNumber, name);

        String body = callService(url);
        int balance = parser.parseBalanceResponse(body);

        return balance;
    }

    /**
     * Requests a deposit of the specified amount to the named account. The idempotency key is
     * used to identify duplicate requests. If this key matches one previously seen in this
     * session, this method will not perform the deposit. Instead, it will return the transaction
     * ID for that earlier transaction. In addition to the IOException, it may throw
     * NoSuchAccountException (if the account is unknown) or AccountOperationException (for
     * other types of application-level failures).
     *
     * @param bankName the account to which the money should be credited
     * @param amount the quantity by which the account balance should be increased
     * @param idempotencyKey a user-specified unique key for this request
     * @return the transaction ID for the deposit
     * @throws IOException if it encounters failure while making the call or parsing the response
     */
    public String deposit(String bankName, int amount, String idempotencyKey) throws IOException {
        String name = URLEncoder.encode(bankName, StandardCharsets.UTF_8);
        String key = URLEncoder.encode(idempotencyKey, StandardCharsets.UTF_8);
        String baseUrl = "http://%s:%d/api/deposit?bankName=%s&amount=%d&idempotencyKey=%s";
        String url = String.format(baseUrl, hostname, portNumber, name, amount, key);

        String body = callService(url);
        String transactionId = parser.parseDepositResponse(body);

        return transactionId;
    }

    /**
     * Requests a withdrawal of the specified amount from the named account. The idempotency key is
     * used to identify duplicate requests. If this key matches one previously seen in this session,
     * this method will not perform the withdrawal. Instead, it will return the transaction ID for
     * that earlier transaction. In addition to IOException, it may throw InsufficientFundsException
     * (if the amount exceeds the account balance), NoSuchAccountException (if the account is unknown)
     * or AccountOperationException (for other types of application-level failures)
     *
     * @param bankName the account from which the money should be debited
     * @param amount the quantity by which the account balance should be decreased
     * @param idempotencyKey a user-specified unique key for this request
     * @return the transaction ID for the withdrawal
     * @throws Exception if a problem is encountered during the request
     */
    public String withdraw(String bankName, int amount, String idempotencyKey) throws Exception {
        String name = URLEncoder.encode(bankName, StandardCharsets.UTF_8);
        String key = URLEncoder.encode(idempotencyKey, StandardCharsets.UTF_8);
        String baseUrl = "http://%s:%d/api/withdraw?bankName=%s&amount=%d&idempotencyKey=%s";
        String url = String.format(baseUrl, hostname, portNumber, name, amount, key);

        String body = callService(url);
        String transactionId = parser.parseWithdrawResponse(body);

        return transactionId;
    }

    private String callService(String serviceUrl) throws IOException {
        logger.debug("Making call to URL {}", serviceUrl);

        URI uri = URI.create(serviceUrl);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

        String body;
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            body = response.body();
        } catch (InterruptedException ie) {
            throw new IOException("HTTP Client operation interrupted", ie);
        }

        return body;
    }
}