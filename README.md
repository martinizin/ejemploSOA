# BikeStore Async v1.0 - POC Asíncrona

> **Arquitectura asíncrona con RabbitMQ | Java 17 + Spring Boot**


---
# DEMO DE FUNCIONAMIENTO
## Ejecutamos el comando docker compose -d e inicializamos RabbitMQ, luego nos logeamos con las credenciales que hayamos colocado
<img width="1917" height="912" alt="image" src="https://github.com/user-attachments/assets/7b16708b-21b1-4caf-ba27-d7cc1094d4fd" />

## Inicializamos el servicio desarrollado (En mi caso desde Intelligent Idea)
<img width="1919" height="1015" alt="image" src="https://github.com/user-attachments/assets/50a04c55-df0d-4025-a8ff-1c4419f6a42c" />

## Preparé una colección para probar las API en postman
<img width="1919" height="1022" alt="image" src="https://github.com/user-attachments/assets/8d3cc96f-0baa-4976-9b74-e046e756e461" />

## Prueba 01 : Health Check

<img width="1221" height="902" alt="image" src="https://github.com/user-attachments/assets/8453dc89-220e-43d6-959a-9540a4a09682" />

## Prueba 02: Creación de orden simple
<img width="1224" height="876" alt="image" src="https://github.com/user-attachments/assets/81bb8a3e-248e-40bb-ae81-a79c2ddb989c" />

# Prueba 03: Orden de monto Alto
<img width="1218" height="907" alt="image" src="https://github.com/user-attachments/assets/9765bc07-c1e7-48f5-9971-1c31ac087c93" />

## Prueba 04: Múltiples variables:
<img width="1211" height="918" alt="image" src="https://github.com/user-attachments/assets/c6bf6a02-8d9a-4e68-af9f-e29789cc4f41" />

## Flujo Completo: 1. Enviar un pedido
<img width="1235" height="914" alt="image" src="https://github.com/user-attachments/assets/67dfb8a8-9ec3-4914-9e22-dacc9d752dd3" />

## Escenario Exitoso
<img width="1755" height="550" alt="image" src="https://github.com/user-attachments/assets/b19ea298-56a6-4ea9-bd94-db2a8896e52b" />

### Validaciones
- Cada línea tiene pedidoId (TEST-FLOW-001)
- Cada línea tiene timestamp (12:30:45.123)
- Cada línea tiene thread (http-nio-8080-exec-1, RabbitListenerEndpointContainer#0-1)
- El PaymentWorker procesó exitosamente (50% de probabilidad)
- El EmailWorker envió email porque paymentStatus=PAID
### Validación RabbitMQ
<img width="974" height="324" alt="image" src="https://github.com/user-attachments/assets/5a3ae061-0837-4c3a-8d19-5da6cd11bf75" />
<img width="1396" height="380" alt="image" src="https://github.com/user-attachments/assets/b6de9717-75d4-4255-b8f7-65813a92292c" />
<img width="1919" height="905" alt="image" src="https://github.com/user-attachments/assets/46ccf44d-d731-44ce-888a-70263829d0e0" />

- payments.queue: 0 mensajes (procesado inmediatamente)
- emails.queue: 0 mensajes (email enviado)
- orders.dlq: 0 mensajes (no hubo fallo)

# Ahora desde POSTMAN enviaré 10 veces la misma petición haber cómo responde el servidor
<img width="1279" height="902" alt="image" src="https://github.com/user-attachments/assets/9f22d7f1-f0f9-4589-94d2-4806b5c75c96" />


## Resultados

<img width="1764" height="693" alt="image" src="https://github.com/user-attachments/assets/03b830b6-14da-40d0-9734-22304b961120" />

<img width="1251" height="273" alt="image" src="https://github.com/user-attachments/assets/9d3798a9-1b98-4a34-93ba-7cd5e4f65bf1" />

## 🏗 Arquitectura

### Flujo de Mensajería

```
┌──────────────┐     ┌──────────────────┐     ┌────────────────┐
│ POST /orders │────>│ orders.exchange  │────>│ payments.queue │
└──────────────┘     └──────────────────┘     └────────────────┘
                              │                        │
                              │                        ▼
                              │              ┌──────────────────┐
                              │              │  PaymentWorker   │
                              │              │  (3 reintentos)  │
                              │              └──────────────────┘
                              │                   │        │
                              │                ✓ PAID   ✗ FAILED
                              │                   │        │
                              │                   ▼        ▼
                       ┌──────────────┐    ┌──────────────────┐
                       │ emails.queue │    │   orders.dlq     │
                       └──────────────┘    │ (Dead Letter Q)  │
                              │            └──────────────────┘
                              ▼
                       ┌──────────────┐
                       │ EmailWorker  │
                       │ (solo PAID)  │
                       └──────────────┘
```

### Componentes

| Componente | Descripción |
|------------|-------------|
| **OrderController** | API REST `/orders` que recibe pedidos |
| **OrderProducer** | Publica mensajes JSON al exchange |
| **PaymentWorker** | Procesa pagos con 50% prob. fallo y 3 reintentos |
| **EmailWorker** | Envía emails **solo si** `paymentStatus=PAID` |
| **RabbitMQ** | Broker de mensajería (Docker) |

### Colas y Exchanges

- **Exchange**: `orders.exchange` (direct)
- **Colas**:
  - `payments.queue` → Procesamiento de pagos
  - `emails.queue` → Envío de emails
  - `orders.dlq` → Dead Letter Queue (fallos permanentes)

---

## 🛠 Requisitos

- **Java 17+** (JDK instalado)
- **Maven 3.6+** o superior
- **Docker Desktop** (para RabbitMQ)
- **IntelliJ IDEA** (Community o Ultimate)
- **curl** o **Postman** (para pruebas de API)

---



## 🐛 Troubleshooting

### Error: "Cannot connect to RabbitMQ"

**Síntoma**: 
```
java.net.ConnectException: Connection refused: no further information
```

**Solución**:
1. Verifica Docker:
   ```powershell
   docker ps
   ```
2. Si RabbitMQ no está corriendo:
   ```powershell
   docker compose up -d
   ```
3. Espera 10-15 segundos para que RabbitMQ inicie completamente

### Error: "Port 8080 already in use"

**Solución**:
1. Cambia puerto en `application.yml`:
   ```yaml
   server:
     port: 8081
   ```
2. O mata proceso en puerto 8080:
   ```powershell
   netstat -ano | findstr :8080
   taskkill /PID <PID> /F
   ```

### No se ven logs en consola

**Solución**:
1. Verifica nivel de log en `application.yml`:
   ```yaml
   logging:
     level:
       com.bikestore: INFO
   ```

## 📚 REFERENCIAS

- [Spring AMQP Documentation](https://docs.spring.io/spring-amqp/reference/)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
- [Dead Letter Exchanges](https://www.rabbitmq.com/dlx.html)

---

**Versión**: 1.0.0  
**Autor**: Martin Jimenez 
