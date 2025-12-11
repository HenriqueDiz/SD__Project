# Script para iniciar o Frontend com HTTPS na porta 3000
# Googol Search Engine - Frontend HTTPS

Write-Host "╔════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   Iniciando Frontend com HTTPS (porta 3000)    ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Navegar para o diretório do frontend
$frontendPath = Join-Path $PSScriptRoot "..\frontend"
Set-Location $frontendPath

# Verificar se os certificados existem
$certsPath = Join-Path $frontendPath "certs"
if (-not (Test-Path $certsPath)) {
    Write-Host "⚠️  Certificados SSL não encontrados. Gerando..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $certsPath -Force | Out-Null
    
    # Gerar certificados SSL
    openssl req -x509 -newkey rsa:2048 -keyout "$certsPath\key.pem" -out "$certsPath\cert.pem" -days 365 -nodes -subj "/C=PT/ST=Portugal/L=Coimbra/O=Googol/CN=localhost"
    
    Write-Host "✓ Certificados SSL gerados com sucesso!" -ForegroundColor Green
    Write-Host ""
}

# Verificar se as dependências estão instaladas
if (-not (Test-Path "node_modules")) {
    Write-Host "⚠️  Dependências não encontradas. Instalando..." -ForegroundColor Yellow
    npm install
    Write-Host "✓ Dependências instaladas!" -ForegroundColor Green
    Write-Host ""
}

# Iniciar o servidor HTTPS
Write-Host "🚀 Iniciando servidor HTTPS..." -ForegroundColor Green
Write-Host ""
npm run dev:https
