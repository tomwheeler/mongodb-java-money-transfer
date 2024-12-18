package org.mongodb.workflows;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.mongodb.activities.AccountActivities;
import org.mongodb.exceptions.InsufficientFundsException;
import org.mongodb.exceptions.NoSuchAccountException;
import org.mongodb.models.TransferDetails;
import org.slf4j.Logger;

import java.time.Duration;

/**
 * Implements the money transfer Workflow.
 */
public class MoneyTransferWorkflowImpl implements MoneyTransferWorkflow {

    private static final Logger logger = Workflow.getLogger(MoneyTransferWorkflowImpl.class);

    private final ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setMaximumInterval(Duration.ofSeconds(60))
                    .setBackoffCoefficient(2.0)
                    .setDoNotRetry(
                            InsufficientFundsException.class.getName())  // comment out to retry these
                    .build())
            .build();

    private final AccountActivities activitiesStub = Workflow.newActivityStub(AccountActivities.class, options);

    // Small transfers have manager approval by default
    private boolean hasManagerApproval = true;

    @Override
    public String transfer(TransferDetails input) {
        logger.info("Starting Money Transfer Workflow");

        // Large transfers must be explicitly approved by a manager
        if (input.getAmount() > 500) {
            logger.warn("This transfer is on hold awaiting manager approval");
            hasManagerApproval = false;
        }

        // The Workflow blocks here awaiting approval, if that was required. This approval
        // is sent using a Signal. When that Signal is received, the approve(String) method
        // is invoked and sets hasManagerApproval to true, causing the Workflow to proceed.
        Workflow.await(() -> hasManagerApproval);

        // withdraw money from the sender's account (this returns a transaction ID).
        logger.info("Starting withdraw operation");
        String withdrawKey = String.format("withdrawal-for-%s", input.getReferenceId());
        String withdrawResult = activitiesStub.withdraw(input.getSender(), input.getAmount(), withdrawKey);

        // NOTE: You can uncomment the next statement and restart the Worker to add a 30-second delay
        //       between the withdraw and deposit in future transfers. That delay will provide you
        //       with time to kill the Worker, thereby simulating an application crash. When you
        //       start the Worker again afterward, you will see "durable execution" in action by
        //       observing that the Workflow resumes from where the crash occurred and then runs to
        //       completion. That is, it will not repeat the withdrawal, which already completed, but
        //       instead starts the deposit, which had not yet run).
        //Workflow.sleep(Duration.ofSeconds(30));

        // deposit money into the recipient's account (this also returns a transaction ID)
        logger.info("Starting deposit operation");
        String depositKey = String.format("deposit-for-%s", input.getReferenceId());
        String depositResult = activitiesStub.deposit(input.getRecipient(), input.getAmount(), depositKey);

        String confirmation = String.format("withdrawal=%s, deposit=%s", withdrawResult, depositResult);

        logger.info("Money Transfer Workflow now complete. Confirmation: {}", confirmation);
        return confirmation;
    }

    @Override
    public void approve(String managerName) {
        logger.info("This transfer has now been approved by {}", managerName);
        hasManagerApproval = true;
    }
}
