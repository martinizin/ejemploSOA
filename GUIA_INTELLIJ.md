# Guía Completa de Ejecución - IntelliJ IDEA

## 🎯 Objetivo
Ejecutar BikeStore Async v1.0 desde cero en IntelliJ IDEA de forma reproducible.

---

## 📦 Prerrequisitos

### 1. Verificar Java
```powershell
java -version
```
**Esperado**: Java 17 o superior

Si no tienes Java 17:
- Descarga: https://adoptium.net/
- Instala y configura JAVA_HOME

### 2. Verificar Maven
```powershell
mvn -version
```
**Esperado**: Maven 3.6+

### 3. Verificar Docker
```powershell
docker --version
docker compose version
```

---

## 🚀 Pasos de Ejecución

### PASO 1: Abrir Proyecto en IntelliJ

1. **Abrir IntelliJ IDEA**
2. **File → Open** (Ctrl+O)
3. Navegar a:
   ```
   c:\Users\VICTUS\Desktop\SEMESTRE 202610\MATERIAS\DISEÑO Y ARQ SOFTWARE\SEMANA 07\ejercicioArquitectura
   ```
4. Click **OK**
5. **Esperar**: IntelliJ indexa y descarga dependencias (1-3 minutos)

**Indicador de éxito**: Barra de progreso completa en esquina inferior derecha

---

### PASO 2: Configurar JDK en IntelliJ

1. **File → Project Structure** (Ctrl+Alt+Shift+S)
2. En panel izquierdo: **Project**
3. Configurar:
   - **SDK**: Selecciona Java 17 (o Add SDK si no aparece)
   - **Language level**: 17 - Sealed types, always-strict floating-point
4. Click **Apply** → **OK**

**Verificación**: En ventana Project, src/main/java aparece sin errores

---

### PASO 3: Levantar RabbitMQ

1. En IntelliJ: **View → Tool Windows → Terminal** (Alt+F12)
2. Ejecutar:
   ```powershell
   docker compose up -d
   ```

**Esperado**:
```
✔ Container bikestore-rabbitmq  Started
```

3. Verificar:
   ```powershell
   docker ps
   ```

**Esperado**: Ver contenedor `bikestore-rabbitmq` con puertos 5672 y 15672

---

### PASO 4: Ejecutar la Aplicación

#### Método A: Botón Run (Recomendado)

1. En panel izquierdo, expandir: `src/main/java/com/bikestore/`
2. Click derecho en **Application.java**
3. Seleccionar: **Run 'Application'**

#### Método B: Maven

En terminal de IntelliJ:
```powershell
mvn clean spring-boot:run
```

#### Método C: Configuración de Run

1. **Run → Edit Configurations**
2. Click **+** → **Spring Boot**
3. Configurar:
   - **Name**: BikeStore Async
   - **Main class**: com.bikestore.Application
   - **Module**: bikestore-async
4. Click **OK** → **Run**

---

### PASO 5: Validar Inicio

**Consola IntelliJ debe mostrar**:
```
=== BikeStore Async v1.0 Started ===
API: http://localhost:8080/orders
RabbitMQ Management: http://localhost:15672 (admin/admin123)

...
Started Application in X.XXX seconds
```

**Ventana Services** (si está activa) debe mostrar Spring Boot verde

---

### PASO 6: Probar API

#### Test 1: Health Check

En **nueva terminal** (Click **+** en panel Terminal):
```powershell
curl http://localhost:8080/orders/health
```

**Esperado**: `BikeStore Async v1.0 - UP`

#### Test 2: Crear Pedido

```powershell
curl -X POST http://localhost:8080/orders `
  -H "Content-Type: application/json" `
  -d '{\"pedidoId\":\"TEST-001\",\"monto\":120.50,\"clienteEmail\":\"test@mail.com\"}'
```

**Esperado**: `Order accepted: TEST-001`

---

### PASO 7: Observar Logs

En la **ventana Run** de IntelliJ, deberías ver:

```
[2025-11-13 10:30:15.123] [http-nio-8080-exec-1] [ORDER_RECEIVED] PedidoId=TEST-001 | Received order...
[2025-11-13 10:30:15.145] [http-nio-8080-exec-1] [ORDER_PUBLISHED] PedidoId=TEST-001 | Publishing to exchange...
[2025-11-13 10:30:15.201] [org.springframework...] [PAYMENT_PROCESSING] PedidoId=TEST-001 | Attempt 1/3...
```

**Elementos a verificar**:
- ✅ Timestamp en cada línea
- ✅ Nombre del thread
- ✅ pedidoId presente
- ✅ Transiciones de estado

---

### PASO 8: Monitorear RabbitMQ

1. Abre navegador
2. URL: **http://localhost:15672**
3. Login:
   - **Username**: admin
   - **Password**: admin123
4. Click en tab **Queues**

**Colas esperadas**:
- `payments.queue`
- `emails.queue`
- `orders.dlq`

---

### PASO 9: Prueba de Reintentos

Envía 5-10 pedidos rápidamente:

