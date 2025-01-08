# Temporal-MongoDB Money Transfer Demo - Java

## Start the Temporal Service

The steps that follow require a Temporal Service running locally, so start that now by
running the command below:

```
temporal server start-dev --db-filename temporal-service.db
```

This starts the Temporal Service, with the Web UI listening on its default port (8233). 
If that's unavailable, use the `--ui-port` option to specify a different one. 

The `--db-filename` option specifies the path to the file that the Temporal Service 
will use to store Event History and other data. It will create this file if it does 
not exist. If this option is omitted, the Temporal Service does not persist this data 
to disk, so it will be lost if you restart the Temporal Service.

## How to run the code

1. Start the banking services (and associated GUI), as described in the
   [README for the other project](../bank-services/README.md).
2. Run the `main` method in the `org.mongodb.workers.ApplicationWorker` class:
   ```
   mvn compile exec:java -Dexec.mainClass="org.mongodb.workers.ApplicationWorker"
   ```
3. Run the `main` method in the `org.mongodb.Starter` class to start the 
   Workflow, specifying the sender, recipient, and amount in arguments to
   that program:
   ```
   mvn compile exec:java -Dexec.mainClass="org.mongodb.Starter" -Dexec.args="Maria David 100"
   ```

## Scenarios for the tutorial

1. **Happy Path**: 
   The banking services are running and the transfer completes successfully, 
   without interruption, on the first attempt. Provided that the services 
   are running and the accounts for Maria and David have been created, that
   is what should happen when you run the commands above.

2. **Automatic Retries**: 
   Shut down the sender's banking service and repeat the steps for the happy 
   path scenario. You'll see that the Workflow Execution does not complete. 
   Open the Web UI and the "Pending Activities" section will show what's wrong. 
   It will also show you how many attempts have been made for this Activity 
   so far and how long it will be until the next one. Start the banking service 
   and you should observe that, with the outage now resolved, the Workflow runs 
   to completion as if there was never an outage at all.

3. **Automatic Retries** (business-level failures):
   This is similar to the above scenario, but it doesn't have to be an outage 
   that triggers a retry. It will happen with business-level failures, too. 
   To see this, try initiating a transfer using a sender for which there is
   no account. The `withdraw` call will fail because that account doesn't 
   exist, but it will be retried. If you then created the account, then the 
   problem will be resolved and the Workflow will run to completion. It's also
   possible to customize the Retry Policy to specify a particular type of error 
   as non-retryable, as is the case for insufficient funds. In that case, the 
   Workflow will fail because our business logic demands that behavior.

4. **Durable Execution**: 
   Uncomment the `Workflow.sleep` statement in the Workflow implementation 
   class (and then restart the Worker, if it's already running, so the change 
   takes effect). Re-run the Workflow as in the Happy Path scenario, but while 
   that 30-second Timer is active (i.e., after the `withdraw` Activity but before 
   the `deposit` Activity), kill the Worker. Open the Temporal Web UI and observe
   that the withdraw Activity already completed, but the deposit Activity has 
   not yet run, and that there is no progress (because the only Worker is down).
   When you restart the Worker, you'll see that the Workflow continues from where
   it left off.

5. **Human-in-the-Loop**:
   Any transfer exceeding $500 is automatically placed on hold. A manager can 
   release the hold by sending a Signal to the Workflow Execution, which can
   be done with the `temporal` CLI, the Temporal Web UI, or the GUI used in
   the tutorial. 
