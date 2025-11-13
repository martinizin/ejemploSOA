# BikeStore Async - Quick Test Script
# Sends multiple test orders to the API

param(
    [int]$Count = 5,
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "BikeStore Async - Test Runner" -ForegroundColor Cyan
Write-Host "Sending $Count test orders..." -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$successCount = 0
$failCount = 0

for ($i = 1; $i -le $Count; $i++) {
    $pedidoId = "TEST-$("{0:D3}" -f $i)"
    $monto = [math]::Round((Get-Random -Minimum 10 -Maximum 500) + (Get-Random) / 100, 2)
    $email = "customer$i@mail.com"
    
    $body = @{
        pedidoId = $pedidoId
        monto = $monto
        clienteEmail = $email
    } | ConvertTo-Json
    
    try {
        Write-Host "[$i/$Count] Sending order: $pedidoId (Amount: `$$monto)" -ForegroundColor Yellow
        
        $response = Invoke-WebRequest `
            -Uri "$BaseUrl/orders" `
            -Method Post `
            -ContentType "application/json" `
            -Body $body `
            -TimeoutSec 5
        
        if ($response.StatusCode -eq 202) {
            Write-Host "  ✓ Accepted: $($response.Content)" -ForegroundColor Green
            $successCount++
        }
    } catch {
        Write-Host "  ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
        $failCount++
    }
    
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Total Orders: $Count" -ForegroundColor White
Write-Host "Successful:   $successCount" -ForegroundColor Green
Write-Host "Failed:       $failCount" -ForegroundColor Red
Write-Host ""
Write-Host "💡 Check application logs for processing details" -ForegroundColor Yellow
Write-Host "💡 Check RabbitMQ UI: http://localhost:15672" -ForegroundColor Yellow
Write-Host ""
