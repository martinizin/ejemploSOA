# 📚 BikeStore Async v1.0 - Documentation Index

> **Complete documentation guide** | Navigate to the right resource

---

## 🚀 Getting Started (Choose Your Path)

### ⚡ I want to run it NOW (3 minutes)
**→ Read**: [`QUICKSTART.md`](QUICKSTART.md)
- 3-step launch
- Minimal explanation
- Quick validation tests

### 📖 I want step-by-step instructions (15 minutes)
**→ Read**: [`GUIA_INTELLIJ.md`](GUIA_INTELLIJ.md)
- 10 detailed steps
- IntelliJ IDEA setup
- Troubleshooting included
- Screenshots descriptions

### 🔍 I want to understand the architecture (20 minutes)
**→ Read**: [`ARQUITECTURA.md`](ARQUITECTURA.md)
- Component diagrams
- Message flow details
- Design patterns explained
- Extension points

---

## 📂 Documentation Files

| File | Purpose | When to Use |
|------|---------|-------------|
| **[README.md](README.md)** | Main documentation + FAQ | General overview, requirements |
| **[QUICKSTART.md](QUICKSTART.md)** | 3-minute quick start | Want to run immediately |
| **[GUIA_INTELLIJ.md](GUIA_INTELLIJ.md)** | Step-by-step IntelliJ guide | First time setup |
| **[ARQUITECTURA.md](ARQUITECTURA.md)** | Architecture deep dive | Understanding design decisions |
| **[RESUMEN_EJECUTIVO.md](RESUMEN_EJECUTIVO.md)** | Executive summary | High-level overview |
| **[PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)** | Code structure explained | Understanding file organization |
| **[TEST_COMMANDS.md](TEST_COMMANDS.md)** | Testing commands reference | Running tests |
| **[LOG_EXAMPLES.md](LOG_EXAMPLES.md)** | Real log outputs | Validating behavior |

---

## 🎯 By Task

### I Need To...

