package org.mongodb.workflows;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.mongodb.models.TransferDetails;

/**
 * Defines the Workflow used for money transfer.
 */
@WorkflowInterface
public interface MoneyTransferWorkflow {

    /**
     * Defines the method invoked when executing the money transfer Workflow.
     *
     * @param input The TransferDetails instance that specifies input data for the transfer
     * @return a confirmation message containing details of the completed transaction
     */
    @WorkflowMethod
    String transfer(TransferDetails input);

    /**
     * Defines the method invoked to approve a large transfer that is blocked as it awaits
     * manager approval
     *
     * @param managerName the name of the manager who approved the transfer
     */
    @SignalMethod
    void approve(String managerName);
}
