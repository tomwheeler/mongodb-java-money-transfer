package org.mongodb.bankapi;


import org.junit.Before;
import org.junit.Test;
import org.mongodb.exceptions.InsufficientFundsException;
import org.mongodb.exceptions.NoSuchAccountException;

import java.io.IOException;

import static org.junit.Assert.*;

public class MessageParserTest {

    private MessageParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new MessageParser();
    }

    @Test
    public void parseBalanceResponse() throws IOException {
        String body = "{\"status\":\"SUCCESS\",\"balance\":925}";
        assertEquals(925, parser.parseBalanceResponse(body));
    }

    @Test
    public void parseBalanceResponseNoSuchAccount() throws IOException {
        String body = "{\"message\":\"java.lang.IllegalArgumentException: " +
                "Bank with name 'Bogus' does not exist.\",\"status\":\"ERROR\"}";

        NoSuchAccountException e = assertThrows(NoSuchAccountException.class, () -> parser.parseBalanceResponse(body));
        assertTrue(e.getMessage().contains("Bank with name 'Bogus' does not exist."));
    }

    @Test
    public void parseDepositResponse() throws IOException {
        String body = "{\"status\":\"SUCCESS\",\"transactionId\":\"D5393255438\"}";
        assertEquals("D5393255438", parser.parseDepositResponse(body));
    }

    @Test
    public void parseDepositResponseNoSuchAccount() throws IOException {
        String body = "{\"message\":\"java.lang.IllegalArgumentException: " +
                "Bank with name 'Bogus' does not exist.\",\"status\":\"ERROR\"}";

        NoSuchAccountException e = assertThrows(NoSuchAccountException.class, () -> parser.parseDepositResponse(body));
        assertTrue(e.getMessage().contains("Bank with name 'Bogus' does not exist."));
    }

    @Test
    public void parseWithdrawResponse() throws IOException {
        String body = "{\"status\":\"SUCCESS\",\"transactionId\":\"W3468134039\"}";
        assertEquals("W3468134039", parser.parseWithdrawResponse(body));
    }

    @Test
    public void parseWithdrawResponseInsufficientFunds() throws IOException {
        String body = "{\"message\":\"org.mongodb.banking.exceptions.InsufficientFundsException: " +
                "Insufficient funds: balance=500, withdrawal=750\",\"status\":\"ERROR\"}";
        InsufficientFundsException e = assertThrows(InsufficientFundsException.class, () -> parser.parseWithdrawResponse(body));
        assertTrue(e.getMessage().contains("Insufficient funds: balance=500, withdrawal=750"));
    }

    @Test
    public void parseWithdrawResponseNoSuchAccount() throws IOException {
        String body = "{\"message\":\"java.lang.IllegalArgumentException: " +
                "Bank with name 'Bogus' does not exist.\",\"status\":\"ERROR\"}";

        NoSuchAccountException e = assertThrows(NoSuchAccountException.class, () -> parser.parseWithdrawResponse(body));
        assertTrue(e.getMessage().contains("Bank with name 'Bogus' does not exist."));
    }
}