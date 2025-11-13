# Log Examples - BikeStore Async v1.0

> **Real log outputs** for testing and validation

---

## 📊 Log Format

```
[TIMESTAMP] [THREAD] [STEP] PedidoId=UUID | MESSAGE
```

**Components**:
- **TIMESTAMP**: `yyyy-MM-dd HH:mm:ss.SSS`
- **THREAD**: Java thread name
- **STEP**: Operation identifier (uppercase, snake_case)
- **PedidoId**: Unique order identifier
- **MESSAGE**: Human-readable description

---

## ✅ Scenario 1: Successful Payment → Email Sent

### Input
```bash
curl -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"pedidoId":"SUCCESS-001","monto":120.50,"clienteEmail":"success@mail.com"}'
```

### Complete Log Output
```
[2025-11-13 14:23:45.123] [http-nio-8080-exec-1] [ORDER_RECEIVED] PedidoId=SUCCESS-001 | Received order for success@mail.com - Amount: $120.5
[2025-11-13 14:23:45.145] [http-nio-8080-exec-1] [ORDER_PUBLISHED] PedidoId=SUCCESS-001 | Publishing to exchange: orders.exchange
[2025-11-13 14:23:45.156] [http-nio-8080-exec-1] [ORDER_SENT_TO_PAYMENT] PedidoId=SUCCESS-001 | Message sent to payment processing
[2025-11-13 14:23:45.201] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_PROCESSING] PedidoId=SUCCESS-001 | Attempt 1/3 - Amount: $120.5
[2025-11-13 14:23:45.210] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_SUCCESS] PedidoId=SUCCESS-001 | Payment processed successfully - Status: PAID
[2025-11-13 14:23:45.215] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_FORWARDED_TO_EMAIL] PedidoId=SUCCESS-001 | Order sent to email queue
[2025-11-13 14:23:45.320] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-2] [EMAIL_RECEIVED] PedidoId=SUCCESS-001 | Email task received - Status: PAID
[2025-11-13 14:23:45.325] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-2] [EMAIL_SENDING] PedidoId=SUCCESS-001 | Sending confirmation email to: success@mail.com
[2025-11-13 14:23:45.830] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-2] [EMAIL_SENT] PedidoId=SUCCESS-001 | ✓ Confirmation email sent successfully to success@mail.com
```

### Timeline Breakdown
| Time (ms) | Thread | Step | Action |
|-----------|--------|------|--------|
| 0 | exec-1 | ORDER_RECEIVED | HTTP request arrives |
| +22 | exec-1 | ORDER_PUBLISHED | Sending to RabbitMQ |
| +33 | exec-1 | ORDER_SENT_TO_PAYMENT | Published successfully |
| +78 | Listener#0-1 | PAYMENT_PROCESSING | Payment worker starts |
| +87 | Listener#0-1 | PAYMENT_SUCCESS | Payment succeeded |
| +92 | Listener#0-1 | PAYMENT_FORWARDED_TO_EMAIL | Sent to email queue |
| +197 | Listener#0-2 | EMAIL_RECEIVED | Email worker receives |
| +202 | Listener#0-2 | EMAIL_SENDING | Sending email (SMTP simulation) |
| +707 | Listener#0-2 | EMAIL_SENT | Email confirmed |

**Total processing time**: ~707ms

---

## ⚠️ Scenario 2: Payment Fails → 3 Retries → DLQ

### Input
```bash
curl -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"pedidoId":"FAIL-001","monto":75.00,"clienteEmail":"fail@mail.com"}'
```

