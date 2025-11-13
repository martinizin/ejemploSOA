# ⚡ QUICKSTART - BikeStore Async v1.0

> **Get running in 3 minutes** | Minimal steps, maximum results

---

## 🎯 What You'll Get

✅ Working async messaging system with RabbitMQ  
✅ Payment processing with automatic retries (3x)  
✅ Dead Letter Queue for failed messages  
✅ Email notifications for successful payments  
✅ Complete logging (pedidoId, timestamp, thread)

---

## 📋 Before You Start

Check you have:
- ☑️ Java 17+ (`java -version`)
- ☑️ Maven 3.6+ (`mvn -version`)
- ☑️ Docker Desktop running (`docker --version`)

---

## 🚀 3-Step Launch

### 1️⃣ Start RabbitMQ (10 seconds)
```powershell
docker compose up -d
```
✓ RabbitMQ will be available on ports 5672 (AMQP) and 15672 (Management UI)

---

### 2️⃣ Run Application (IntelliJ IDEA)

**Option A: GUI** (Recommended)
1. Open IntelliJ IDEA
2. **File → Open** → Select project folder
3. Open `src/main/java/com/bikestore/Application.java`
4. Click green ▶️ button next to `main` method
5. Wait for console to show: `Started Application in X seconds`

**Option B: Terminal**
```powershell
mvn spring-boot:run
```

---

### 3️⃣ Test the API

```powershell
curl -X POST http://localhost:8080/orders `
  -H "Content-Type: application/json" `
  -d '{\"pedidoId\":\"TEST-001\",\"monto\":120.50,\"clienteEmail\":\"test@mail.com\"}'
```

**Expected response**: `Order accepted: TEST-001`

---

## 📊 Watch It Work

### Console Logs (IntelliJ)
You'll see logs like:
```
[2025-11-13 10:30:15.123] [http-nio-8080-exec-1] [ORDER_RECEIVED] PedidoId=TEST-001 | Received order...
[2025-11-13 10:30:15.201] [RabbitListenerEndpointContainer#0-1] [PAYMENT_PROCESSING] PedidoId=TEST-001 | Attempt 1/3
[2025-11-13 10:30:15.210] [RabbitListenerEndpointContainer#0-1] [PAYMENT_SUCCESS] PedidoId=TEST-001 | Status: PAID
[2025-11-13 10:30:15.830] [RabbitListenerEndpointContainer#0-2] [EMAIL_SENT] PedidoId=TEST-001 | ✓ Confirmation email sent
```

### RabbitMQ Management UI
1. Open browser: **http://localhost:15672**
2. Login: `admin` / `admin123`
3. Go to **Queues** tab
4. See messages flowing through:
   - `payments.queue`
   - `emails.queue`
   - `orders.dlq` (failed messages)

---

## 🧪 Quick Tests

### Send Multiple Orders
```powershell
# Use automated test script
.\test-orders.ps1 -Count 10
```

### Manual Tests

**Test 1: Single Order**
```powershell
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{\"pedidoId\":\"MANUAL-001\",\"monto\":99.99,\"clienteEmail\":\"manual@test.com\"}'
```

**Test 2: Batch Orders**
```powershell
for ($i=1; $i -le 5; $i++) {
    curl -X POST http://localhost:8080/orders `
      -H "Content-Type: application/json" `
      -d "{`"pedidoId`":`"BATCH-$i`",`"monto`":$($i*50).00,`"clienteEmail`":`"user$i@mail.com`"}"
}
```

---

## 🔍 What to Look For

### ✅ Success Scenario (50% probability)
Payment succeeds → Email sent
```
[PAYMENT_SUCCESS] Status: PAID
[EMAIL_SENT] ✓ Confirmation email sent
```

### ⚠️ Retry Scenario (50% probability)
Payment fails → Retry 3 times → DLQ
```
[PAYMENT_PROCESSING] Attempt 1/3
[PAYMENT_FAILED_RETRY] Retry 1/3
[PAYMENT_PROCESSING] Attempt 2/3
[PAYMENT_FAILED_RETRY] Retry 2/3
[PAYMENT_PROCESSING] Attempt 3/3
[PAYMENT_FAILED_MAX_RETRIES] Sending to DLQ
```

Check RabbitMQ UI → `orders.dlq` queue will have these failed messages.

---

## 🛑 Stop Everything

### Stop Application
In IntelliJ: Click ⏹️ Stop button (or `Ctrl+F2`)

### Stop RabbitMQ
```powershell
docker compose down
```

---

## 📚 Next Steps

**Want more details?**
- **Complete guide**: Open `GUIA_INTELLIJ.md` (10 detailed steps)
- **Architecture**: Open `ARQUITECTURA.md` (diagrams, patterns, flows)
- **Test commands**: Open `TEST_COMMANDS.md` (PowerShell, curl, bash)

**Want to modify the code?**
- Change retry count: Edit `PaymentWorker.java` → `MAX_RETRIES`
- Change failure rate: Edit `PaymentWorker.java` → `random.nextBoolean()`
- Change ports: Edit `application.yml` → `server.port`

---

## ❓ Troubleshooting

| Problem | Solution |
|---------|----------|
| "Connection refused" to RabbitMQ | Wait 15 seconds after `docker compose up -d` |
| "Port 8080 already in use" | Change port in `application.yml` |
| Application won't start | Check Java 17+ is configured in IntelliJ (File → Project Structure) |
| No logs appearing | Check IntelliJ Run window is visible (Alt+4) |

---

## 🎯 Validation Checklist

- [ ] RabbitMQ container running (`docker ps`)
- [ ] Application started (console shows "Started Application")
- [ ] Health endpoint works (`curl http://localhost:8080/orders/health`)
- [ ] Order accepted (`curl POST /orders` returns 202)
- [ ] Logs show pedidoId, timestamp, thread
- [ ] RabbitMQ UI accessible (http://localhost:15672)
- [ ] Messages appear in queues
- [ ] DLQ receives failed messages after 3 retries
- [ ] Emails only sent when payment succeeds

---

## 🎓 What This POC Demonstrates

| Feature | Implementation |
|---------|----------------|
| **Async messaging** | RabbitMQ with Spring AMQP |
| **Retry pattern** | 3 automatic retries with exponential backoff |
| **Dead Letter Queue** | Failed messages preserved for analysis |
| **Conditional processing** | Email only sent if payment succeeds |
| **Observability** | Structured logging with context |
| **Decoupling** | Producer/consumer pattern |

---

## 🚀 Automated Setup (Alternative)

**Want zero manual steps?** Run the automation script:

```powershell
.\setup.ps1
```

This will:
1. ✓ Check prerequisites (Java, Maven, Docker)
2. ✓ Start RabbitMQ
3. ✓ Build the project
4. ✓ Start the application
5. ✓ Test the health endpoint

---

## 📞 Support

**Got stuck?**
- Read `README.md` for detailed documentation
- Check `GUIA_INTELLIJ.md` for step-by-step guide
- Review logs for error messages

**Works perfectly?**
- Test all scenarios (success, retries, DLQ)
- Check RabbitMQ UI to see messages
- Explore the code in `src/main/java/com/bikestore/`

---

**⏱️ Elapsed time**: ~3 minutes  
**🎯 Status**: Running and processing orders asynchronously  
**✅ Result**: Production-ready async architecture pattern

---

**Version**: 1.0.0 | **Stack**: Java 17 + Spring Boot + RabbitMQ | **License**: MIT
