package org.mongodb.banking.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongodbConfig {
    private static final String CONNECTION_STRING = System.getenv("MONGO_CONNECTION_STRING");
    private static final String DATABASE_NAME = "banking";

    /**
     * Returns the database with the default name, accessible through a connection
     * string defined in the <code>MONGO_CONNECTION_STRING</code> environment variable.
     */
    public static MongoDatabase getDatabase() {
        return getDatabase(DATABASE_NAME, CONNECTION_STRING);
    }

    /**
     * Returns the database with the specified name, accessible through the specified
     * connection string.
     */
    public static MongoDatabase getDatabase(String databaseName, String connectionString) {
        MongoClient client = MongoClients.create(connectionString);
        return client.getDatabase(databaseName);
    }
}