### Complete Log Output
```
[2025-11-13 14:25:10.001] [http-nio-8080-exec-2] [ORDER_RECEIVED] PedidoId=FAIL-001 | Received order for fail@mail.com - Amount: $75.0
[2025-11-13 14:25:10.015] [http-nio-8080-exec-2] [ORDER_PUBLISHED] PedidoId=FAIL-001 | Publishing to exchange: orders.exchange
[2025-11-13 14:25:10.021] [http-nio-8080-exec-2] [ORDER_SENT_TO_PAYMENT] PedidoId=FAIL-001 | Message sent to payment processing

[2025-11-13 14:25:10.089] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_PROCESSING] PedidoId=FAIL-001 | Attempt 1/3 - Amount: $75.0
[2025-11-13 14:25:10.095] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_FAILED_RETRY] PedidoId=FAIL-001 | Payment failed - Retry 1/3

[2025-11-13 14:25:12.110] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_PROCESSING] PedidoId=FAIL-001 | Attempt 2/3 - Amount: $75.0
[2025-11-13 14:25:12.116] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_FAILED_RETRY] PedidoId=FAIL-001 | Payment failed - Retry 2/3

[2025-11-13 14:25:14.135] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_PROCESSING] PedidoId=FAIL-001 | Attempt 3/3 - Amount: $75.0
[2025-11-13 14:25:14.142] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_FAILED_MAX_RETRIES] PedidoId=FAIL-001 | Payment failed after 3 attempts - Sending to DLQ
```

### Timeline Breakdown
| Time (sec) | Thread | Step | Action |
|------------|--------|------|--------|
| 0.000 | exec-2 | ORDER_RECEIVED | HTTP request arrives |
| 0.015 | exec-2 | ORDER_PUBLISHED | Sent to RabbitMQ |
| 0.089 | Listener#0-1 | PAYMENT_PROCESSING | Attempt 1 fails |
| 0.095 | Listener#0-1 | PAYMENT_FAILED_RETRY | Re-queued |
| 2.110 | Listener#0-1 | PAYMENT_PROCESSING | Attempt 2 fails (after 2s backoff) |
| 2.116 | Listener#0-1 | PAYMENT_FAILED_RETRY | Re-queued |
| 4.135 | Listener#0-1 | PAYMENT_PROCESSING | Attempt 3 fails (after 2s backoff) |
| 4.142 | Listener#0-1 | PAYMENT_FAILED_MAX_RETRIES | Sent to DLQ |

**Total processing time**: ~4.1 seconds (includes retry delays)

**Note**: No EMAIL_SENT logs because payment never reached PAID status.

---

## 🔀 Scenario 3: Partial Retries → Success on Attempt 2

### Input
```bash
curl -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"pedidoId":"RETRY-SUCCESS-001","monto":200.00,"clienteEmail":"retry@mail.com"}'
```

### Complete Log Output
```
[2025-11-13 14:27:30.500] [http-nio-8080-exec-3] [ORDER_RECEIVED] PedidoId=RETRY-SUCCESS-001 | Received order for retry@mail.com - Amount: $200.0
[2025-11-13 14:27:30.512] [http-nio-8080-exec-3] [ORDER_PUBLISHED] PedidoId=RETRY-SUCCESS-001 | Publishing to exchange: orders.exchange
[2025-11-13 14:27:30.518] [http-nio-8080-exec-3] [ORDER_SENT_TO_PAYMENT] PedidoId=RETRY-SUCCESS-001 | Message sent to payment processing

[2025-11-13 14:27:30.601] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_PROCESSING] PedidoId=RETRY-SUCCESS-001 | Attempt 1/3 - Amount: $200.0
[2025-11-13 14:27:30.607] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_FAILED_RETRY] PedidoId=RETRY-SUCCESS-001 | Payment failed - Retry 1/3

[2025-11-13 14:27:32.625] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_PROCESSING] PedidoId=RETRY-SUCCESS-001 | Attempt 2/3 - Amount: $200.0
[2025-11-13 14:27:32.630] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_SUCCESS] PedidoId=RETRY-SUCCESS-001 | Payment processed successfully - Status: PAID
[2025-11-13 14:27:32.635] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_FORWARDED_TO_EMAIL] PedidoId=RETRY-SUCCESS-001 | Order sent to email queue
[2025-11-13 14:27:32.720] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-2] [EMAIL_RECEIVED] PedidoId=RETRY-SUCCESS-001 | Email task received - Status: PAID
[2025-11-13 14:27:32.725] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-2] [EMAIL_SENDING] PedidoId=RETRY-SUCCESS-001 | Sending confirmation email to: retry@mail.com
[2025-11-13 14:27:33.230] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-2] [EMAIL_SENT] PedidoId=RETRY-SUCCESS-001 | ✓ Confirmation email sent successfully to retry@mail.com
```

