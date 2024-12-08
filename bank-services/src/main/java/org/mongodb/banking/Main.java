package org.mongodb.banking;

import com.mongodb.client.MongoDatabase;
import org.mongodb.banking.config.MongodbConfig;
import org.mongodb.banking.repository.BankRepositoryImpl;

public class Main {

    private static int SERVICE_PORT = 8080;

    public static void main(String[] args) {
        try {
            // Set up MongoDB connection
            MongoDatabase database = MongodbConfig.getDatabase();
            BankRepositoryImpl repository = new BankRepositoryImpl(database);

            // Initialize BankManager
            BankManager manager = new BankManager(repository);

            // Start the server
            BankController controller = new BankController(manager, SERVICE_PORT);
            controller.start();

            System.out.printf("Banking service is running at http://localhost:%d\n", SERVICE_PORT);
        } catch (Exception e) {
            System.err.println("Failed to start the banking service: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
