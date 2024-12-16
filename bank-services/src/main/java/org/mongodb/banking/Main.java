package org.mongodb.banking;


import com.mongodb.client.MongoDatabase;
import org.mongodb.banking.config.MongodbConfig;
import org.mongodb.banking.repository.BankRepositoryImpl;
import org.mongodb.banking.ui.view.BankUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final int SERVICE_PORT = 8080;

    public static void main(String[] args) {
        try {
            logger.info("Starting application");

            if (System.getenv(MongodbConfig.CONN_STRING_ENV_VARNAME) == null) {
                logger.error(MongodbConfig.CONN_STRING_ENV_VARNAME + " environment variable is not set!");
                System.exit(1);
            }

            logger.debug("Setting up up MongoDB connection");
            MongoDatabase database = MongodbConfig.getDatabase();
            BankRepositoryImpl repository = new BankRepositoryImpl(database);

            logger.debug("Initializing BankManager");
            BankManager manager = new BankManager(repository);

            logger.debug("Starting the server");
            BankController controller = new BankController(manager, SERVICE_PORT);
            controller.start();

            if (args.length > 0) {
                for (String arg : args) {
                    if ("--no-gui".equals(arg)) {
                        return;
                    }
                }
            }
            logger.debug("Launching the GUI");
            BankUI gui = new BankUI("localhost", SERVICE_PORT);
            gui.display();
        } catch (Exception e) {
            logger.error("Error encountered while running the application", e);
        }
    }
}
