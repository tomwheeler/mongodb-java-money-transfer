package org.mongodb;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.activities.AccountActivities;
import org.mongodb.activities.AccountActivitiesImpl;
import org.mongodb.models.TransferDetails;
import org.mongodb.workers.ApplicationWorker;
import org.mongodb.workflows.MoneyTransferWorkflow;
import org.mongodb.workflows.MoneyTransferWorkflowImpl;

public class MoneyTransferWorkflowTest {

    private TestWorkflowEnvironment testEnv;
    private Worker worker;
    private WorkflowClient workflowClient;

    @Before
    public void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        worker = testEnv.newWorker(ApplicationWorker.TASK_QUEUE_NAME);
        worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);
        workflowClient = testEnv.getWorkflowClient();
    }

    @After
    public void tearDown() {
        testEnv.close();
    }

    @Test
    public void testBasicTransfer() {
        AccountActivities activities = mock(AccountActivitiesImpl.class);
        worker.registerActivitiesImplementations(activities);
        testEnv.start();

        TransferDetails input = new TransferDetails("alice", "bob", 100, "abc123");
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(ApplicationWorker.TASK_QUEUE_NAME)
                .setWorkflowId("transfer-workflow-" + input.getReferenceId())
                .build();

        MoneyTransferWorkflow workflow = workflowClient.newWorkflowStub(MoneyTransferWorkflow.class, options);

        workflow.transfer(input);

        verify(activities, times(1)).withdraw(input.getSender(), input.getAmount(), input.getReferenceId());
        verify(activities, times(1)).deposit(input.getRecipient(), input.getAmount(), input.getReferenceId());
    }

    @Test
    public void testTransferWithManagerApproval() throws ExecutionException, InterruptedException {
        AccountActivities activities = mock(AccountActivitiesImpl.class);
        worker.registerActivitiesImplementations(activities);
        testEnv.start();

        TransferDetails input = new TransferDetails("carlos", "diana", 750, "xyz789");
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(ApplicationWorker.TASK_QUEUE_NAME)
                .setWorkflowId("transfer-workflow-" + input.getReferenceId())
                .build();

        MoneyTransferWorkflow workflow = workflowClient.newWorkflowStub(MoneyTransferWorkflow.class, options);

        // Since this unit test verifies the human-in-the-loop example in which the
        // transfer awaits manager approval before proceeding, it starts the Workflow
        // Execution asynchronously
        CompletableFuture<String> result = WorkflowClient.execute(workflow::transfer, input);

        // Although the Workflow Execution has started, it is immediately placed on hold, and
        // neither of these Activities should have been called yet.
        verify(activities, times(0)).withdraw(input.getSender(), input.getAmount(), input.getReferenceId());
        verify(activities, times(0)).deposit(input.getRecipient(), input.getAmount(), input.getReferenceId());

        // Calling the Signal method allows the transfer to proceed
        workflow.approve("Maria Manager");

        // When the result is available, the Workflow Execution is complete
        String confirmation = result.get();
        assertTrue(confirmation.contains("withdrawal="));
        assertTrue(confirmation.contains("deposit="));

        // Both the withdrawal and deposit took place
        verify(activities, times(1)).withdraw(input.getSender(), input.getAmount(), input.getReferenceId());
        verify(activities, times(1)).deposit(input.getRecipient(), input.getAmount(), input.getReferenceId());
    }

}