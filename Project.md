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
