package org.mongodb.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.mongodb.models.TransactionDetails;

/**
 * Declares the operations that are used to carry out the money transfer.
 *
 * Since these involve interaction with external systems, and therefore
 * depend on the availability and correct behavior of those systems, they
 * are inherently non-deterministic since they could produce different
 * results from one invocation to the next. For this reason, they are
 * defined as Activities, which are methods that are separate from (but
 * referenced by) the Workflow method. Encapsulating failure-prone code
 * in Activity methods enables the Temporal Service to track the execution
 * of each operation and automatically retry it upon failure. As a result,
 * your application will withstand network or service outages, because
 * the retry will succeed once the outage is resolved, at which point
 * your code will continue with the execution as if it had never occurred.
 */
@ActivityInterface
public interface AccountActivities {

    /**
     * Contacts the appropriate banking service and withdraws a specified
     * amount of money from the source account.
     *
     * @param input The TransactionDetails object that contains details about
     *              the source account and amount of money to withdraw.
     * @return A string containing the transaction ID for this operation
     */
    @ActivityMethod
    String withdraw(TransactionDetails input);

    /**
     * Contacts the appropriate banking service and deposits a specified
     * amount of money into the target account.
     *
     * @param input The TransactionDetails object that contains details about
     *              the target account and amount of money to deposit.
     * @return A string containing the transaction ID for this operation
     */
    @ActivityMethod
    String deposit(TransactionDetails input) ;

    // NOTE: For the sake of simplicity, this example does not cover
    // Saga/compensation, but we could mention it in the tutorial and point
    // to the Javadoc here:
    // https://www.javadoc.io/doc/io.temporal/temporal-sdk/latest/io/temporal/workflow/Saga.html

}
