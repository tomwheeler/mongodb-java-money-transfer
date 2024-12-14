package org.mongodb.workers;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.mongodb.activities.AccountActivities;
import org.mongodb.activities.AccountActivitiesImpl;
import org.mongodb.workflows.MoneyTransferWorkflowImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Temporal Worker polls a specified Task Queue maintained by the Temporal
 * Service, seeking tasks related to the Workflows and/or Activities that it
 * is configured to support. Upon accepting such a Task, it executes the
 * relevant Workflow or Activity code and reports the result (or error)
 * back to the Temporal Service.
 *
 * This class provides a main method that will configure and instantiate a
 * Temporal Worker that will poll the Task Queue related to money transfer
 * operations. It supports the Workflow and each Activity defined for those
 * operations.
 */
public class ApplicationWorker {

    public static final String TASK_QUEUE_NAME = "money-transfer-task-queue";

    private static final Logger logger = LoggerFactory.getLogger(ApplicationWorker.class);

    public static void main(String[] args) {
        logger.info("Configuring Worker");

        WorkflowServiceStubs serviceStub = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(serviceStub);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE_NAME);

        worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);

        AccountActivities activities = new AccountActivitiesImpl();
        worker.registerActivitiesImplementations(activities);

        logger.info("Worker started. Awaiting tasks on queue: ", TASK_QUEUE_NAME);
        factory.start();
    }
}
