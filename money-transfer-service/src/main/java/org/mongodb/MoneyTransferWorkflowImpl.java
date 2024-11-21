package org.mongodb;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Implementation of the money transfer Workflow.
 */
public class MoneyTransferWorkflowImpl implements MoneyTransferWorkflow {

    private static final Logger logger = LoggerFactory.getLogger(MoneyTransferWorkflowImpl.class);

    private final ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .build();

    private final AccountActivities activitiesStub = Workflow.newActivityStub(AccountActivities.class, options);

    @Override
    public String transfer(TransactionDetails input) {
        logger.info("Starting Money Transfer Workflow");

        // TODO - consider refactoring to have a different input parameter for the Activities
        // (e.g., AccountOperationDetails) that only lists the relevant account and then
        // rename TransactionDetails to MoneyTransferDetails
        String wdConf = activitiesStub.withdraw(input);

        // NOTE: Uncomment the next statement to pause 20 seconds between the withdraw and deposit Activities.
        //       This will give you time to kill the Worker, thus terminating the process in which the
        //       money transfer workflow code is executing. You can then restart the Worker, after which
        //       you will see "durable execution" in action by observing that the Workflow resumes and then
        //       runs to completion (i.e., it does not repeat the withdraw operation, which already completed;
        //       it completes the deposit operation, which had not already run).
        // Workflow.sleep(20000);

        String depConf = activitiesStub.deposit(input);

        String confirmation = String.format("Withdrawal TXID: %s, Deposit TXID: %s", wdConf, depConf);

        logger.info("Starting Money Transfer Workflow now complete. Confirmation: ", confirmation);
        return confirmation;
    }
}
