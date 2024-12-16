# Temporal-MongoDB Money Transfer Demo - Java (Work In Progress)

## How to run the code

1. Start the banking services (and optionally, the GUI showing the status 
   and account balances)
2. Run the `main` method in the `org.mongodb.workers.ApplicationWorker` class (I'm 
   just doing it from the IDE right now, but you could also run 
   `mvn compile exec:java -Dexec.mainClass="org.mongodb.workers.ApplicationWorker"`
   to do it)
3. Run the `main` method in the `org.mongodb.Starter` class (from the IDE
   or `mvn compile exec:java -Dexec.mainClass="org.mongodb.Starter" -Dexec.args="Alice Bob 100"`)


## Potential demonstrations for the tutorial

1. **Happy Path**: 
   The banking services are running and the transfer completes successfully, 
   without interruption, on the first attempt. 

2. **Automatic Retries**: 
   Shut down the sender's banking service and repeat the steps for the happy 
   path scenario. You'll observe that the Workflow Execution does not complete. 
   Open the Web UI and the "Pending Activities" area will show what's wrong. 
   It will also show you how many attempts have been made for this Activity 
   so far and how long it will be until the next one. Start the banking service 
   and you should observe that, with the outage now resolved, the Workflow runs 
   to completion as if there was never an outage at all.

3. **Automatic Retries** (business-level failures):
   This is similar to the above scenario, but it doesn't have to be an outage 
   that triggers a retry. It will happen with business-level failures, too. 
   Try initiating a transfer for an amount higher than the sender's current 
   balance to observe this. It will fail because there's insufficient funds 
   in the account. If you add funds to the account (see the README file for 
   the bank service to find the `curl` command that deposits money into the 
   account), then the problem will be resolved and the Workflow will run to 
   completion. It's also possible to customize the Retry Policy to specify a 
   particular type of error as non-retryable, so we can make the Workflow fail 
   in this case (should the business logic require that).

4. **Durable Execution**: 
   Uncomment the `Workflow.sleep(20000)` statement in the Workflow implementation 
   class (and then restart the Worker, if it's already running, so the change 
   takes effect). Re-run the Workflow as in the Happy Path scenario, but while 
   that 20-second Timer is active (i.e., after the withdraw Activity but before 
   the deposit Activity), kill the Worker. Open the Temporal Web UI and observe 
   that the withdraw Activity already completed, but the deposit Activity has 
   not yet run, and that there is no progress (because the only Worker is down).
   Restart the Worker and then observe that the Workflow continues from where 
   it left off.

5. **Human-in-the-Loop**:
   We can place a hold on any transfer over some threshold (e.g., $500). A 
   manager could release the hold by sending a Signal or reject the transfer 
   by specifying a reason and terminating the Workflow. Both of those operations 
   can be performed with the `temporal` CLI. You can also use the Temporal 
   Web UI to approve the transfer (which uses a method provided by Temporal's 
   Java API to send the Signal). If you want to terminate the Workflow Execution
   (that is, halt the transaction), you can use the terminate feature in the 
   Temporal Web UI.

   The code now supports this. To try it, initiate a transfer for more than
   $500:

   ```bash
   $ mvn compile exec:java \
       -Dexec.mainClass="org.mongodb.Starter" \
	   -Dexec.args="Alice Bob 100"
   ```

   The Workflow Execution will begin, but immediately block while awaiting
   approval. You can send a Signal to grant that approval using the CLI
   (the strange quoting for the `--input` argument is intentional, as the 
   provided value must be in JSON format):

   ```bash
   $ temporal workflow signal \
       --workflow-id transfer-workflow-XF12345 \
	   --name approve --input '"Ron Smith"'
   ```


### Licensing
* Tim and I agreed that we should use the MIT license for the code, with attribution 
  to both MongoDB and Temporal

