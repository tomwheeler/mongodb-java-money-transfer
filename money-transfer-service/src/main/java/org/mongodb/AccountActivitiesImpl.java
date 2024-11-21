package org.mongodb;

import io.temporal.activity.Activity;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the operations that are used to carry out the money transfer.
 * This class includes two Activities, each of which make a request to a
 * banking service. One does this for the withdrawal, while the other does
 * it for the deposit.
 */
public class AccountActivitiesImpl implements AccountActivities {

    private static final Logger logger = LoggerFactory.getLogger(AccountActivitiesImpl.class);

    @Override
    public String withdraw(TransactionDetails input) {
        logger.info("Starting withdraw operation");

        // TODO: should probably include reference ID as idempotency key, but that makes
        // it harder to observe hat Temporal doesn't repeat the withdrawal operation if
        // the Workflow is terminated and restarted. I need to think about this some more.
        String serviceUrl = String.format("http://localhost:8080/withdraw?amount=%d&idempotency-key=%s",
                input.getAmount(), input.getReferenceId());
        String transactionId = null;
        try {
            transactionId = callService(serviceUrl);
        } catch (IOException| AccountOperationException e) {
            logger.error("Withdraw operation failed", e);
            throw Activity.wrap(e);
        }

        logger.info("Withdraw operation complete.");
        return transactionId;
    }

    @Override
    public String deposit(TransactionDetails input) {
        logger.info("Starting deposit operation");

        String serviceUrl = String.format("http://localhost:8080/deposit?amount=%d&idempotency-key=%s",
                input.getAmount(), input.getReferenceId());
        String transactionId = null;
        try {
            transactionId = callService(serviceUrl);
        } catch (IOException| AccountOperationException e) {
            logger.error("Withdraw operation failed", e);
            throw Activity.wrap(e);
        }

        logger.info("Deposit operation complete.");
        return transactionId;
    }

    /**
     * Calls the specified URL and parses the response, either returning a transaction ID if the
     * call was successful or throwing an exception otherwise.
     *
     * @param serviceUrl the URL of the microservice to call
     * @return a transaction ID for the operation, if successful
     * @throws IOException if there was a failure making the request or retrieving the response
     * @throws AccountOperationException if there was a business-level failure fulfilling the request
     */
    private String callService(String serviceUrl) throws IOException, AccountOperationException {
        URI uri = URI.create(serviceUrl);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new AccountOperationException("Error from banking service: " + response.body());
            }

            // Parse JSON response
            Map<String, Object> result = JsonUtil.toMap(response.body());
            if (!"SUCCESS".equals(result.get("status"))) {
                throw new AccountOperationException((String) result.get("message"));
            }

            return (String) result.get("transaction-id");
        } catch (InterruptedException e) {
            throw new IOException("HTTP Client operation interrupted", e);
        }
    }

}
