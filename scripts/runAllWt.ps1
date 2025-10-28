param([string]$cwd = (Get-Location).Path)

if (-not (Get-Command wt -ErrorAction SilentlyContinue)) {
    Write-Error "wt (Windows Terminal) não encontrado. Instale o Windows Terminal."
    exit 1
}

# Usar aspas duplas ao redor do caminho completo
$escapedPath = "`"$cwd`""

# Comando que cria 3 terminais cada um com 2 painéis (o primeiro com URL Queue e o Barrel 1, o segundo com o Barrel 2 e o Gateway, e o terceiro com o Client e o Downloader)
wt `
  new-tab cmd /k "cd /d $escapedPath && title url-queue && make run-q" `; `
  split-pane -V cmd /k "cd /d $escapedPath && title barrel1 && make run-b1" `; `
  new-tab cmd /k "cd /d $escapedPath && title barrel2 && make run-b2" `; `
  split-pane -V cmd /k "cd /d $escapedPath && title gateway && timeout /t 2 /nobreak && make run-g" `; `
  new-tab cmd /k "cd /d $escapedPath && title client && timeout /t 5 /nobreak && make run-c" `; `
  split-pane -V cmd /k "cd /d $escapedPath && title downloader && timeout /t 5 /nobreak && make run-d"