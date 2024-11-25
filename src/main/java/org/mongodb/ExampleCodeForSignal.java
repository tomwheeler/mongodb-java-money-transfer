package org.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

/**
 * This is an example for Tim, illustrating how to send a Signal (to approve
 * a transfer requiring manager approval) from Java code. You would run this
 * in place of running the "temporal workflow signal" command currently near
 * the README file. Once this code has been integrated into the web app, you
 * can delete this class.
 */
public class ExampleCodeForSignal {

    private static final Logger logger = LoggerFactory.getLogger(Starter.class);

    public static void main(String[] args) {
        String approverName = "Maria Manager";
        if (args.length == 1) {
            approverName = args[0];
        }

        // The next several lines are similar to what's in the Starter class. Sending a
        // Signal to a Workflow Execution involves sending a request to the Temporal Service,
        // much like happens when you start the Workflow Execution. We must specify the
        // Workflow ID corresponding to the Workflow Execution we want to Signal (i.e.,
        // the one that was launched in the Starter class). I hardcoded that Workflow ID
        // here since I don't have access to the TransactionDetails instance used by the
        // Starter to create the Workflow ID.
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        MoneyTransferWorkflow workflow = client.newWorkflowStub(MoneyTransferWorkflow.class, "transfer-workflow-XF12345");

        // The big difference is that, instead of calling the Workflow method (which would start
        // the Workflow), we call the Signal method
        workflow.approve(approverName);
    }
}
