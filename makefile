run-d:
	mvn exec:java -Dexec.mainClass="downloader.Downloader" -Dexec.args="bola 1099 https://abola.pt"

run-b:
	mvn exec:java -Dexec.mainClass="barrel.IndexStorageBarrel"

run-c:
	mvn exec:java -Dexec.mainClass="client.Client" -Dexec.args="1099"