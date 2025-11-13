# Project Structure - BikeStore Async v1.0

```
ejercicioArquitectura/
│
├── 📋 Documentation
│   ├── README.md                        # Main documentation + quick start
│   ├── GUIA_INTELLIJ.md                 # Step-by-step guide (10 steps)
│   ├── ARQUITECTURA.md                  # Detailed architecture + diagrams
│   ├── RESUMEN_EJECUTIVO.md             # Executive summary
│   └── TEST_COMMANDS.md                 # Test commands reference
│
├── 🐳 Docker
│   └── docker-compose.yml               # RabbitMQ container setup
│
├── 🔧 Build Configuration
│   ├── pom.xml                          # Maven dependencies
│   └── .gitignore                       # Git ignore rules
│
├── 🧪 Testing Tools
│   ├── setup.ps1                        # Automated setup script
│   ├── test-orders.ps1                  # Bulk order testing script
│   └── BikeStore_Async.postman_collection.json  # Postman collection
│
└── 📦 Source Code
    └── src/
        ├── main/
        │   ├── java/com/bikestore/
        │   │   │
        │   │   ├── Application.java                    # 🚀 Main entry point
        │   │   │
        │   │   ├── config/
        │   │   │   └── RabbitConfig.java               # ⚙️ RabbitMQ setup
        │   │   │       • Defines exchanges
        │   │   │       • Creates queues (payments, emails, dlq)
        │   │   │       • Configures bindings
        │   │   │       • Sets up DLX (Dead Letter Exchange)
        │   │   │       • JSON message converter
        │   │   │
        │   │   ├── model/
        │   │   │   └── OrderMessage.java               # 📄 Message contract
        │   │   │       • pedidoId (String)
        │   │   │       • monto (Double)
        │   │   │       • clienteEmail (String)
        │   │   │       • paymentStatus (Enum: PENDING/PAID/FAILED)
        │   │   │       • createdAt (LocalDateTime)
        │   │   │       • retryCount (Integer)
        │   │   │
        │   │   ├── producer/
        │   │   │   ├── OrderController.java            # 🌐 REST API Controller
        │   │   │   │   • POST /orders - Receive orders
        │   │   │   │   • GET /orders/health - Health check
        │   │   │   │   • Generates UUID if not provided
        │   │   │   │
        │   │   │   └── OrderProducer.java              # 📤 Message Publisher
        │   │   │       • Converts OrderMessage to JSON
        │   │   │       • Publishes to RabbitMQ exchange
        │   │   │       • Logs publication
        │   │   │
        │   │   ├── consumer/
        │   │   │   ├── PaymentWorker.java              # 💳 Payment Processor
        │   │   │   │   • Listens to payments.queue
        │   │   │   │   • Simulates payment (50% failure)
        │   │   │   │   • Implements retry logic (3 attempts)
        │   │   │   │   • Sends to DLQ after max retries
        │   │   │   │   • Forwards PAID orders to emails.queue
        │   │   │   │
        │   │   │   └── EmailWorker.java                # 📧 Email Sender
        │   │   │       • Listens to emails.queue
        │   │   │       • Processes ONLY if paymentStatus=PAID
        │   │   │       • Simulates email sending (500ms delay)
        │   │   │       • Logs email confirmation
        │   │   │
        │   │   └── util/
        │   │       └── LoggingUtil.java                # 📝 Logging Helper
        │   │           • Consistent log format
        │   │           • Includes timestamp (milliseconds)
        │   │           • Includes thread name
        │   │           • Includes pedidoId in every log
        │   │
        │   └── resources/
        │       └── application.yml                     # ⚙️ Spring Boot Config
        │           • Server port: 8080
        │           • RabbitMQ connection settings
        │           • Retry configuration
        │           • Logging levels
        │
        └── test/
            └── (Unit tests would go here)
```

---

## File Summary

| File | LOC | Purpose |
|------|-----|---------|
| `Application.java` | ~20 | Spring Boot entry point |
| `RabbitConfig.java` | ~100 | Exchange, queues, bindings config |
| `OrderMessage.java` | ~40 | Data model with Lombok |
| `OrderController.java` | ~35 | REST endpoint handler |
| `OrderProducer.java` | ~30 | RabbitMQ publisher |
| `PaymentWorker.java` | ~70 | Payment processor with retries |
| `EmailWorker.java` | ~40 | Conditional email sender |
| `LoggingUtil.java` | ~25 | Centralized logging |
| `application.yml` | ~25 | Spring configuration |
| **Total** | **~385** | **Clean, minimal codebase** |

---

## Component Dependencies

```
┌─────────────────────────────────────────┐
│         Application.java                │
│         (Spring Boot Main)              │
└──────────────┬──────────────────────────┘
               │
      ┌────────┴────────┐
      │                 │
      ▼                 ▼
┌──────────────┐  ┌──────────────┐
│ OrderController│  │ RabbitConfig │
└─────┬────────┘  └──────┬───────┘
      │                  │
      ▼                  │
┌──────────────┐         │
│ OrderProducer│         │
└─────┬────────┘         │
      │                  │
      └──────┬───────────┘
             │
    ┌────────┴────────┐
    │   RabbitMQ      │
    │   (Docker)      │
    └────┬──────┬─────┘
         │      │
    ┌────▼──┐  ┌▼────────┐
    │Payment│  │Email    │
    │Worker │  │Worker   │
    └───────┘  └─────────┘
         │          │
         └──────┬───┘
                │
         ┌──────▼──────┐
         │ LoggingUtil │
         └─────────────┘
```

---

## Message Flow Through Files

