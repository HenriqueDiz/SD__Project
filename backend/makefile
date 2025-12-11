run-d:
	mvn exec:java -Dexec.mainClass="downloader.Downloader"

run-b1:
	mvn exec:java -Dexec.mainClass="barrel.IndexStorageBarrel" -Dexec.args="barrel1"

run-b2:
	mvn exec:java -Dexec.mainClass="barrel.IndexStorageBarrel" -Dexec.args="barrel2"

run-b3:
	mvn exec:java -Dexec.mainClass="barrel.IndexStorageBarrel" -Dexec.args="barrel3"

run-c:
	mvn exec:java -Dexec.mainClass="client.Client"

run-q:
	mvn exec:java -Dexec.mainClass="queue.URLQueue"

run-g:
	mvn exec:java -Dexec.mainClass="gateway.Gateway"

run-frontend:
ifeq ($(OS),Windows_NT)
	@echo "Inicializando Frontend (Windows)..."
	@powershell -NoProfile -ExecutionPolicy Bypass -File "..\scripts\runFrontendWT.ps1" "$(CURDIR)"
else
	@echo "Inicializando Frontend (macOS/Linux)..."
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && mvn spring-boot:run"'
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)/../frontend\" && npm run dev:https"'
endif

run-backend:
ifeq ($(OS),Windows_NT)
	@echo "Inicializando Backend (Windows)..."
	@powershell -NoProfile -ExecutionPolicy Bypass -File "..\scripts\runBackendWT.ps1" "$(CURDIR)"
else
	@echo "Inicializando Backend (macOS/Linux)..."
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-q"'
	sleep 2
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-g"'
	sleep 5
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-b1"'
	sleep 2
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-b2"'
	sleep 2
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-c"'
	sleep 1
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-d"'
endif

stop-backend:
	@echo "Parando todos os componentes (macOS/Linux)..."
	pkill -f "downloader.Downloader" || true
	pkill -f "barrel.IndexStorageBarrel" || true
	pkill -f "client.Client" || true
	pkill -f "queue.URLQueue" || true
	pkill -f "gateway.Gateway" || true

run-all:
	mvn clean compile
	make run-backend
	make run-frontend

clean:
	mvn clean compile