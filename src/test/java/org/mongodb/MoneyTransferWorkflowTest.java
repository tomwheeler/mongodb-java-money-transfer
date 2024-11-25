package org.mongodb;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import io.temporal.client.WorkflowException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    public void testTransfer() {
        AccountActivities activities = mock(AccountActivitiesImpl.class);
        worker.registerActivitiesImplementations(activities);
        testEnv.start();

        TransactionDetails details = new TransactionDetails("alice", "bob", "xyz123", 100);

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(ApplicationWorker.TASK_QUEUE_NAME)
                .setWorkflowId("transfer-workflow-" + details.getReferenceId())
                .build();

        MoneyTransferWorkflow workflow = workflowClient.newWorkflowStub(MoneyTransferWorkflow.class, options);

        workflow.transfer(details);

        verify(activities, times(1)).withdraw(details);
        verify(activities, times(1)).deposit(details);
    }

    // TODO - write a test to exercise the human-in-the-loop transfer scenario

}