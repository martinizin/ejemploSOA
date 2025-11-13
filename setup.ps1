# BikeStore Async - Setup and Test Script
# PowerShell automation script

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "BikeStore Async v1.0 - Setup Script" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Function to check if command exists
function Test-Command($cmdname) {
    return [bool](Get-Command -Name $cmdname -ErrorAction SilentlyContinue)
}

# 1. Check Prerequisites
Write-Host "[1/6] Checking prerequisites..." -ForegroundColor Yellow

if (Test-Command java) {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "  ✓ Java: $javaVersion" -ForegroundColor Green
} else {
    Write-Host "  ✗ Java not found!" -ForegroundColor Red
    exit 1
}

if (Test-Command mvn) {
    $mavenVersion = mvn -version | Select-String "Apache Maven"
    Write-Host "  ✓ Maven: $mavenVersion" -ForegroundColor Green
} else {
    Write-Host "  ✗ Maven not found!" -ForegroundColor Red
    exit 1
}

if (Test-Command docker) {
    $dockerVersion = docker --version
    Write-Host "  ✓ Docker: $dockerVersion" -ForegroundColor Green
} else {
    Write-Host "  ✗ Docker not found!" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 2. Start RabbitMQ
Write-Host "[2/6] Starting RabbitMQ..." -ForegroundColor Yellow

$rabbitmqRunning = docker ps --filter "name=bikestore-rabbitmq" --format "{{.Names}}"
if ($rabbitmqRunning) {
    Write-Host "  ✓ RabbitMQ already running" -ForegroundColor Green
} else {
    docker compose up -d
    Write-Host "  ✓ RabbitMQ started" -ForegroundColor Green
    Write-Host "  ⏳ Waiting for RabbitMQ to be ready (15 seconds)..." -ForegroundColor Cyan
    Start-Sleep -Seconds 15
}

Write-Host ""

# 3. Check RabbitMQ Health
Write-Host "[3/6] Checking RabbitMQ health..." -ForegroundColor Yellow

$rabbitmqStatus = docker ps --filter "name=bikestore-rabbitmq" --format "{{.Status}}"
if ($rabbitmqStatus -like "*Up*") {
    Write-Host "  ✓ RabbitMQ is healthy: $rabbitmqStatus" -ForegroundColor Green
} else {
    Write-Host "  ✗ RabbitMQ is not healthy!" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 4. Build Project
Write-Host "[4/6] Building project..." -ForegroundColor Yellow
Write-Host "  (This may take a few minutes on first run)" -ForegroundColor Cyan

mvn clean package -DskipTests -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Build successful" -ForegroundColor Green
} else {
    Write-Host "  ✗ Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 5. Start Application (in background)
Write-Host "[5/6] Starting application..." -ForegroundColor Yellow

$appProcess = Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -PassThru -NoNewWindow
Write-Host "  ⏳ Waiting for application to start (20 seconds)..." -ForegroundColor Cyan
Start-Sleep -Seconds 20

Write-Host ""

# 6. Test Health Endpoint
Write-Host "[6/6] Testing application..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/orders/health" -Method Get -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✓ Application is UP: $($response.Content)" -ForegroundColor Green
    }
} catch {
    Write-Host "  ✗ Application not responding!" -ForegroundColor Red
    Write-Host "  Check logs manually or wait longer for startup" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📌 Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Open RabbitMQ Management: http://localhost:15672 (admin/admin123)"
Write-Host "  2. Test API endpoint:"
Write-Host "     curl -X POST http://localhost:8080/orders ``" -ForegroundColor Cyan
Write-Host "       -H 'Content-Type: application/json' ``" -ForegroundColor Cyan
Write-Host "       -d '{`"pedidoId`":`"TEST-001`",`"monto`":120.50,`"clienteEmail`":`"test@mail.com`"}'" -ForegroundColor Cyan
Write-Host "  3. Check logs in terminal where Maven is running"
Write-Host ""
Write-Host "To stop everything:" -ForegroundColor Yellow
Write-Host "  - Stop Maven process: Ctrl+C"
Write-Host "  - Stop RabbitMQ: docker compose down"
Write-Host ""
