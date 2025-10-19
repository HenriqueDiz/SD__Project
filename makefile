run-d:
	mvn exec:java -Dexec.mainClass="downloader.Downloader" -Dexec.args="localhost 1099 https://abola.pt"

run-b:
	mvn exec:java -Dexec.mainClass="barrel.IndexStorageBarrel"