```powershell
for ($i=1; $i -le 10; $i++) {
    curl -X POST http://localhost:8080/orders `
      -H "Content-Type: application/json" `
      -d "{`"pedidoId`":`"BATCH-$i`",`"monto`":$($i*10).00,`"clienteEmail`":`"batch$i@mail.com`"}"
}
```

**Observar en logs**:
- Algunos pedidos procesan pago exitosamente (PAID) → email enviado
- Otros fallan 3 veces → van a DLQ

**En RabbitMQ UI**:
- `orders.dlq` debe tener mensajes (aprox. 40-50% de los enviados tras reintentos)

---

### PASO 10: Inspeccionar DLQ

1. En RabbitMQ UI, click en **orders.dlq**
2. Scroll down a **Get messages**
3. Click **Get Message(s)**
4. Ver JSON:
   ```json
   {
     "pedidoId": "BATCH-X",
     "paymentStatus": "FAILED",
     "retryCount": 3,
     ...
   }
   ```

---

## 🎨 Tips de IntelliJ

### Ver Logs Organizados
- **View → Tool Windows → Services** (Alt+8)
- Expande **Spring Boot** → **bikestore-async**
- Click derecho → **Show Run Panel**

### Colorear Logs
- **Help → Find Action** → "Registry"
- Buscar: `ide.console.stripe.logs`
- Marcar como **enabled**

### Detener Aplicación
- Click en botón **Stop** (cuadrado rojo) en ventana Run
- O presiona **Ctrl+F2**

### Reiniciar Rápido
- **Run → Reload Changed Classes** (Ctrl+F9) para hot-reload
- O detén y reinicia con **Shift+F10**

---

## 🧪 Casos de Prueba Detallados

### Caso 1: Pago Exitoso (EMAIL enviado)

**Entrada**:
```json
{
  "pedidoId": "SUCCESS-001",
  "monto": 100.00,
  "clienteEmail": "success@mail.com"
}
```

**Log esperado**:
```
[ORDER_RECEIVED]
[ORDER_PUBLISHED]
[PAYMENT_PROCESSING] Attempt 1/3
[PAYMENT_SUCCESS] Status: PAID
[PAYMENT_FORWARDED_TO_EMAIL]
[EMAIL_RECEIVED]
[EMAIL_SENDING]
[EMAIL_SENT] ✓ Confirmation email sent
```

---

### Caso 2: Fallo → 3 Reintentos → DLQ

**Entrada**:
```json
{
  "pedidoId": "FAIL-001",
  "monto": 50.00,
  "clienteEmail": "fail@mail.com"
}
```

**Log esperado** (si falla 3 veces):
```
[PAYMENT_PROCESSING] Attempt 1/3
[PAYMENT_FAILED_RETRY] Retry 1/3
[PAYMENT_PROCESSING] Attempt 2/3
[PAYMENT_FAILED_RETRY] Retry 2/3
[PAYMENT_PROCESSING] Attempt 3/3
[PAYMENT_FAILED_MAX_RETRIES] Sending to DLQ
```

**Validación**:
- NO debe haber logs de `EMAIL_SENT`
- Mensaje aparece en `orders.dlq` en RabbitMQ UI

---

### Caso 3: Pago Falla 2 veces → Éxito en Intento 3

**Log esperado**:
```
[PAYMENT_PROCESSING] Attempt 1/3
[PAYMENT_FAILED_RETRY] Retry 1/3
[PAYMENT_PROCESSING] Attempt 2/3
[PAYMENT_FAILED_RETRY] Retry 2/3
[PAYMENT_PROCESSING] Attempt 3/3
[PAYMENT_SUCCESS] Status: PAID
[EMAIL_SENT] ✓ Confirmation email sent
```

---

## 🔍 Debugging en IntelliJ

### Activar Modo Debug

1. Click en gutter (margen izquierdo) en línea de código
   - Ejemplo: `PaymentWorker.java` línea 35 (antes de `random.nextBoolean()`)
2. Click derecho en **Application.java** → **Debug 'Application'**
3. Envía un pedido con curl
4. IntelliJ pausa en el breakpoint
5. **F8** para ejecutar línea por línea
6. **F9** para continuar

---

## 📊 Verificación Final - Checklist

En IntelliJ, verifica:

- [ ] Proyecto abre sin errores de compilación
- [ ] RabbitMQ corriendo (`docker ps` en terminal)
- [ ] Aplicación inicia correctamente (logs visibles)
- [ ] Health endpoint responde
- [ ] Pedidos se reciben y procesan
- [ ] Logs muestran pedidoId, timestamp, thread
- [ ] Reintentos funcionan (3 intentos)
- [ ] DLQ recibe mensajes fallidos
- [ ] EmailWorker solo procesa PAID
- [ ] RabbitMQ UI muestra colas y mensajes

---

## 🛑 Detener Todo

### Detener Aplicación
En ventana Run de IntelliJ: **Stop** (Ctrl+F2)

### Detener RabbitMQ
En terminal:
```powershell
docker compose down
```

### Limpiar Todo (incluyendo datos)
```powershell
docker compose down -v
```

---

## 📸 Screenshots (Puntos Clave)

### 1. IntelliJ - Proyecto Abierto
**Esperado**: Árbol de archivos visible, sin errores en `src/main/java`

### 2. Terminal - Docker Running
**Esperado**: `docker ps` muestra `bikestore-rabbitmq`

### 3. IntelliJ Run Console
**Esperado**: Logs con formato `[timestamp] [thread] [step] PedidoId=...`

### 4. RabbitMQ Management UI
**Esperado**: Tab "Queues" con 4 colas (payments, emails, orders, dlq)

### 5. Postman / curl Response
**Esperado**: HTTP 202 con mensaje `Order accepted: ...`

---

## ❓ FAQ

**P: ¿Puedo usar Gradle en lugar de Maven?**  
R: Sí, pero esta POC usa Maven. Para Gradle, convierte `pom.xml` a `build.gradle`.

**P: ¿Funciona con Java 21?**  
R: Sí, es compatible hacia adelante.

**P: ¿Cómo cambio el puerto de la API?**  
R: Edita `application.yml`, cambia `server.port: 8080` a otro puerto.

**P: ¿Los logs se guardan en archivo?**  
R: No, solo consola. Para archivo, agrega Logback config.

---

**Fin de la Guía**  
✅ Si completaste todos los pasos, la POC está funcional.
