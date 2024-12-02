package org.mongodb.banking;

import com.mongodb.client.MongoDatabase;
import org.mongodb.banking.config.MongodbConfig;
import org.mongodb.banking.repository.BankRepositoryImpl;

public class Main {
    public static void main(String[] args) {
        try {
            // Set up MongoDB connection
            MongoDatabase database = MongodbConfig.getDatabase();
            BankRepositoryImpl repository = new BankRepositoryImpl(database);

            // Initialize BankManager
            BankManager manager = new BankManager(repository);

            // Start the server
            BankController controller = new BankController(manager, 8080);
            controller.start();

            System.out.println("Banking service is running at http://localhost:8080");
        } catch (Exception e) {
            System.err.println("Failed to start the banking service: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
