package org.mongodb;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class provides a main method that, when invoked, initiates a new money transfer.
 * This is an alternative to using the <code>temporal</code> CLI to do so. The details
 * for the transfer, including the amount being transferred, are defined in an object
 * created at the top of the main method. This is later passed as an input parameter
 * to the Workflow method.
 *
 * Although that call may <i>appear</i> to executes the Workflow method, it's actually
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
        // defaults to transferring $100, but you can specify a different amount at runtime
        int transferAmount = 100;
        if (args.length == 1) {
            try {
                transferAmount = Integer.valueOf(args[0]);
            } catch (NumberFormatException nfe) {
                logger.error("Could not parse specified amount: " + args[0], nfe);
            }
        }

        TransactionDetails details = new TransactionDetails("Tom", "Ted", "XF12345", transferAmount);
        logger.info("Will transfer {} from {} to {}", details.getAmount(), details.getSender(), details.getRecipient());

        WorkflowServiceStubs serviceStub = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(serviceStub);
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(ApplicationWorker.TASK_QUEUE_NAME)
                .setWorkflowId("transfer-workflow-" + details.getReferenceId())
                .build();

        MoneyTransferWorkflow workflow = client.newWorkflowStub(MoneyTransferWorkflow.class, options);
        String confirmation = workflow.transfer(details);

        System.out.printf("Money Transfer complete. Confirmation: %s\n", confirmation);

        System.exit(0);
    }

}
