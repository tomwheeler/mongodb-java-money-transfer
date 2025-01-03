package org.mongodb;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.mongodb.models.TransferDetails;
import org.mongodb.workers.ApplicationWorker;
import org.mongodb.workflows.MoneyTransferWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;


/**
 * This class provides a main method that, when invoked, initiates a new money transfer.
 * This is an alternative to using the <code>temporal</code> CLI to do so. The details
 * for the transfer, including the amount being transferred, are defined in an object
 * created at the top of the main method. This is later passed as an input parameter
 * to the Workflow method.
 *
 * Although that call may <i>appear</i> to execute the Workflow method, it's actually
 * submitting a Workflow Execution request to the Temporal Service. When the Temporal
 * Service receives that request, it queues a Task that specifies the input data and
 * type of Workflow to execute. The Worker, which is polling this same Task Queue,
 * accepts the Task and begins executing the Workflow and Activity code. When this
 * execution is complete, the Worker reports the result to the Temporal Service, at
 * which point the result is assigned to the caller. This example is synchronous;
 * the call to the Workflow method blocks until Workflow Execution is complete. The
 * Temporal SDK also provides an asynchronous way of doing this, which is especially
 * useful for long-running Workflows (Temporal Workflows are capable of running for
 * years).
 */
public class Starter {

    private static final Logger logger = LoggerFactory.getLogger(Starter.class);

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Incorrect number of arguments specified.");
            System.out.println("Format: SENDER RECIPIENT AMOUNT");
            System.exit(1);
        }

        String sender = args[0];
        if (sender == null || sender.trim().isEmpty()) {
            System.err.println("Sender name must not be empty");
            System.exit(1);
        }

        String recipient = args[1];
        if (recipient == null || recipient.trim().isEmpty()) {
            System.err.println("Recipient name must not be empty");
            System.exit(1);
        }

        int transferAmount = -1;
        try {
            transferAmount = Integer.parseInt(args[2]);
        } catch (NumberFormatException nfe) {
            logger.error("Could not parse specified amount {}", args[0], nfe);
            System.exit(1);
        }

        String referenceId = UUID.randomUUID().toString();

        TransferDetails details = new TransferDetails(sender, recipient, transferAmount, referenceId);
        logger.info("Will transfer {} from {} to {}", transferAmount, sender, recipient);

        String workflowId = String.format("transfer-%d-%s-to-%s", transferAmount, sender, recipient).toLowerCase();

        WorkflowServiceStubs serviceStub = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(serviceStub);
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(ApplicationWorker.TASK_QUEUE_NAME)
                .setWorkflowId(workflowId)
                .build();

        MoneyTransferWorkflow workflow = client.newWorkflowStub(MoneyTransferWorkflow.class, options);
        String confirmation = workflow.transfer(details);

        logger.info("Money Transfer complete. Confirmation: {}", confirmation);

        System.exit(0);
    }

}
