# Arquitectura Detallada - BikeStore Async v1.0

## 🏗️ Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────────┐
│                       BikeStore Async v1.0                      │
│                     Spring Boot Application                     │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────┐
│   HTTP Client   │
│  (curl/Postman) │
└────────┬────────┘
         │ POST /orders
         ▼
┌─────────────────────────────────────┐
│      OrderController.java           │
│  - Recibe pedidos (HTTP POST)       │
│  - Genera UUID si no existe         │
│  - Valida entrada                   │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│      OrderProducer.java             │
│  - Convierte a JSON                 │
│  - Publica a RabbitMQ               │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────┐
│                  RabbitMQ Broker                    │
│  ┌────────────────────────────────────────────┐    │
│  │        orders.exchange (Direct)            │    │
│  └──┬───────────────┬──────────────────┬──────┘    │
│     │               │                  │            │
│     │ orders.       │ payments.        │ orders.    │
│     │ created       │ process          │ dead       │
│     ▼               ▼                  ▼            │
│  ┌────────┐   ┌──────────┐      ┌──────────┐      │
│  │orders. │   │payments. │      │orders.   │      │
│  │queue   │   │queue     │      │dlq       │      │
│  └────────┘   └────┬─────┘      └──────────┘      │
│                     │                               │
│                     │ emails.send                   │
│                     ▼                               │
│                ┌──────────┐                         │
│                │emails.   │                         │
│                │queue     │                         │
│                └──────────┘                         │
└─────────────────────────────────────────────────────┘
         │                           │
         ▼                           ▼
┌─────────────────────┐    ┌──────────────────────┐
│  PaymentWorker.java │    │  EmailWorker.java    │
│  - Simula pago      │    │  - Envía email solo  │
│  - 50% fallo        │    │    si PAID           │
│  - 3 reintentos     │    │  - Sleep 500ms       │
│  - Envía a DLQ      │    └──────────────────────┘
└─────────────────────┘
```

---

## 🔄 Flujo de Datos Detallado

### Escenario 1: Pago Exitoso (Happy Path)

```
Cliente                OrderController         OrderProducer          RabbitMQ            PaymentWorker        EmailWorker
   │                          │                      │                    │                      │                  │
   ├─POST /orders────────────>│                      │                    │                      │                  │
   │                          │                      │                    │                      │                  │
   │                          ├─publishOrder()──────>│                    │                      │                  │
   │                          │                      │                    │                      │                  │
   │                          │                      ├─convertAndSend()──>│                      │                  │
   │                          │                      │  (payments.queue)  │                      │                  │
   │<─202 Accepted────────────┤                      │                    │                      │                  │
   │                          │                      │                    │                      │                  │
   │                          │                      │                    ├─consume()───────────>│                  │
   │                          │                      │                    │                      │                  │
   │                          │                      │                    │                 [Simula Pago: SUCCESS] │
   │                          │                      │                    │                      │                  │
   │                          │                      │                    │<─send(emails.queue)──┤                  │
   │                          │                      │                    │                      │                  │
   │                          │                      │                    ├─────consume()────────┼─────────────────>│
   │                          │                      │                    │                      │             [Envía Email]
   │                          │                      │                    │                      │                  │
```

### Escenario 2: Fallo con Reintentos → DLQ

```
Cliente                OrderController         PaymentWorker          RabbitMQ (DLQ)
   │                          │                      │                      │
   ├─POST /orders────────────>│                      │                      │
   │                          │                      │                      │
   │                          ├─publish──────────────┼─────────────────────>│
   │<─202 Accepted────────────┤                      │                      │
   │                          │                      │                      │
   │                          │                 [Intento 1: FAIL]           │
   │                          │                      ├─re-queue────────────>│
   │                          │                      │                      │
   │                          │                 [Intento 2: FAIL]           │
   │                          │                      ├─re-queue────────────>│
   │                          │                      │                      │
   │                          │                 [Intento 3: FAIL]           │
   │                          │                      │                      │
   │                          │                      ├─send(orders.dlq)────>│
   │                          │                      │                      │
   │                          │                      │             [Mensaje en DLQ]
