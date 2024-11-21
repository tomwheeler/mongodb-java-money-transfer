package org.mongodb;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Defines the Workflow used for money transfer.
 */
@WorkflowInterface
public interface MoneyTransferWorkflow {

    /**
     * Defines the method invoked when executing the money transfer Workflow.
     *
     * @param input The TransactionDetails instance that provides input data for the transfer
     * @return a confirmation message containing details of the completed transaction
     */
    @WorkflowMethod
    String transfer(TransactionDetails input);

}
