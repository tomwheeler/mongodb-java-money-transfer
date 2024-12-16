package org.mongodb.banking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankController {
    
    private static final Logger logger = LoggerFactory.getLogger(BankController.class);
    
    private static final String SUCCESS = "SUCCESS"; // service returns this to indicate success
    private static final String ERROR = "ERROR";     // service returns this to indicate an error

    private final BankManager manager;
    private final int port;
    private final Javalin server;

    public BankController(BankManager manager, int port) {
        logger.info("Creating new BankController instance, with port={}", port);

        this.manager = manager;
        this.port = port;

        server = Javalin.create(config -> config.showJavalinBanner = false);
        server.get("/api/balance", new GetBalanceHandler());
        server.get("/api/createBank", new CreateBankHandler());
        server.get("/api/deleteBank", new DeleteBankHandler());
        server.get("/api/deposit", new DepositHandler());
        server.get("/api/withdraw", new WithdrawHandler());
        server.get("/api/isAvailable", new GetAvailabilityHandler());
        server.get("/api/setAvailable", new SetAvailabilityHandler());
        server.get("/api/listBanks", new ListBanksHandler());
    }
    
    public void start() {
        logger.info("Starting BankController on port {}", port);
        server.start("localhost", port);
    }

    private class GetBalanceHandler implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            logger.debug("BankController handling 'balance' request");

            String bankName = ctx.req().getParameter("bankName");

            try {
                BankService service = manager.getOrCreateBank(bankName);
                int balance = service.getBalance(bankName);

                sendResult(ctx, Map.of("status", SUCCESS, "balance", balance));
            } catch (Exception e) {
                sendError(ctx, Map.of("status", ERROR, "message", e.toString()));
            }
        }
    }
    
    private class CreateBankHandler implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            logger.debug("BankController handling 'createBank' request");

            try {
                String bankName = ctx.req().getParameter("bankName");
                int initialBalance = Integer.parseInt(ctx.req().getParameter("initialBalance"));

                manager.getOrCreateBank(bankName).createBank(bankName, initialBalance);
                
                sendResult(ctx, Map.of("status", SUCCESS, "message", "Created '" + bankName + "' account"));
            } catch (Exception e) {
                sendError(ctx, Map.of("status", ERROR, "message", e.toString()));
            }
        }
    }
    
    private class DeleteBankHandler implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            logger.debug("BankController handling 'deleteBank' request");

            try {
                String bankName = ctx.req().getParameter("bankName");

                BankService service = manager.getOrCreateBank(bankName);
                boolean wasDeleted = service.deleteBank(bankName);
                if (! wasDeleted) {
                    throw new RuntimeException("Unable to delete account for '" + bankName + "'");
                }

                sendResult(ctx, Map.of("status", SUCCESS, "message", "Created '" + bankName + "' account"));
            } catch (Exception e) {
                sendError(ctx, Map.of("status", ERROR, "message", e.toString()));
            }
        }
    }
    
    private class DepositHandler implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            logger.debug("BankController handling 'deposit' request");

            try {
                String bankName = ctx.req().getParameter("bankName");
                int amount = Integer.parseInt(ctx.req().getParameter("amount"));
                String idempotencyKey = ctx.req().getParameter("idempotencyKey");

                BankService service = manager.getOrCreateBank(bankName);
                String transactionId = service.deposit(bankName, amount, idempotencyKey);
                
                sendResult(ctx, Map.of("status", SUCCESS, "transactionId", transactionId));
            } catch (Exception e) {
                sendError(ctx, Map.of("status", ERROR, "message", e.toString()));
            }
        }
    }
    
    private class WithdrawHandler implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            logger.debug("BankController handling 'widthdraw' request");

            try {
                String bankName = ctx.req().getParameter("bankName");
                int amount = Integer.parseInt(ctx.req().getParameter("amount"));
                String idempotencyKey = ctx.req().getParameter("idempotencyKey");

                BankService service = manager.getOrCreateBank(bankName);
                String transactionId = service.withdraw(bankName, amount, idempotencyKey);
                
                sendResult(ctx, Map.of("status", SUCCESS, "transactionId", transactionId));
            } catch (Exception e) {
                sendError(ctx, Map.of("status", ERROR, "message", e.toString()));
            }
        }
    }
    
    private class GetAvailabilityHandler implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            logger.debug("BankController handling 'isAvailable' request");
            try {
                String bankName = ctx.req().getParameter("bankName");

                BankService service = manager.getOrCreateBank(bankName);
                boolean isAvailable = service.isAvailable(bankName);
                sendResult(ctx, Map.of("status", SUCCESS, "available", isAvailable));
            } catch (Exception e) {
                sendError(ctx, Map.of("status", ERROR, "message", e.toString()));
            }
        }
    }
    
    private class SetAvailabilityHandler implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            logger.debug("BankController handling 'setAvailable' request");

            try {
                String bankName = ctx.req().getParameter("bankName");
                boolean wantsAvailable = Boolean.parseBoolean(ctx.req().getParameter("value"));

                BankService service = manager.getOrCreateBank(bankName);
                if (wantsAvailable) {
                    service.startBank(bankName);
                } else {
                    service.stopBank(bankName);
                }
                
                sendResult(ctx, Map.of("status", SUCCESS, "available", wantsAvailable));
            } catch (Exception e) {
                sendError(ctx, Map.of("status", ERROR, "message", e.toString()));
            }
        }
    }
    
    private class ListBanksHandler implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            logger.debug("BankController handling 'listBanks' request");

            try {
                List<String> banks = manager.getAllBankNames();
                sendResult(ctx, Map.of("status", SUCCESS, "banks", banks));
            } catch (Exception e) {
                sendError(ctx, Map.of("status", ERROR, "message", e.toString()));
            }
        }
    }
    
    private void sendResult(Context ctx, Map data) throws JsonProcessingException {
        logger.debug("BankController sending result");

        String result = new ObjectMapper().writeValueAsString(data);
        ctx.status(200);
        ctx.result(result);        
    }

    private void sendError(Context ctx, Map data) throws JsonProcessingException {
        logger.debug("BankController sending error");

        String result = new ObjectMapper().writeValueAsString(data);
        ctx.status(500);
        ctx.result(result);        
    }
}
