package org.mongodb.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

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
     * amount of money from the sender's account.
     *
     * @param account identifies the account to be debited
     * @param amount the amount to debit that account
     * @param referenceId a caller-specified identifier for this request
     * @return A string containing the transaction ID for this operation
     */
    @ActivityMethod
    String withdraw(String account, int amount, String referenceId);

    /**
     * Contacts the appropriate banking service and deposits a specified
     * amount of money into the recipient's account.
     *
     * @param account identifies the account to be credited
     * @param amount the amount to credit to that account
     * @param referenceId a caller-specified identifier for this request
     * @return A string containing the transaction ID for this operation
     */
    @ActivityMethod
    String deposit(String account, int amount, String referenceId) ;

}