**Key observation**: Payment failed once, succeeded on retry #2, then email sent.

---

## 🚦 Scenario 4: Concurrent Orders (Multiple Threads)

### Input
```bash
# Send 3 orders simultaneously
curl -X POST http://localhost:8080/orders -H 'Content-Type: application/json' -d '{"pedidoId":"CONCURRENT-001","monto":50,"clienteEmail":"c1@mail.com"}' &
curl -X POST http://localhost:8080/orders -H 'Content-Type: application/json' -d '{"pedidoId":"CONCURRENT-002","monto":60,"clienteEmail":"c2@mail.com"}' &
curl -X POST http://localhost:8080/orders -H 'Content-Type: application/json' -d '{"pedidoId":"CONCURRENT-003","monto":70,"clienteEmail":"c3@mail.com"}' &
```

### Interleaved Log Output
```
[2025-11-13 14:30:00.100] [http-nio-8080-exec-4] [ORDER_RECEIVED] PedidoId=CONCURRENT-001 | Received order for c1@mail.com - Amount: $50.0
[2025-11-13 14:30:00.105] [http-nio-8080-exec-5] [ORDER_RECEIVED] PedidoId=CONCURRENT-002 | Received order for c2@mail.com - Amount: $60.0
[2025-11-13 14:30:00.110] [http-nio-8080-exec-6] [ORDER_RECEIVED] PedidoId=CONCURRENT-003 | Received order for c3@mail.com - Amount: $70.0
[2025-11-13 14:30:00.120] [http-nio-8080-exec-4] [ORDER_PUBLISHED] PedidoId=CONCURRENT-001 | Publishing to exchange: orders.exchange
[2025-11-13 14:30:00.125] [http-nio-8080-exec-5] [ORDER_PUBLISHED] PedidoId=CONCURRENT-002 | Publishing to exchange: orders.exchange
[2025-11-13 14:30:00.130] [http-nio-8080-exec-6] [ORDER_PUBLISHED] PedidoId=CONCURRENT-003 | Publishing to exchange: orders.exchange
[2025-11-13 14:30:00.201] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_PROCESSING] PedidoId=CONCURRENT-001 | Attempt 1/3 - Amount: $50.0
[2025-11-13 14:30:00.203] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-2] [PAYMENT_PROCESSING] PedidoId=CONCURRENT-002 | Attempt 1/3 - Amount: $60.0
[2025-11-13 14:30:00.205] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-3] [PAYMENT_PROCESSING] PedidoId=CONCURRENT-003 | Attempt 1/3 - Amount: $70.0
[2025-11-13 14:30:00.210] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_SUCCESS] PedidoId=CONCURRENT-001 | Payment processed successfully - Status: PAID
[2025-11-13 14:30:00.212] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-2] [PAYMENT_FAILED_RETRY] PedidoId=CONCURRENT-002 | Payment failed - Retry 1/3
[2025-11-13 14:30:00.214] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-3] [PAYMENT_SUCCESS] PedidoId=CONCURRENT-003 | Payment processed successfully - Status: PAID
...
```

