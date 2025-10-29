run-d:
	mvn exec:java -Dexec.mainClass="downloader.Downloader"

run-b1:
	mvn exec:java -Dexec.mainClass="barrel.IndexStorageBarrel" -Dexec.args="barrel1"

run-b2:
	mvn exec:java -Dexec.mainClass="barrel.IndexStorageBarrel" -Dexec.args="barrel2"

run-c:
	mvn exec:java -Dexec.mainClass="client.Client"

run-q:
	mvn exec:java -Dexec.mainClass="queue.URLQueue"

run-g:
	mvn exec:java -Dexec.mainClass="gateway.Gateway"

run-all:
ifeq ($(OS),Windows_NT)
	@echo "Inicializando todos os componentes (Windows)..."
	@powershell -NoProfile -ExecutionPolicy Bypass -File "scripts\runAllWt.ps1" "$(CURDIR)"
else
	@echo "Inicializando todos os componentes (macOS/Linux)..."
	@echo "Abrindo terminais..."
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-q"'
	sleep 2
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-g"'
	sleep 5
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-b1"'
	sleep 2
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-c"'
	sleep 1
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-d"'
	@echo "Todos os componentes iniciados!"
endif

stop-all:
	@echo "Parando todos os componentes..."
	pkill -f "downloader.Downloader" || true
	pkill -f "barrel.IndexStorageBarrel" || true
	pkill -f "client.Client" || true
	pkill -f "queue.URLQueue" || true
	pkill -f "gateway.Gateway" || true
	@echo "Todos os componentes parados!"

clean:
	mvn clean compile