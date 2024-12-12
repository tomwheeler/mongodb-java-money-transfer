package org.mongodb.banking.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongodbConfig {

    public static final String CONN_STRING_ENV_VARNAME = "MONGO_CONNECTION_STRING";
    
    private static final String CONNECTION_STRING = System.getenv(CONN_STRING_ENV_VARNAME);
    private static final String DATABASE_NAME = "bankingdemo";

    /**
     * Returns the database with the default name, accessible through a
     * connection string defined through an environment variable.
     */
    public static MongoDatabase getDatabase() {
        return getDatabase(DATABASE_NAME, CONNECTION_STRING);
    }

    /**
     * Returns the database with the specified name, accessible through the
     * specified connection string.
     * 
     * @param databaseName the name of the database to connect to
     * @param connectionString specifies details for connecting to that database
     * @return the MongoDatabase corresponding to the input parameters
     */
    public static MongoDatabase getDatabase(String databaseName, String connectionString) {
        MongoClient client = MongoClients.create(connectionString);
        return client.getDatabase(databaseName);
    }
}
