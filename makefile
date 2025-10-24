run-d:
	mvn exec:java -Dexec.mainClass="downloader.Downloader" -Dexec.args="bola 8183 8181 https://pt.wikipedia.org/wiki/Wikipédia:Página_principal"

run-b1:
	mvn exec:java -Dexec.mainClass="barrel.IndexStorageBarrel" -Dexec.args="8182 barrel1"

run-b2:
	mvn exec:java -Dexec.mainClass="barrel.IndexStorageBarrel" -Dexec.args="8184 barrel2"

run-c:
	mvn exec:java -Dexec.mainClass="client.Client" -Dexec.args="8183"

run-q:
	mvn exec:java -Dexec.mainClass="queue.URLQueue" -Dexec.args="8181"

run-g:
	mvn exec:java -Dexec.mainClass="gateway.Gateway"

# Comando para abrir tudo de uma vez
run-all:
	@echo "Iniciando todos os serviços..."
	@echo "Abrindo terminais..."
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-q"'
	sleep 2
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-b1"'
	sleep 2
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-b2"'
	sleep 2
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-g"'
	sleep 3
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-c"'
	sleep 1
	osascript -e 'tell application "Terminal" to do script "cd \"$(CURDIR)\" && make run-d"'
	@echo "Todos os serviços iniciados!"

# Comando para parar tudo
stop-all:
	@echo "Parando todos os serviços..."
	pkill -f "downloader.Downloader" || true
	pkill -f "barrel.IndexStorageBarrel" || true
	pkill -f "client.Client" || true
	pkill -f "queue.URLQueue" || true
	pkill -f "gateway.Gateway" || true
	@echo "Todos os serviços parados!"


clean:
	mvn clean compile