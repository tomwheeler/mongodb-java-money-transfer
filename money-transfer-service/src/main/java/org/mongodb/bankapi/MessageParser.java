package org.mongodb.bankapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mongodb.exceptions.AccountOperationException;
import org.mongodb.exceptions.InsufficientFundsException;
import org.mongodb.exceptions.NoSuchAccountException;

import java.io.IOException;

public class MessageParser {

    private final String SUCCESS = "SUCCESS"; // service returns this to indicate success

    int parseBalanceResponse(String body) throws IOException, NoSuchAccountException, AccountOperationException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode bodyNode = mapper.readTree(body);
        JsonNode statusNode = bodyNode.get("status");

        if (! SUCCESS.equals(statusNode.asText())) {
            String message = bodyNode.get("message").asText();

            if (message.contains("IllegalArgumentException:")
                    && message.contains("does not exist.")) {
                String detail = message.substring(message.indexOf(":") + 1);
                throw new NoSuchAccountException(detail);
            }

            // Some business-level failure we don't yet recognize
            throw new AccountOperationException(message);
        }

        return bodyNode.get("balance").asInt();
    }

    String parseDepositResponse(String body) throws IOException, NoSuchAccountException, AccountOperationException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode bodyNode = mapper.readTree(body);
        JsonNode statusNode = bodyNode.get("status");

        if (! SUCCESS.equals(statusNode.asText())) {
            String message = bodyNode.get("message").asText();
            if (message.contains("IllegalArgumentException:")
                    && message.contains("does not exist.")) {
                String detail = message.substring(message.indexOf(":") + 1);
                throw new NoSuchAccountException(detail);
            }

            // Some business-level failure we don't yet recognize
            throw new AccountOperationException(message);
        }

        return bodyNode.get("transactionId").asText();
    }

    String parseWithdrawResponse(String body) throws IOException, NoSuchAccountException, InsufficientFundsException, AccountOperationException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode bodyNode = mapper.readTree(body);
        JsonNode statusNode = bodyNode.get("status");

        if (!SUCCESS.equals(statusNode.asText())) {
            String message = bodyNode.get("message").asText();
            if (message.contains("InsufficientFundsException:")) {
                String detail = message.substring(message.indexOf(":") + 1);
                throw new InsufficientFundsException(detail);
            } else if (message.contains("IllegalArgumentException:")
                    && message.contains("does not exist.")) {
                String detail = message.substring(message.indexOf(":") + 1);
                throw new NoSuchAccountException(detail);
            }

            // Some business-level failure we don't yet recognize
            throw new AccountOperationException(message);
        }

        return bodyNode.get("transactionId").asText();
    }
}
