# **MongoDB Banking Application**

## **Setup**

### **1. Prerequisites**

---

### **2. Environment Variables**

Set the MongoDB connection string as an environment variable.

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

### **2. Run the Backend Service**

```bash
mvn exec:java -Dexec.mainClass="org.mongodb.banking.Main"
```

This starts the REST API server on `http://localhost:8080`.

### **3. Run the UI**

Run the GUI application:

```bash
mvn exec:java -Dexec.mainClass="org.mongodb.banking.ui.BankUI"
```

The GUI will launch, displaying a list of bank accounts and their balances.

---

## **API Endpoints**

### **Base URL**

All endpoints are available at `http://localhost:8080`.

### **1. Create a Bank**

Create a new bank account.

**Endpoint:**

```http
GET /api/createBank?bankName={name}&initialBalance={balance}
```

**Example:**

```bash
curl -X GET "http://localhost:8080/api/createBank?bankName=DemoBank1&initialBalance=1000"
```

**Response:**

```json
{
  "status": "SUCCESS",
  "message": "Bank created successfully"
}
```

---

### **2. Get Bank Balance**

Retrieve the balance of a specific bank.

**Endpoint:**

```http
GET /api/balance?bankName={name}
```

**Example:**

```bash
curl -X GET "http://localhost:8080/api/balance?bankName=DemoBank1"
```

**Response:**

```json
{
  "status": "SUCCESS",
  "balance": 1000
}
```

---

### **3. Deposit to a Bank**

Deposit money into a specific bank.

**Endpoint:**

```http
GET /api/deposit?bankName={name}&amount={amount}&idempotencyKey={key}
```

**Example:**

```bash
curl -X GET "http://localhost:8080/api/deposit?bankName=DemoBank1&amount=500&idempotencyKey=key123"
```

**Response:**

```json
{
  "status": "SUCCESS",
  "transaction-id": "D123456789"
}
```

---

### **4. Withdraw from a Bank**

Withdraw money from a specific bank.

**Endpoint:**

```http
GET /api/withdraw?bankName={name}&amount={amount}&idempotencyKey={key}
```

**Example:**

```bash
curl -X GET "http://localhost:8080/api/withdraw?bankName=DemoBank1&amount=200&idempotencyKey=key456"
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
    
2. **Test API Endpoints**: Use the provided CURL commands to interact with the service. Verify the expected responses.
    
3. **Simulate Downtime**:
    
    - Stop a bank in the GUI (`Stop Bank` button).
    - Attempt a transaction and observe that it fails due to the bank being offline.
4. **Add and Remove Banks**:
    
    - Add new banks using the `createBank` API.
    - Remove a bank directly from the MongoDB database:
        
        ```bash
        db.accounts.deleteOne({ bankName: "DemoBank1" })
        ```
        
    - Verify that the UI updates to reflect the changes.

---

## **Troubleshooting**

### **Environment Variable Not Set**

If the application cannot connect to MongoDB, ensure the `MONGO_CONNECTION_STRING` environment variable is set correctly.

### **Service Unavailable**

If the service is unreachable, ensure the backend server is running on `http://localhost:8080`.

### **MongoDB Issues**

Ensure the MongoDB instance is running and accessible from the application.