```

---

## 📊 Estructura de Mensaje (JSON)

### Mensaje Inicial (POST /orders)

```json
{
  "pedidoId": "550e8400-e29b-41d4-a716-446655440000",
  "monto": 120.50,
  "clienteEmail": "cliente@mail.com",
  "paymentStatus": "PENDING",
  "createdAt": "2025-11-13T10:30:15",
  "retryCount": 0
}
```

### Mensaje tras Pago Exitoso

```json
{
  "pedidoId": "550e8400-e29b-41d4-a716-446655440000",
  "monto": 120.50,
  "clienteEmail": "cliente@mail.com",
  "paymentStatus": "PAID",          // ← Cambió a PAID
  "createdAt": "2025-11-13T10:30:15",
  "retryCount": 0
}
```

### Mensaje en DLQ (tras 3 fallos)

```json
{
  "pedidoId": "550e8400-e29b-41d4-a716-446655440000",
  "monto": 120.50,
  "clienteEmail": "cliente@mail.com",
  "paymentStatus": "FAILED",        // ← Cambió a FAILED
  "createdAt": "2025-11-13T10:30:15",
  "retryCount": 3                   // ← 3 intentos
}
```

---

## 🗂️ Estructura de Colas

| Cola | Tipo | DLX | TTL | Propósito |
|------|------|-----|-----|-----------|
| `payments.queue` | Durable | orders.exchange | - | Procesar pagos |
| `emails.queue` | Durable | - | - | Enviar emails |
| `orders.dlq` | Durable | - | - | Almacenar fallos permanentes |

### Configuración de DLX (Dead Letter Exchange)

```yaml
payments.queue:
  arguments:
    x-dead-letter-exchange: orders.exchange
    x-dead-letter-routing-key: orders.dead
```

Cuando un mensaje falla 3 veces:
1. PaymentWorker marca `paymentStatus = FAILED`
2. Publica con routing key `orders.dead`
3. RabbitMQ enruta a `orders.dlq`

---

## 🎯 Routing Keys y Bindings

```
orders.exchange (Direct Exchange)
│
├─ Binding 1: orders.created → orders.queue
├─ Binding 2: payments.process → payments.queue
├─ Binding 3: emails.send → emails.queue
└─ Binding 4: orders.dead → orders.dlq
```

### Decisiones de Routing

| Origen | Routing Key | Destino |
|--------|-------------|---------|
| OrderProducer | `payments.process` | payments.queue |
| PaymentWorker (éxito) | `emails.send` | emails.queue |
| PaymentWorker (fallo max) | `orders.dead` | orders.dlq |

---

## 🧵 Modelo de Concurrencia

### Threads Principales

```
┌─────────────────────────────────────────┐
│         Tomcat Thread Pool              │
│   (http-nio-8080-exec-[1-10])           │
│   - Maneja requests HTTP                │
│   - Publica mensajes a RabbitMQ         │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│    RabbitMQ Listener Containers         │
│  org.springframework.amqp.rabbit.       │
│  RabbitListenerEndpointContainer#0-[N]  │
│   - Consume mensajes                    │
│   - Ejecuta @RabbitListener methods     │
└─────────────────────────────────────────┘
```

### Ejemplo de Logs con Threads

```
[2025-11-13 10:30:15.123] [http-nio-8080-exec-1] [ORDER_RECEIVED]
                           ↑ Thread HTTP

[2025-11-13 10:30:15.201] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_PROCESSING]
                           ↑ Thread Listener RabbitMQ
```

---

## 🔐 Configuración de Seguridad

### RabbitMQ (docker-compose.yml)

```yaml
RABBITMQ_DEFAULT_USER: admin
RABBITMQ_DEFAULT_PASS: admin123
```

⚠️ **Nota de Producción**: Cambiar credenciales y usar variables de entorno.

### Spring Boot (application.yml)

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123
```

---

## 📈 Patrones de Diseño Implementados

### 1. Producer-Consumer Pattern
- **Producer**: OrderProducer
- **Consumers**: PaymentWorker, EmailWorker
- **Benefit**: Desacoplamiento entre producción y consumo

### 2. Retry Pattern
- **Implementación**: PaymentWorker con contador de reintentos
- **Configuración**: MAX_RETRIES = 3
- **Benefit**: Tolerancia a fallos transitorios

### 3. Dead Letter Queue Pattern
- **Implementación**: orders.dlq con DLX configuration
- **Benefit**: Manejo de fallos permanentes sin pérdida de datos

### 4. Conditional Processing Pattern
- **Implementación**: EmailWorker verifica `paymentStatus == PAID`
- **Benefit**: Procesamiento selectivo basado en estado

---

## 🧪 Lógica de Reintentos

```java
// PaymentWorker.java (simplified)

if (paymentSuccess) {
    order.setPaymentStatus(PAID);
    rabbitTemplate.send(EMAILS_QUEUE, order);
} else {
    order.setRetryCount(retryCount + 1);
    
    if (order.getRetryCount() >= MAX_RETRIES) {
        order.setPaymentStatus(FAILED);
        rabbitTemplate.send(ORDERS_DLQ, order);
    } else {
        rabbitTemplate.send(PAYMENTS_QUEUE, order); // Retry
    }
}
```

### Tabla de Estados

