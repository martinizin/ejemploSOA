# BikeStore Async v1.0 - POC Asíncrona

> **Arquitectura asíncrona con RabbitMQ | Java 17 + Spring Boot**

## 📋 Índice

1. [Arquitectura](#-arquitectura)
2. [Requisitos](#-requisitos)
3. [Guía Paso a Paso - IntelliJ IDEA](#-guía-paso-a-paso---intellij-idea)
4. [Pruebas y Validación](#-pruebas-y-validación)
5. [Monitoreo](#-monitoreo)
6. [Nota para Visual Studio](#-nota-para-visual-studio)
7. [Troubleshooting](#-troubleshooting)

---

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

## 🚀 Guía Paso a Paso - IntelliJ IDEA

### Paso 1: Clonar/Abrir el Proyecto

1. Abre **IntelliJ IDEA**
2. Selecciona **Open** y navega a la carpeta del proyecto:
   ```
   c:\Users\VICTUS\Desktop\SEMESTRE 202610\MATERIAS\DISEÑO Y ARQ SOFTWARE\SEMANA 07\ejercicioArquitectura
   ```
3. IntelliJ detectará automáticamente el proyecto Maven

   **Esperado**: IntelliJ indexa el proyecto y descarga dependencias

### Paso 2: Configurar JDK

1. Ve a **File → Project Structure** (Ctrl+Alt+Shift+S)
2. En **Project**, selecciona:
   - **SDK**: Java 17 o superior
   - **Language level**: 17
3. Clic en **Apply** y **OK**

### Paso 3: Levantar RabbitMQ con Docker

1. Abre terminal integrada en IntelliJ (**View → Tool Windows → Terminal**)
2. Ejecuta:
   ```powershell
   docker compose up -d
   ```

**Validación**:
```powershell
docker ps
```

**Esperado**: Ver contenedor `bikestore-rabbitmq` corriendo en puertos 5672 y 15672

### Paso 4: Verificar Configuración

1. Abre `src/main/resources/application.yml`
2. Verifica la configuración de RabbitMQ:
   ```yaml
   spring:
     rabbitmq:
       host: localhost
       port: 5672
       username: admin
       password: admin123
   ```

### Paso 5: Ejecutar la Aplicación

#### Opción A: Desde IntelliJ (Recomendado)

1. Localiza `src/main/java/com/bikestore/Application.java`
2. Clic derecho → **Run 'Application'**
3. O presiona **Shift+F10**

#### Opción B: Terminal Maven

```powershell
mvn clean spring-boot:run
```

**Esperado en consola**:
```
=== BikeStore Async v1.0 Started ===
API: http://localhost:8080/orders
RabbitMQ Management: http://localhost:15672 (admin/admin123)
```

### Paso 6: Verificar Salud de la API

En terminal (nueva pestaña):
```powershell
curl http://localhost:8080/orders/health
```

**Esperado**: `BikeStore Async v1.0 - UP`

---

## 🧪 Pruebas y Validación

### Prueba 1: Enviar Pedido (Escenario Exitoso)

```powershell
curl -X POST http://localhost:8080/orders `
  -H "Content-Type: application/json" `
  -d '{\"pedidoId\":\"ORDER-001\",\"monto\":120.50,\"clienteEmail\":\"cliente@mail.com\",\"paymentStatus\":\"PENDING\"}'
```

**Logs esperados** (consola IntelliJ):

```
[2025-11-13 10:15:30.123] [http-nio-8080-exec-1] [ORDER_RECEIVED] PedidoId=ORDER-001 | Received order for cliente@mail.com - Amount: $120.5
[2025-11-13 10:15:30.145] [http-nio-8080-exec-1] [ORDER_PUBLISHED] PedidoId=ORDER-001 | Publishing to exchange: orders.exchange
[2025-11-13 10:15:30.156] [http-nio-8080-exec-1] [ORDER_SENT_TO_PAYMENT] PedidoId=ORDER-001 | Message sent to payment processing
[2025-11-13 10:15:30.201] [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#0-1] [PAYMENT_PROCESSING] PedidoId=ORDER-001 | Attempt 1/3 - Amount: $120.5
```

#### Escenario A: Pago Exitoso (50% prob.)

```
[2025-11-13 10:15:30.210] [PAYMENT_SUCCESS] PedidoId=ORDER-001 | Payment processed successfully - Status: PAID
[2025-11-13 10:15:30.215] [PAYMENT_FORWARDED_TO_EMAIL] PedidoId=ORDER-001 | Order sent to email queue
[2025-11-13 10:15:30.320] [EMAIL_RECEIVED] PedidoId=ORDER-001 | Email task received - Status: PAID
[2025-11-13 10:15:30.325] [EMAIL_SENDING] PedidoId=ORDER-001 | Sending confirmation email to: cliente@mail.com
[2025-11-13 10:15:30.830] [EMAIL_SENT] PedidoId=ORDER-001 | ✓ Confirmation email sent successfully to cliente@mail.com
```

#### Escenario B: Fallo con Reintentos (50% prob.)

```
[2025-11-13 10:15:30.210] [PAYMENT_FAILED_RETRY] PedidoId=ORDER-001 | Payment failed - Retry 1/3
[2025-11-13 10:15:32.215] [PAYMENT_PROCESSING] PedidoId=ORDER-001 | Attempt 2/3 - Amount: $120.5
[2025-11-13 10:15:32.220] [PAYMENT_FAILED_RETRY] PedidoId=ORDER-001 | Payment failed - Retry 2/3
[2025-11-13 10:15:34.230] [PAYMENT_PROCESSING] PedidoId=ORDER-001 | Attempt 3/3 - Amount: $120.5
[2025-11-13 10:15:34.235] [PAYMENT_FAILED_MAX_RETRIES] PedidoId=ORDER-001 | Payment failed after 3 attempts - Sending to DLQ
```

### Prueba 2: Múltiples Pedidos (Observar Threads)

```powershell
# Pedido 1
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{\"pedidoId\":\"ORDER-002\",\"monto\":75.00,\"clienteEmail\":\"user1@mail.com\"}'

# Pedido 2
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{\"pedidoId\":\"ORDER-003\",\"monto\":200.00,\"clienteEmail\":\"user2@mail.com\"}'

# Pedido 3
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{\"pedidoId\":\"ORDER-004\",\"monto\":50.00,\"clienteEmail\":\"user3@mail.com\"}'
```

**Validación**: Observar en logs:
- ✅ Diferentes **thread names** (`exec-1`, `exec-2`, `RabbitListenerEndpointContainer#0-1`)
- ✅ **Timestamps** precisos por cada transición
- ✅ **pedidoId** presente en cada línea
- ✅ **Procesamiento asíncrono** (no secuencial)

### Prueba 3: Validar DLQ (Dead Letter Queue)

Envía 5-10 pedidos y verifica en RabbitMQ Management UI:

1. Abre navegador: **http://localhost:15672**
2. Login: `admin` / `admin123`
3. Ve a **Queues** tab
4. Observa:
   - `payments.queue`: Mensajes entrantes
   - `orders.dlq`: Mensajes fallidos tras 3 reintentos
   - `emails.queue`: Solo mensajes con `PAID`

---

## 📊 Monitoreo

### RabbitMQ Management UI

**URL**: http://localhost:15672  
**Credenciales**: admin / admin123

#### Colas a Monitorear

| Cola | Propósito | Esperado |
|------|-----------|----------|
| `payments.queue` | Procesamiento de pagos | Consume rápido, reintentos visibles |
| `emails.queue` | Envío de emails | Solo mensajes PAID |
| `orders.dlq` | Fallos permanentes | Mensajes tras 3 reintentos fallidos |

#### Inspeccionar Mensajes

1. En RabbitMQ UI, clic en **`orders.dlq`**
2. Scroll a **Get messages**
3. Clic en **Get Message(s)**
4. Ver payload JSON con `paymentStatus: FAILED` y `retryCount: 3`

---

## 📝 Nota para Visual Studio

> **Compatibilidad**: Este proyecto está optimizado para **IntelliJ IDEA**. Para **Visual Studio Code**:

### Ejecutar desde VS Code

1. Instala extensiones:
   - **Extension Pack for Java** (Microsoft)
   - **Spring Boot Extension Pack** (VMware)

2. Abre la carpeta del proyecto

3. Ejecutar RabbitMQ:
   ```powershell
   docker compose up -d
   ```

4. Ejecutar aplicación:
   - Presiona **F5** o usa terminal:
     ```powershell
     mvn spring-boot:run
     ```

5. Probar API con el mismo `curl` indicado arriba

### Limitaciones VS Code

- No hay soporte nativo para ejecutar aplicaciones Java con "modo agente" similar a IntelliJ
- Para debugging avanzado, IntelliJ IDEA es recomendado
- VS Code funciona correctamente para ejecución estándar y pruebas

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
2. Asegúrate de que IntelliJ esté mostrando la ventana **Run**

### Mensajes no se procesan

**Checklist**:
- [ ] RabbitMQ corriendo (`docker ps`)
- [ ] Aplicación iniciada correctamente
- [ ] Colas creadas en RabbitMQ UI
- [ ] Revisa logs para excepciones

---

## ✅ Checklist de Validación Final

- [ ] **RabbitMQ** corriendo en Docker
- [ ] **API** `/orders` responde (health check OK)
- [ ] **Publicación** de mensajes exitosa
- [ ] **PaymentWorker** procesa con reintentos (3x)
- [ ] **DLQ** recibe mensajes fallidos
- [ ] **EmailWorker** solo procesa `PAID`
- [ ] **Logs** incluyen pedidoId, timestamp, thread en cada paso
- [ ] **RabbitMQ UI** muestra colas y mensajes

---

## 📚 Recursos Adicionales

- [Spring AMQP Documentation](https://docs.spring.io/spring-amqp/reference/)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
- [Dead Letter Exchanges](https://www.rabbitmq.com/dlx.html)

---

**Versión**: 1.0.0  
**Autor**: BikeStore Architecture Team  
**Fecha**: Noviembre 2025
