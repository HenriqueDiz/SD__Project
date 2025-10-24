# How to compile/run
#### Best commands:
- `make all` Compila todos os componentes
- `make stop-all` Para todos os componentes
- `make clean` Limpa os ficheiros compilados

#### Run commands:
- `make run-g` Corre a Gateway
- `make run-d` Corre o Downloader
- `make run-b1` Corre o Barrel 1
- `make run-b2` Corre o Barrel 2
- `make run-c` Corre o Cliente
- `make run-q` Corre o URL-queue



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