| Intento | Resultado | Acción | Estado |
|---------|-----------|--------|--------|
| 1 | Fallo | Re-queue (retryCount=1) | PENDING |
| 2 | Fallo | Re-queue (retryCount=2) | PENDING |
| 3 | Fallo | Send to DLQ | FAILED |
| X | Éxito | Send to emails.queue | PAID |

---

## 📦 Dependencias Maven

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    
    <!-- JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    
    <!-- Code Generation -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

---

## 🔍 Formato de Logs

### Estructura

```
[TIMESTAMP] [THREAD] [STEP] PedidoId=UUID | MESSAGE
```

### Ejemplo Real

```
[2025-11-13 10:30:15.123] [http-nio-8080-exec-1] [ORDER_RECEIVED] PedidoId=TEST-001 | Received order for test@mail.com - Amount: $120.5
```

### Campos

- **TIMESTAMP**: `yyyy-MM-dd HH:mm:ss.SSS`
- **THREAD**: Nombre del thread ejecutor
- **STEP**: Etiqueta de operación (ORDER_RECEIVED, PAYMENT_PROCESSING, etc.)
- **PedidoId**: UUID único del pedido
- **MESSAGE**: Descripción de la operación

---

## 🛠️ Configuración de Spring AMQP

### RabbitTemplate Configuration

```java
@Bean
public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(jsonMessageConverter());
    return template;
}
```

### Message Converter

```java
@Bean
public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
}
```

Convierte automáticamente:
- `OrderMessage` (Java) → JSON (RabbitMQ)
- JSON (RabbitMQ) → `OrderMessage` (Java)

---

## 📊 Métricas y Monitoreo

### KPIs Observables

| Métrica | Ubicación | Propósito |
|---------|-----------|-----------|
| **Total de pedidos** | RabbitMQ UI → payments.queue | Carga del sistema |
| **Mensajes en DLQ** | RabbitMQ UI → orders.dlq | Tasa de fallos permanentes |
| **Emails enviados** | Logs → EMAIL_SENT | Confirmaciones exitosas |
| **Tasa de reintentos** | Logs → PAYMENT_FAILED_RETRY | Performance de pago |

### RabbitMQ Management UI

**URL**: http://localhost:15672

**Vistas útiles**:
1. **Overview**: Tasa de mensajes/segundo
2. **Queues**: Profundidad de colas, rate de consume
3. **Connections**: Clientes conectados
4. **Exchanges**: Bindings y routing

---

## 🚨 Manejo de Errores

### Niveles de Error

```
┌──────────────────────────────────────┐
│   Errores Recuperables (Reintentos)  │
│   - Timeout de pago                  │
│   - Servicio externo temporalmente   │
│     no disponible                    │
└─────────────┬────────────────────────┘
              │
              ▼
         [3 Reintentos]
              │
              ▼
┌──────────────────────────────────────┐
│   Errores Permanentes (DLQ)          │
│   - Pago rechazado definitivamente   │
│   - Validación fallida               │
│   - Max reintentos alcanzados        │
└──────────────────────────────────────┘
```

### Estrategia de Backoff

Configurado en `application.yml`:

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        retry:
          initial-interval: 2000    # 2 segundos
          max-attempts: 3
          multiplier: 2.0           # Exponencial: 2s, 4s, 8s
```

---

## 🎓 Conceptos Clave

### 1. Asincronía
- Requests HTTP retornan inmediatamente (202 Accepted)
- Procesamiento ocurre en background
- Desacoplamiento temporal entre servicios

### 2. Idempotencia
- Mensajes con `pedidoId` único
- Re-envío de mensaje con mismo ID debe producir mismo resultado
- Importante para reintentos

### 3. Eventual Consistency
- Sistema no es inmediatamente consistente
- Email puede llegar segundos después del pedido
- Aceptable para mayoría de casos de uso

### 4. Circuit Breaker (Futuro)
- No implementado en esta POC
- Para producción: usar Resilience4j
- Previene cascading failures

---

## 🔮 Extensiones Futuras

### 1. Persistencia de Mensajes
```java
// Guardar en DB antes de enviar a RabbitMQ
orderRepository.save(order);
rabbitTemplate.send(...);
```

### 2. Observabilidad
- Integrar Spring Boot Actuator
- Micrometer para métricas
- Zipkin/Jaeger para tracing distribuido

### 3. Gestión de DLQ
- Worker que consume de DLQ
- Reintentos manuales o automatizados
- Notificaciones a administradores

### 4. Testing
- Unit tests con Mockito
- Integration tests con Testcontainers
- Contract testing con Pact

---

**Versión**: 1.0.0  
**Última actualización**: Noviembre 2025