**Key observations**:
- ✅ Different **exec threads** (exec-4, exec-5, exec-6) handle HTTP requests simultaneously
- ✅ Different **listener threads** (#0-1, #0-2, #0-3) process payments in parallel
- ✅ **pedidoId** clearly identifies each order in interleaved logs

---

## 🔍 Thread Patterns

### HTTP Request Threads
```
http-nio-8080-exec-[N]
```
- Tomcat thread pool for handling HTTP requests
- Non-blocking: returns 202 Accepted immediately
- Typical lifetime: <50ms per request

### RabbitMQ Listener Threads
```
org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-[N]
```
- Spring AMQP listener container threads
- Long-lived: processes messages from queues
- One thread per concurrent message consumer

---

## 📈 Performance Metrics from Logs

### Latency Breakdown (Successful Flow)

| Stage | Duration | Thread Type |
|-------|----------|-------------|
| HTTP request handling | 15-30ms | exec |
| Message publishing | 10-20ms | exec |
| Payment processing | 5-15ms | listener |
| Email sending | 500-550ms | listener |
| **Total end-to-end** | **~600ms** | - |

### Latency with Retries (3 failures)

| Stage | Duration | Thread Type |
|-------|----------|-------------|
| Attempt 1 | 5ms | listener |
| Backoff 1 | 2000ms | - |
| Attempt 2 | 5ms | listener |
| Backoff 2 | 2000ms | - |
| Attempt 3 | 5ms | listener |
| Send to DLQ | 10ms | listener |
| **Total** | **~4030ms** | - |

---

## 🎯 Log Validation Checklist

Use this checklist when reviewing logs:

### For Every Order
- [ ] `ORDER_RECEIVED` log exists with correct pedidoId
- [ ] `ORDER_PUBLISHED` log follows immediately
- [ ] Timestamp format is `yyyy-MM-dd HH:mm:ss.SSS`
- [ ] Thread name is present in every log line

### For Payment Processing
- [ ] `PAYMENT_PROCESSING` shows attempt number (1/3, 2/3, or 3/3)
- [ ] Retry logs show incrementing retry count
- [ ] Max retries leads to `PAYMENT_FAILED_MAX_RETRIES`
- [ ] Success leads to `PAYMENT_SUCCESS` with `Status: PAID`

### For Email Processing
- [ ] `EMAIL_RECEIVED` only appears for PAID orders
- [ ] `EMAIL_SENDING` precedes `EMAIL_SENT`
- [ ] `EMAIL_SENT` has ✓ checkmark
- [ ] No email logs for FAILED orders

### For Concurrency
- [ ] Different orders use different thread IDs
- [ ] Logs are interleaved but pedidoId keeps them traceable
- [ ] No deadlocks or stuck threads

---

## 🐛 Error Log Examples

### RabbitMQ Connection Failure
```
[2025-11-13 14:35:00.000] [main] [ERROR] | Connection refused: localhost:5672
java.net.ConnectException: Connection refused: no further information
```
**Fix**: Ensure RabbitMQ is running (`docker ps`)

### Port Already in Use
```
[2025-11-13 14:36:00.000] [main] [ERROR] | Port 8080 was already in use
org.springframework.boot.web.server.PortInUseException: Port 8080 is already in use
```
**Fix**: Change port in `application.yml` or kill process on 8080

### JSON Parsing Error
```
[2025-11-13 14:37:00.000] [http-nio-8080-exec-1] [ERROR] | JSON parse error: Unrecognized field "invalid"
```
**Fix**: Validate JSON structure matches `OrderMessage` model

---

## 📊 RabbitMQ Management UI Correlation

When you see this in logs:
```
[PAYMENT_FAILED_MAX_RETRIES] PedidoId=FAIL-001 | Sending to DLQ
```

You should see in RabbitMQ UI (http://localhost:15672):
- **Queue**: `orders.dlq`
- **Ready messages**: +1
- **Message payload** (click "Get messages"):
  ```json
  {
    "pedidoId": "FAIL-001",
    "paymentStatus": "FAILED",
    "retryCount": 3
  }
  ```

---

## 🎓 Advanced: Custom Log Filtering

### PowerShell: Filter by PedidoId
```powershell
# View logs for specific order
Get-Content application.log | Select-String "PedidoId=TEST-001"
```

### PowerShell: Filter by Step
```powershell
# View only payment processing logs
Get-Content application.log | Select-String "PAYMENT_"
```

### PowerShell: Count successes vs failures
```powershell
$success = (Get-Content application.log | Select-String "PAYMENT_SUCCESS").Count
$failed = (Get-Content application.log | Select-String "PAYMENT_FAILED_MAX_RETRIES").Count
Write-Host "Success: $success | Failed: $failed | Ratio: $($success/($success+$failed)*100)%"
```

---

**Last Updated**: November 2025  
**Log Format Version**: 1.0.0
