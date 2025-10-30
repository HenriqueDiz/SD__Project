# How to compile/run
#### Best commands:
- `make run-all` Corre todos os componentes
- `make stop-all` Para todos os componentes (Apenas macOs/linux)
- `make clean` Limpa e compila o projeto

#### Run commands for each componente individually :
- Gateway: `make run-g`
- Downloader: `make run-d` (Args: `<gatewayPort>` `<queuePort>`)
- Barrel 1: `make run-b1` (Args: `<barrel1Port>` `<barrel1Name>`)
- Barrel 2: `make run-b2` (Args: `<barrel2Port>` `<barrel2Name>`)
- Client: `make run-c` (Args: `<gatewayPort>`)
- URL-Queue: `make run-q` (Args: `<queuePort>`)

(O uso de argumentos não é obrigatório, uma vez que host/port/name podem ser mudados no `\src\main\resources\Config.cfg`)

# Explicação do sistema
Duas versões de cada método porque eles pertencem a componentes diferentes e com responsabilidades distintas:

**Barrel.addToIndex / Barrel.searchWord**

- Estão na interface/impl do barrel. É o código que realmente armazena e recupera dados (ConcurrentHashMap). Cada barrel é um servidor RMI que expõe esses métodos.

**Gateway.addToIndex / Gateway.searchWord**

- Estão na interface/impl do gateway. O gateway actua como fachada: recebe pedidos dos clients/downloader, faz load‑balancing/failover e depois chama os métodos dos barrels. No teu caso o gateway também replica indexação enviando para todos os barrels.

# Componentes
#### Gateway 
- Porta de entrada do sistema. *ExtendsRemoteObjects* 

#### Cliente
- Aplicação, neste caso, de linha de comandos. Começa por ligar-se à gateway por RMI
- Usar por exemplo este url para testar: `https://pt.wikipedia.org/wiki/Wikipédia:Página_principal`
#### Barrels
- São vários e partilham todos a mesma informação e podem comunicar entre eles ou através do gateway
#### Downloaders
- São os workers, os que vão às páginas buscar mais urls e outras informações, threads independentes
#### URL-queue
- Os barrels é que guardam a informação da queue quando esta é fechada


# Estatísticas:
- indentificação dos barrels ativos + tamanho da hash map + tempo de resposta de pesquisa 

- (Para guardar o tempo)
- incrementar t1 + t2 + t3 --------> chega um -----------> incrementar t1 + t2 + t3 + t4
- divisor 3                                                 divisor 4
