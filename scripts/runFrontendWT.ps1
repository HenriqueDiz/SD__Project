param([string]$cwd = (Get-Location).Path)

if (-not (Get-Command wt -ErrorAction SilentlyContinue)) {
    Write-Error "wt (Windows Terminal) não encontrado. Instale o Windows Terminal."
    exit 1
}

$frontendPath = Join-Path $cwd "..\frontend"

wt `
  new-tab cmd /k "cd /d $cwd && title spring-backend && mvn spring-boot:run" `; `
  split-pane -V cmd /k "cd /d $frontendPath && title react-frontend && npm run dev:https"