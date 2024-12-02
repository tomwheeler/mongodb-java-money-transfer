package org.mongodb.banking;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

public class BankController {
    private final BankManager manager;
    private final HttpServer server;

    public BankController(BankManager manager, int port) throws IOException {
        this.manager = manager;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        initializeHandlers();
    }

    private void initializeHandlers() {
        // Withdraw Endpoint
        server.createContext("/api/withdraw", exchange -> {
            try {
                RequestDetails request = parseRequest(exchange);
                if (request == null) return;

                String transactionId = request.service.withdraw(request.bankName, request.amount, request.idempotencyKey);
                sendResponse(exchange, Map.of("status", "SUCCESS", "transactionId", transactionId));
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        });

        // Deposit Endpoint
        server.createContext("/api/deposit", exchange -> {
            try {
                RequestDetails request = parseRequest(exchange);
                if (request == null) return;

                String transactionId = request.service.deposit(request.bankName, request.amount, request.idempotencyKey);
                sendResponse(exchange, Map.of("status", "SUCCESS", "transactionId", transactionId));
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        });


        // Balance Check Endpoint (Optional)
        server.createContext("/api/balance", exchange -> {
            try {
                if (!"GET".equals(exchange.getRequestMethod())) {
                    sendError(exchange, "Only GET requests are allowed");
                    return;
                }

                String query = exchange.getRequestURI().getQuery();
                String bankName = getQueryParam(query, "bankName");

                BankService service = manager.getOrCreateBank(bankName);
                int balance = service.getBalance(bankName);

                sendResponse(exchange, Map.of("status", "SUCCESS", "balance", balance));
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        });

        server.createContext("/api/createBank", exchange -> {
            try {
                String query = exchange.getRequestURI().getQuery();
                String bankName = getQueryParam(query, "bankName");
                int initialBalance = Integer.parseInt(getQueryParam(query, "initialBalance"));

                manager.getOrCreateBank(bankName).createBank(bankName, initialBalance);
                sendResponse(exchange, Map.of("status", "SUCCESS", "message", "Bank created successfully"));
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        });

    }

    public void start() {
        server.start();
        System.out.println("Banking service is running on http://localhost:" + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
    }

    private void sendResponse(com.sun.net.httpserver.HttpExchange exchange, Map<String, Object> response) throws IOException {
        String jsonResponse = JsonUtil.toJson(response);
        exchange.sendResponseHeaders(200, jsonResponse.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }

    private void sendError(com.sun.net.httpserver.HttpExchange exchange, String errorMessage) throws IOException {
        String jsonResponse = JsonUtil.toJson(Map.of("status", "ERROR", "message", errorMessage));
        exchange.sendResponseHeaders(400, jsonResponse.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }

    private String getQueryParam(String query, String paramName) {
        for (String param : query.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        throw new IllegalArgumentException("Missing query parameter: " + paramName);
    }

    private RequestDetails parseRequest(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendError(exchange, "Only GET requests are allowed");
            return null;
        }

        String query = exchange.getRequestURI().getQuery();
        String bankName = getQueryParam(query, "bankName");
        int amount = Integer.parseInt(getQueryParam(query, "amount"));
        String idempotencyKey = getQueryParam(query, "idempotencyKey");

        BankService service = manager.getOrCreateBank(bankName);

        return new RequestDetails(bankName, amount, idempotencyKey, service);
    }

    private static class RequestDetails {
        String bankName;
        int amount;
        String idempotencyKey;
        BankService service;

        public RequestDetails(String bankName, int amount, String idempotencyKey, BankService service) {
            this.bankName = bankName;
            this.amount = amount;
            this.idempotencyKey = idempotencyKey;
            this.service = service;
        }
    }


}