#### ✅ Set Up the Project
1. Read [Prerequisites](#prerequisites)
2. Follow [QUICKSTART.md](QUICKSTART.md) or [GUIA_INTELLIJ.md](GUIA_INTELLIJ.md)
3. Run verification tests from [TEST_COMMANDS.md](TEST_COMMANDS.md)

#### 🧪 Test the Application
1. Start with [TEST_COMMANDS.md](TEST_COMMANDS.md)
2. Compare logs with [LOG_EXAMPLES.md](LOG_EXAMPLES.md)
3. Monitor RabbitMQ UI (instructions in [QUICKSTART.md](QUICKSTART.md))

#### 🔧 Modify the Code
1. Understand architecture in [ARQUITECTURA.md](ARQUITECTURA.md)
2. Review code structure in [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)
3. Make changes and test

#### 🐛 Troubleshoot Issues
1. Check [GUIA_INTELLIJ.md - Troubleshooting section](GUIA_INTELLIJ.md#-troubleshooting)
2. Compare your logs with [LOG_EXAMPLES.md](LOG_EXAMPLES.md)
3. Verify setup in [README.md - FAQ](README.md#-troubleshooting)

#### 📊 Understand Logs
1. Read [LOG_EXAMPLES.md](LOG_EXAMPLES.md)
2. Check log format in [ARQUITECTURA.md](ARQUITECTURA.md)
3. Use filtering commands from [LOG_EXAMPLES.md - Advanced section](LOG_EXAMPLES.md)

#### 🎓 Learn Async Patterns
1. Read [ARQUITECTURA.md - Design Patterns](ARQUITECTURA.md)
2. Study [PROJECT_STRUCTURE.md - Component Dependencies](PROJECT_STRUCTURE.md)
3. Review code in `src/main/java/com/bikestore/`

---

## 📋 Prerequisites

### Required Software
- ✅ **Java 17+** (Check: `java -version`)
- ✅ **Maven 3.6+** (Check: `mvn -version`)
- ✅ **Docker Desktop** (Check: `docker --version`)
- ✅ **IntelliJ IDEA** (Community or Ultimate)

### Optional Tools
- 📮 **Postman** (Import `BikeStore_Async.postman_collection.json`)
- 🐚 **curl** (For command-line testing)
- 🔧 **VS Code** (Alternative IDE, see [README.md](README.md))

---

## 🗂️ Project Structure at a Glance

```
ejercicioArquitectura/
│
├── 📋 Documentation (You are here!)
│   ├── INDEX.md                    ⬅️ This file
│   ├── README.md                   Main docs
│   ├── QUICKSTART.md               3-min start
│   ├── GUIA_INTELLIJ.md            Step-by-step
│   ├── ARQUITECTURA.md             Architecture
│   ├── RESUMEN_EJECUTIVO.md        Summary
│   ├── PROJECT_STRUCTURE.md        Code structure
│   ├── TEST_COMMANDS.md            Test commands
│   └── LOG_EXAMPLES.md             Log samples
│
├── 🐳 Infrastructure
│   └── docker-compose.yml          RabbitMQ setup
│
├── 🔧 Configuration
│   ├── pom.xml                     Maven deps
│   └── src/main/resources/
│       └── application.yml         Spring config
│
├── 💻 Source Code
│   └── src/main/java/com/bikestore/
│       ├── Application.java        Main class
│       ├── config/                 RabbitMQ config
│       ├── model/                  Message contract
│       ├── producer/               API + Publisher
│       ├── consumer/               Workers
│       └── util/                   Logging
│
└── 🧪 Testing Tools
    ├── setup.ps1                   Automated setup
    ├── test-orders.ps1             Bulk testing
    └── BikeStore_Async.postman_collection.json
```

---

## 🎓 Learning Path

### Beginner (Never used async messaging)
1. **Understand the basics**: Read [README.md](README.md) introduction
2. **Run the project**: Follow [QUICKSTART.md](QUICKSTART.md)
3. **See it work**: Send test orders from [TEST_COMMANDS.md](TEST_COMMANDS.md)
4. **Watch the logs**: Compare with [LOG_EXAMPLES.md](LOG_EXAMPLES.md)

### Intermediate (Know async concepts)
1. **Architecture overview**: Read [ARQUITECTURA.md](ARQUITECTURA.md)
2. **Code structure**: Study [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)
3. **Run and modify**: Change retry count, failure rate
4. **Test scenarios**: Try all test cases from [TEST_COMMANDS.md](TEST_COMMANDS.md)

### Advanced (Production experience)
1. **Deep dive**: Read all architecture documentation
2. **Code review**: Analyze `src/main/java/com/bikestore/`
3. **Extend**: Add persistence, observability, circuit breaker
4. **Compare**: Evaluate vs Kafka, SNS/SQS, Azure Service Bus

---

## 🔗 Quick Links

### Documentation
- 📄 [Main README](README.md)
- ⚡ [Quick Start](QUICKSTART.md)
- 📖 [IntelliJ Guide](GUIA_INTELLIJ.md)
- 🏗️ [Architecture](ARQUITECTURA.md)

### Testing
- 🧪 [Test Commands](TEST_COMMANDS.md)
- 📊 [Log Examples](LOG_EXAMPLES.md)
- 📮 [Postman Collection](BikeStore_Async.postman_collection.json)

### Configuration
- 🐳 [Docker Compose](docker-compose.yml)
- ⚙️ [Spring Config](src/main/resources/application.yml)
- 📦 [Maven POM](pom.xml)

### Scripts
- 🚀 [Setup Script](setup.ps1)
- 🧪 [Test Script](test-orders.ps1)

---

## 📞 Common Questions

### "Where do I start?"
→ Open [QUICKSTART.md](QUICKSTART.md) for immediate action

### "How does it work?"
→ Read [ARQUITECTURA.md](ARQUITECTURA.md) for detailed explanations

### "Something's not working!"
→ Check [GUIA_INTELLIJ.md - Troubleshooting](GUIA_INTELLIJ.md#-troubleshooting)

### "What are these logs?"
→ Compare with [LOG_EXAMPLES.md](LOG_EXAMPLES.md)

### "How do I test it?"
→ Use commands from [TEST_COMMANDS.md](TEST_COMMANDS.md)

### "Can I use VS Code?"
→ Yes! See [README.md - VS Code section](README.md#-nota-para-visual-studio)

### "What does this file do?"
→ Check [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)

---

## 🎯 Validation Checklist

Use this to verify your setup:

- [ ] RabbitMQ running (`docker ps`)
- [ ] Application started (IntelliJ/Maven)
- [ ] Health endpoint works (`curl http://localhost:8080/orders/health`)
- [ ] Order accepted (`curl POST /orders`)
- [ ] Logs show pedidoId, timestamp, thread
- [ ] RabbitMQ UI accessible (http://localhost:15672)
- [ ] Messages flow through queues
- [ ] DLQ receives failed messages
- [ ] Emails sent only for PAID orders

---

## 🏆 What This POC Demonstrates

| Concept | Implementation | Where to Learn |
|---------|----------------|----------------|
| **Async Messaging** | RabbitMQ + Spring AMQP | [ARQUITECTURA.md](ARQUITECTURA.md) |
| **Retry Pattern** | 3 attempts with backoff | [PaymentWorker.java](src/main/java/com/bikestore/consumer/PaymentWorker.java) |
| **Dead Letter Queue** | Failed messages preserved | [RabbitConfig.java](src/main/java/com/bikestore/config/RabbitConfig.java) |
| **Conditional Processing** | Email only if PAID | [EmailWorker.java](src/main/java/com/bikestore/consumer/EmailWorker.java) |
| **Observability** | Structured logging | [LoggingUtil.java](src/main/java/com/bikestore/util/LoggingUtil.java) |
| **Decoupling** | Producer-Consumer pattern | [ARQUITECTURA.md](ARQUITECTURA.md) |

---

## 📚 External Resources

### Spring AMQP
- [Official Documentation](https://docs.spring.io/spring-amqp/reference/)
- [Spring Boot AMQP Guide](https://spring.io/guides/gs/messaging-rabbitmq/)

### RabbitMQ
- [Getting Started Tutorials](https://www.rabbitmq.com/getstarted.html)
- [Dead Letter Exchanges](https://www.rabbitmq.com/dlx.html)
- [Management UI Guide](https://www.rabbitmq.com/management.html)

### Design Patterns
- [Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/)
- [Retry Pattern (Microsoft)](https://docs.microsoft.com/azure/architecture/patterns/retry)

---

## 🚀 Next Steps After Setup

### For Learning
1. ✅ Run all test scenarios
2. ✅ Monitor RabbitMQ UI during tests
3. ✅ Analyze logs for different scenarios
4. ✅ Modify code (change retry count, failure rate)
5. ✅ Study architecture documentation

### For Production Use
1. 🔒 Add authentication (Spring Security)
2. 💾 Add persistence (PostgreSQL/MongoDB)
3. 📊 Add observability (Prometheus + Grafana)
4. 🧪 Add comprehensive tests (Unit + Integration)
5. 🛡️ Add circuit breaker (Resilience4j)

---

## 📊 Documentation Stats

| Metric | Count |
|--------|-------|
| **Documentation files** | 8 MD files |
| **Total pages** | ~80 pages |
| **Code files** | 9 Java files |
| **Lines of code** | ~385 LOC |
| **Test scripts** | 2 PowerShell scripts |
| **Configuration files** | 3 (docker-compose, pom.xml, application.yml) |

---

## 🎓 Technologies Used

- **Language**: Java 17
- **Framework**: Spring Boot 3.2.0
- **Messaging**: RabbitMQ 3.12
- **Build Tool**: Maven 3.6+
- **Containerization**: Docker Compose
- **Serialization**: Jackson (JSON)
- **Code Generation**: Lombok

---

## ✅ Success Criteria

You'll know the POC is working when:

1. ✅ Application starts without errors
2. ✅ API accepts orders (HTTP 202)
3. ✅ Logs show complete flow (ORDER_RECEIVED → EMAIL_SENT)
4. ✅ Retries work (3 attempts visible in logs)
5. ✅ DLQ receives failed messages (visible in RabbitMQ UI)
6. ✅ Emails only sent for PAID orders
7. ✅ All logs include pedidoId, timestamp, thread

---

## 📧 Support

**Need help?**
1. Check troubleshooting in [GUIA_INTELLIJ.md](GUIA_INTELLIJ.md)
2. Review [LOG_EXAMPLES.md](LOG_EXAMPLES.md) for expected output
3. Validate configuration in [README.md](README.md)

**Found an issue?**
- Review all documentation files
- Check logs for error messages
- Verify prerequisites are met

---

**Version**: 1.0.0  
**Last Updated**: November 2025  
**License**: MIT (Educational Use)

---

**🎯 Ready to start? → Go to [QUICKSTART.md](QUICKSTART.md)**
