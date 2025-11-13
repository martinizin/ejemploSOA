# Test Commands - BikeStore Async v1.0

## PowerShell Commands

### Start RabbitMQ
```powershell
docker compose up -d
```

### Check RabbitMQ Status
```powershell
docker ps | findstr rabbitmq
```

### Test 1: Single Order (Success scenario)
```powershell
curl -X POST http://localhost:8080/orders `
  -H "Content-Type: application/json" `
  -d '{\"pedidoId\":\"TEST-001\",\"monto\":120.50,\"clienteEmail\":\"test@mail.com\",\"paymentStatus\":\"PENDING\"}'
```

### Test 2: Multiple Orders
```powershell
# Order 1
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{\"pedidoId\":\"TEST-002\",\"monto\":75.00,\"clienteEmail\":\"user1@mail.com\"}'

# Order 2
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{\"pedidoId\":\"TEST-003\",\"monto\":200.00,\"clienteEmail\":\"user2@mail.com\"}'

# Order 3
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{\"pedidoId\":\"TEST-004\",\"monto\":50.00,\"clienteEmail\":\"user3@mail.com\"}'
```

### Test 3: Batch Testing (10 orders)
```powershell
for ($i=1; $i -le 10; $i++) {
    curl -X POST http://localhost:8080/orders `
      -H "Content-Type: application/json" `
      -d "{`\"pedidoId`\":`"BATCH-$i`",`"monto`":$($i*10).00,`"clienteEmail`":`"batch$i@mail.com`"}"
    Start-Sleep -Milliseconds 500
}
```

### Health Check
```powershell
curl http://localhost:8080/orders/health
```

### View RabbitMQ Logs
```powershell
docker logs bikestore-rabbitmq --tail 50
```

### Stop RabbitMQ
```powershell
docker compose down
```

### Clean All (including volumes)
```powershell
docker compose down -v
```

## CMD Commands (Alternative)

### Single Order
```cmd
curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d "{\"pedidoId\":\"TEST-001\",\"monto\":120.50,\"clienteEmail\":\"test@mail.com\"}"
```

## Git Bash / Linux

### Single Order
```bash
curl -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"pedidoId":"TEST-001","monto":120.50,"clienteEmail":"test@mail.com"}'
```

### Multiple Orders
```bash
for i in {1..5}; do
  curl -X POST http://localhost:8080/orders \
    -H 'Content-Type: application/json' \
    -d "{\"pedidoId\":\"ORDER-$i\",\"monto\":$((i*50)),\"clienteEmail\":\"user$i@mail.com\"}"
  sleep 1
done
```

## Expected Responses

### Success (202 Accepted)
```
Order accepted: TEST-001
```

### Health Check (200 OK)
```
BikeStore Async v1.0 - UP
```
