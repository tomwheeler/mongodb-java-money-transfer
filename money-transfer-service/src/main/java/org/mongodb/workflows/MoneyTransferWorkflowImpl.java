package org.mongodb.workflows;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import org.mongodb.activities.AccountActivities;
import org.mongodb.models.TransactionDetails;
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

    // Small transfers have manager approval by default
    private boolean hasManagerApproval = true;

    @Override
    public String transfer(TransactionDetails input) {
        logger.info("Starting Money Transfer Workflow");

        // Large transfers must be explicitly approved by a manager
        if (input.getAmount() > 500) {
            logger.info("This transfer is on hold awaiting manager approval");
            hasManagerApproval = false;
        }

        // The Workflow blocks here awaiting approval, if that was required. This approval
        // is sent using a Signal. When that Signal is received, the approve(String) method
        // is invoked and sets hasManagerApproval to true, causing the Workflow to proceed.
        Workflow.await(() -> hasManagerApproval);

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

        logger.info("Money Transfer Workflow now complete. Confirmation: {}", confirmation);
        return confirmation;
    }


    @Override
    public void approve(String managerName) {
        logger.info("This transfer has now been approved by {}", managerName);
        hasManagerApproval = true;
    }
}
