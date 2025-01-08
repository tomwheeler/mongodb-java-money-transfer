# **MongoDB Banking Application**

## **Setup**

### **1. Prerequisites**

* Java 11 or higher
* Apache Maven
* MongoDB cluster
* Temporal CLI

---

### **2. Environment Variables**

Set the MongoDB connection string as an environment variable. 
This is likely to be `mongodb://127.0.0.1:27017` for a local MongoDB cluster.

#### **Linux/MacOS**

```bash
export MONGO_CONNECTION_STRING="your_connection_string_here"
```

#### **Windows (Command Prompt)**

```cmd
set MONGO_CONNECTION_STRING=your_connection_string_here
```

#### **Windows (PowerShell)**

```powershell
$env:MONGO_CONNECTION_STRING="your_connection_string_here"
```


---

## **Compiling and Running the Application**

### **1. Compile the Application**

Use Maven to compile the code:

```bash
mvn compile
```

### **2. Start the Backend Service and GUI**

```bash
mvn exec:java -Dexec.mainClass="org.mongodb.banking.Main"
```

This starts the REST API server on `http://localhost:8480`.
It also launches a GUI that you can use to view the available 
bank accounts and control whether each of them will accept 
requests.

---

## **API Endpoints**

### **Base URL**

All endpoints are available at `http://localhost:8480`.

### **Create Account**

Creates a new bank account.

**Endpoint:**

```http
GET /api/createBank?bankName={name}&initialBalance={balance}
```

**Example:**

```bash
curl -X GET "http://localhost:8480/api/createBank?bankName=Maria&initialBalance=1000"
```

**Response:**

```json
{
  "status": "SUCCESS",
  "message": "Bank created successfully"
}
```

---

### **Get Balance**

Retrieve the balance of a bank account.

**Endpoint:**

```http
GET /api/balance?bankName={name}
```

**Example:**

```bash
curl -X GET "http://localhost:8480/api/balance?bankName=Maria"
```

**Response:**

```json
{
  "status": "SUCCESS",
  "balance": 1000
}
```

---

### **Deposit**

Deposit money into a specific bank.

**Endpoint:**

```http
GET /api/deposit?bankName={name}&amount={amount}&idempotencyKey={key}
```

**Example:**

```bash
curl -X GET "http://localhost:8480/api/deposit?bankName=Maria&amount=500&idempotencyKey=key123"
```

**Response:**

```json
{
  "status": "SUCCESS",
  "transaction-id": "D123456789"
}
```

---

### **Withdraw**

Withdraw money from a specific bank.

**Endpoint:**

```http
GET /api/withdraw?bankName={name}&amount={amount}&idempotencyKey={key}
```

**Example:**

```bash
curl -X GET "http://localhost:8480/api/withdraw?bankName=Maria&amount=200&idempotencyKey=key456"
```

**Response:**

```json
{
  "status": "SUCCESS",
  "transaction-id": "W987654321"
}
```

---

## **Testing the Application**

1. **Start the Backend Service**:
    
    ```bash
    mvn exec:java -Dexec.mainClass="org.mongodb.banking.Main"
    ```
    
2. **Test API Endpoints**: 
   - Use the provided `curl` commands to interact with the service. 
   - Verify the expected responses.
    
3. **Simulate Downtime**:
    
    - Stop a bank in the GUI (**Stop** button).
    - Attempt a transaction and observe that it fails due to the bank being offline.

4. **Add and Remove Banks**:
    
    - Add new banks using the `createBank` API.
    - Remove a bank directly from the MongoDB database:
        
        ```bash
        use bankingdemo
        db.accounts.deleteOne({ bankName: "Maria" })
        ```
        
    - Verify that the UI updates to reflect the changes.

---

## **Troubleshooting**

### **Environment Variable Not Set**

If the application cannot connect to MongoDB, ensure the `MONGO_CONNECTION_STRING`environment variable is set correctly.

### **Service Unavailable**

If the service is unreachable, ensure the backend server is running on `http://localhost:8480`.

### **MongoDB Issues**

Ensure the MongoDB instance is running and accessible from the application.
