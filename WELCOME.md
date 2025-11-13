# 🚴 BikeStore Async v1.0

> **Production-ready async messaging POC** | RabbitMQ + Spring Boot + Java 17

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-ff6600.svg)](https://www.rabbitmq.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 🎯 What Is This?

**BikeStore Async** is a complete **Proof of Concept (POC)** demonstrating:

✅ **Asynchronous order processing** with RabbitMQ  
✅ **Payment simulation** with automatic retries (3x)  
✅ **Dead Letter Queue** for failed transactions  
✅ **Conditional email notifications** (only for successful payments)  
✅ **Structured logging** with full traceability  
✅ **Production-ready patterns** (Retry, DLQ, Producer-Consumer)

---

## ⚡ Quick Start (3 Minutes)

```powershell
# 1. Start RabbitMQ
docker compose up -d

# 2. Run Application (in IntelliJ IDEA)
# Open Application.java → Click Run ▶️

# 3. Test API
curl -X POST http://localhost:8080/orders `
  -H "Content-Type: application/json" `
  -d '{\"pedidoId\":\"TEST-001\",\"monto\":120.50,\"clienteEmail\":\"test@mail.com\"}'
```

**✓ Expected**: `Order accepted: TEST-001`

→ **[Full Quick Start Guide](QUICKSTART.md)**

---

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| **[INDEX.md](INDEX.md)** | 📚 Documentation hub - Start here! |
| **[QUICKSTART.md](QUICKSTART.md)** | ⚡ 3-minute rapid deployment |
| **[GUIA_INTELLIJ.md](GUIA_INTELLIJ.md)** | 📖 Step-by-step IntelliJ guide (10 steps) |
| **[ARQUITECTURA.md](ARQUITECTURA.md)** | 🏗️ Architecture deep dive |
| **[TEST_COMMANDS.md](TEST_COMMANDS.md)** | 🧪 Testing commands reference |
| **[LOG_EXAMPLES.md](LOG_EXAMPLES.md)** | 📊 Real log outputs |

**→ [Complete Documentation Index](INDEX.md)**

---

## 🏗️ Architecture

```
HTTP POST /orders → OrderController
       ↓
   OrderProducer → RabbitMQ Exchange
                      ↓
              ┌───────┴───────┐
              ↓               ↓
        PaymentWorker    EmailWorker
        (3 retries)      (PAID only)
              ↓               ↓
        ✓ Success       ✓ Email Sent
        ✗ Failed → DLQ
```

### Components

- **OrderController**: REST API (`POST /orders`)
- **OrderProducer**: Publishes messages to RabbitMQ
- **PaymentWorker**: Processes payments with retry logic
- **EmailWorker**: Sends emails for successful payments
- **Dead Letter Queue**: Stores failed messages

→ **[Full Architecture Documentation](ARQUITECTURA.md)**

---

## 🎓 Features

### Functional
- ✅ Async order processing
- ✅ Payment simulation (50% failure rate)
- ✅ Automatic retries (3 attempts)
- ✅ Dead Letter Queue for failures
- ✅ Conditional email notifications
- ✅ Structured logging (pedidoId + timestamp + thread)

### Technical
- ✅ Java 17 (LTS)
- ✅ Spring Boot 3.2.0 (Web + AMQP)
- ✅ RabbitMQ 3.12 (Docker)
- ✅ Jackson JSON serialization
- ✅ Lombok for clean code
- ✅ Maven build system

### Patterns
- ✅ Producer-Consumer
- ✅ Retry with exponential backoff
- ✅ Dead Letter Queue
- ✅ Conditional processing
- ✅ Structured logging

---

## 🧪 Testing

### Quick Test
```powershell
# Send single order
curl -X POST http://localhost:8080/orders `
  -H "Content-Type: application/json" `
  -d '{\"pedidoId\":\"QUICK-001\",\"monto\":99.99,\"clienteEmail\":\"quick@test.com\"}'
```

### Bulk Test
```powershell
# Send 10 orders
.\test-orders.ps1 -Count 10
```

### Monitor
- **Logs**: Check IntelliJ console
- **RabbitMQ UI**: http://localhost:15672 (admin/admin123)

→ **[Complete Test Guide](TEST_COMMANDS.md)**

---

## 📊 Project Stats

| Metric | Value |
|--------|-------|
| **Lines of Code** | ~385 LOC |
| **Java Files** | 9 |
| **Documentation** | 9 MD files (~80 pages) |
| **Dependencies** | 4 (minimal) |
| **Test Scripts** | 2 PowerShell |

---

## 🛠️ Requirements

- ✅ Java 17+
- ✅ Maven 3.6+
- ✅ Docker Desktop
- ✅ IntelliJ IDEA (recommended) or VS Code

---

## 📂 Project Structure

```
ejercicioArquitectura/
├── 📚 Documentation (9 MD files)
├── 🐳 docker-compose.yml
├── 📦 pom.xml
├── 🧪 Scripts (setup.ps1, test-orders.ps1)
└── 💻 src/main/java/com/bikestore/
    ├── Application.java
    ├── config/RabbitConfig.java
    ├── model/OrderMessage.java
    ├── producer/ (Controller + Producer)
    ├── consumer/ (PaymentWorker + EmailWorker)
    └── util/LoggingUtil.java
```

→ **[Detailed Structure](PROJECT_STRUCTURE.md)**

---

## 🎯 Use Cases

### For Students
- Learn async messaging patterns
- Understand event-driven architecture
- Practice with RabbitMQ and Spring Boot

### For Developers
- Reference implementation for production patterns
- Template for microservices communication
- Foundation for distributed systems

### For Architects
- Evaluate async messaging solutions
- Prototype event-driven designs
- Demonstrate retry and DLQ patterns

---

## 🚀 Getting Started

### Choose Your Path

1. **⚡ Fast Track** (3 min): [QUICKSTART.md](QUICKSTART.md)
2. **📖 Guided Setup** (15 min): [GUIA_INTELLIJ.md](GUIA_INTELLIJ.md)
3. **🏗️ Architecture First** (20 min): [ARQUITECTURA.md](ARQUITECTURA.md)

**Not sure?** → Start with **[INDEX.md](INDEX.md)**

---

## 📞 Support

### Documentation
- 📚 [Full Documentation Index](INDEX.md)
- ❓ [FAQ in README](README.md)
- 🐛 [Troubleshooting Guide](GUIA_INTELLIJ.md#-troubleshooting)

### Common Issues
- **Connection refused**: Check RabbitMQ is running (`docker ps`)
- **Port 8080 in use**: Change port in `application.yml`
- **Build fails**: Verify Java 17 is configured

---

## 🔮 Roadmap

### v1.0.0 (Current)
- ✅ Complete POC implementation
- ✅ Full documentation suite
- ✅ Test scripts and examples

### v1.1.0 (Planned)
- [ ] Database persistence
- [ ] Spring Security integration
- [ ] Comprehensive tests (JUnit + Testcontainers)

### v2.0.0 (Future)
- [ ] Observability (Prometheus + Grafana)
- [ ] Circuit breaker (Resilience4j)
- [ ] Kubernetes deployment

---

## 📄 License

MIT License - Free for educational and commercial use

See [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Spring Boot** team for excellent AMQP integration
- **RabbitMQ** team for robust messaging broker
- **IntelliJ IDEA** for best-in-class development environment

---

## 🎓 Learn More

### External Resources
- [Spring AMQP Documentation](https://docs.spring.io/spring-amqp/reference/)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
- [Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/)

### Related Topics
- Event-driven architecture
- Microservices communication
- Distributed systems
- Message brokers comparison

---

## ✨ Why This POC?

✅ **Minimalist**: Only essential code, no bloat  
✅ **Professional**: Production-ready patterns  
✅ **Complete**: Full documentation + tests  
✅ **Reproducible**: Works out-of-the-box  
✅ **Educational**: Learn by example  

---

## 🎯 Next Steps

1. **[Read QUICKSTART.md](QUICKSTART.md)** → Run in 3 minutes
2. **[Follow GUIA_INTELLIJ.md](GUIA_INTELLIJ.md)** → Understand setup
3. **[Study ARQUITECTURA.md](ARQUITECTURA.md)** → Learn patterns
4. **[Run TEST_COMMANDS.md](TEST_COMMANDS.md)** → Validate behavior
5. **Modify and experiment** → Make it yours!

---

**Version**: 1.0.0  
**Release Date**: November 13, 2025  
**Status**: ✅ Production-Ready Patterns | 🎓 Educational Purpose

---

**🚀 Ready to start? → [QUICKSTART.md](QUICKSTART.md)**