### 1. Order Creation
```
HTTP Client
    │
    ▼
OrderController.java (@PostMapping /orders)
    │ - Receives OrderMessage
    │ - Validates & adds UUID
    │ - Logs ORDER_RECEIVED
    ▼
OrderProducer.java (publishOrder)
    │ - Converts to JSON (via Jackson)
    │ - Sends to orders.exchange
    │ - Logs ORDER_PUBLISHED
    ▼
RabbitMQ (orders.exchange)
```

### 2. Payment Processing
```
RabbitMQ (payments.queue)
    │
    ▼
PaymentWorker.java (@RabbitListener)
    │ - Consumes OrderMessage
    │ - Logs PAYMENT_PROCESSING
    │ - Simulates payment (random success/fail)
    │
    ├─ If SUCCESS:
    │   │ - Sets paymentStatus = PAID
    │   │ - Logs PAYMENT_SUCCESS
    │   │ - Sends to emails.queue
    │   └─ Logs PAYMENT_FORWARDED_TO_EMAIL
    │
    └─ If FAILURE:
        │ - Increments retryCount
        │
        ├─ If retryCount < 3:
        │   │ - Logs PAYMENT_FAILED_RETRY
        │   └─ Re-queues to payments.queue
        │
        └─ If retryCount >= 3:
            │ - Sets paymentStatus = FAILED
            │ - Logs PAYMENT_FAILED_MAX_RETRIES
            └─ Sends to orders.dlq
```

### 3. Email Sending
```
RabbitMQ (emails.queue)
    │
    ▼
EmailWorker.java (@RabbitListener)
    │ - Consumes OrderMessage
    │ - Logs EMAIL_RECEIVED
    │
    ├─ If paymentStatus == PAID:
    │   │ - Logs EMAIL_SENDING
    │   │ - Sleeps 500ms (simulate SMTP)
    │   └─ Logs EMAIL_SENT ✓
    │
    └─ Else:
        └─ Logs EMAIL_SKIPPED
```

---

## RabbitMQ Topology

```
orders.exchange (Direct)
│
├─ Binding: orders.created ──────> orders.queue (unused in this POC)
│
├─ Binding: payments.process ────> payments.queue
│                                   │
│                                   └─ DLX: orders.exchange
│                                      DLX Routing: orders.dead
│
├─ Binding: emails.send ──────────> emails.queue
│
└─ Binding: orders.dead ──────────> orders.dlq
```

---

## Configuration Files

### application.yml
```yaml
server.port: 8080
spring.rabbitmq:
  host: localhost
  port: 5672
  username: admin
  password: admin123
  listener.simple.retry:
    enabled: true
    initial-interval: 2000
    max-attempts: 3
    multiplier: 2.0
```

### docker-compose.yml
```yaml
rabbitmq:
  image: rabbitmq:3.12-management-alpine
  ports:
    - 5672:5672   # AMQP
    - 15672:15672 # Management UI
  environment:
    RABBITMQ_DEFAULT_USER: admin
    RABBITMQ_DEFAULT_PASS: admin123
```

### pom.xml (key dependencies)
```xml
<dependencies>
  <dependency>spring-boot-starter-web</dependency>
  <dependency>spring-boot-starter-amqp</dependency>
  <dependency>jackson-databind</dependency>
  <dependency>lombok</dependency>
</dependencies>
```

---

## Execution Flow

```
1. Start RabbitMQ
   └─ docker compose up -d

2. Start Spring Boot Application
   └─ Run Application.java (IntelliJ/Maven)
       │
       ├─ Loads application.yml
       ├─ Connects to RabbitMQ
       ├─ Creates exchanges/queues (RabbitConfig)
       ├─ Registers @RabbitListener methods
       └─ Starts Tomcat on port 8080

3. Send Order
   └─ curl POST http://localhost:8080/orders
       │
       ├─ OrderController receives JSON
       ├─ OrderProducer publishes to RabbitMQ
       ├─ PaymentWorker consumes from payments.queue
       │   │
       │   ├─ Success? → EmailWorker → ✓ Email sent
       │   └─ Failure? → Retry (x3) → DLQ
       │
       └─ All steps logged with pedidoId + timestamp + thread
```

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Direct Exchange** | Explicit routing, easy to debug |
| **JSON Serialization** | Jackson2JsonMessageConverter for automatic conversion |
| **Lombok** | Reduces boilerplate (@Data, @AllArgsConstructor) |
| **Manual Retry Logic** | Fine-grained control over retry behavior |
| **Centralized Logging** | LoggingUtil ensures consistent format |
| **Spring AMQP** | Simpler than pure RabbitMQ Java Client |
| **Docker Compose** | Easy local RabbitMQ setup |

---

## Extension Points

To extend this POC:

1. **Add Persistence**
   - Create `@Repository` layer
   - Save orders to PostgreSQL before publishing

2. **Add Observability**
   - Integrate Spring Boot Actuator
   - Add Micrometer metrics
   - Export to Prometheus/Grafana

3. **Add Testing**
   ```java
   @SpringBootTest
   @TestPropertySource(locations = "classpath:application-test.yml")
   class OrderControllerTest {
       @Autowired MockMvc mockMvc;
       // Test /orders endpoint
   }
   ```

4. **Add Circuit Breaker**
   ```java
   @CircuitBreaker(name = "payment", fallbackMethod = "paymentFallback")
   public void processPayment(OrderMessage order) { ... }
   ```

5. **Add Tracing**
   - Integrate Spring Cloud Sleuth
   - Export traces to Zipkin

---

**Last Updated**: November 2025  
**Project Status**: ✅ Complete & Functional
