# How to compile/run

- `mvn clean compile` Limpa e compila os ficheiros
- `make run-d` Corre o Downloader
- `make run-b` Corre o Barrel
- `make run-c` Corre o Cliente
- `make run-q` Corre o URL-queue
- `make clean` Limpa os ficheiros compilados

Por agora, correr primeiro o barrel e depois o downloader noutro terminal

# Componentes
#### Gateway 
- Porta de entrada do sistema. *ExtendsRemoteObjects* 

#### Cliente
- Aplicação, neste caso, de linha de comandos. Começa por ligar-se à gateway por RMI
#### Barrels
- São vários e partilham todos a mesma informação e podem comunicar entre eles ou através do gateway

#### Downloaders
- São os workers, os que vão às páginas buscar mais urls e outras informações, threads independentes
#### URL-queue
- Os barrels é que guardam a informação da queue quando esta é fechada
