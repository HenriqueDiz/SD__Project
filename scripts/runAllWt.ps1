param([string]$cwd = (Get-Location).Path)

if (-not (Get-Command wt -ErrorAction SilentlyContinue)) {
    Write-Error "wt (Windows Terminal) não encontrado. Instale o Windows Terminal."
    exit 1
}

# Escapa aspas no caminho
$escaped = $cwd -replace '"','\"'

# Comando que cria 3 terminais cada um com 2 painéis (o primeiro com URL Queue e o Barrel 1, o segundo com o Barrel 2 e o Gateway, e o terceiro com o Client e o Downloader)
$cmd = "new-tab cmd /k `"cd /d $escaped && title url-queue && make run-q`";"
$cmd += " split-pane -V cmd /k `"cd /d $escaped && title barrel1 && make run-b1`";"


$cmd += " ; new-tab cmd /k `"cd /d $escaped && title barrel2 && make run-b2`";"
$cmd += " split-pane -V cmd /k `"cd /d $escaped && title gateway && make run-g`";"

$cmd += " ; new-tab cmd /k `"cd /d $escaped && title client && make run-c`";"
$cmd += " split-pane -V cmd /k `"cd /d $escaped && title downloader && make run-d`""

Start-Process wt -ArgumentList $cmd