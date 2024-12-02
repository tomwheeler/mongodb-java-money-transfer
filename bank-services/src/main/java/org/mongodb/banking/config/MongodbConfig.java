package org.mongodb.banking.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongodbConfig {
    private static final String CONNECTION_STRING = System.getenv("MONGO_CONNECTION_STRING");
    private static final String DATABASE_NAME = "banking";

    public static MongoDatabase getDatabase() {
        MongoClient client = MongoClients.create(CONNECTION_STRING);
        return client.getDatabase(DATABASE_NAME);
    }
}
