# Sistema Distribuído de Indexação e Pesquisa de Páginas Web

Sistema distribuído de indexação e pesquisa web implementado em Java com RMI (Remote Method Invocation). O sistema é composto por múltiplos componentes que trabalham de forma coordenada para indexar páginas web e permitir pesquisas eficientes.

---

## Requisitos

- **Java JDK 11 ou superior**
- **Maven 3.6+**
- **Sistema Operacional:** macOS, Linux ou Windows
- **Conexão à Internet** (para indexação de páginas web)

---

##  Instalação

### 1. Clonar o repositório
```bash
git clone <url-do-repositório>
cd SD_Project
```

### 2. Compilar o projeto
```bash
make clean
```
Ou usando Maven diretamente:
```bash
mvn clean compile
```

---

## Execução

### Opção 1: Executar todos os componentes automaticamente
```bash
make run-all
```
Este comando inicia todos os componentes do sistema na seguinte ordem:
1. Gateway (porta padrão: 8183)
2. URL Queue (porta padrão: 8181)
3. Barrel 1 (porta padrão: 8186)
4. Barrel 2 (porta padrão: 8182)
5. Downloader
6. Client

### Opção 2: Executar componentes individualmente

#### Gateway
```bash
make run-g
```
Ou com argumentos:
```bash
java -cp target/classes gateway.Gateway <port> <name> <host>
```

#### URL Queue
```bash
make run-q
```
Ou com argumentos:
```bash
java -cp target/classes queue.URLQueue <port> <name> <host>
```

#### Barrel (Storage Barrel)
```bash
make run-b1    # Barrel 1
make run-b2    # Barrel 2
```
Ou com argumentos:
```bash
java -cp target/classes barrel.IndexStorageBarrel <port> <name> <host>
```

#### Downloader
```bash
make run-d
```
Ou com argumentos:
```bash
java -cp target/classes downloader.Downloader <gatewayPort> <queuePort> <gatewayHost> <queueHost>
```

#### Client
```bash
make run-c
```
Ou com argumentos:
```bash
java -cp target/classes client.Client <gatewayPort> <gatewayHost>
```

---

## Configuração

As configurações padrão podem ser alteradas no arquivo:
```
src/main/resources/Config.properties
```

### Exemplo de configuração:
```properties
# Gateway
gateway.host=127.0.0.1
gateway.port=8183
gateway.name=gateway

# URL Queue
queue.host=127.0.0.1
queue.port=8181
queue.name=urlqueue

# Barrel1
barrel1.host=127.0.0.1
barrel1.port=8186
barrel1.name=barrel1
```

**Nota:** Se forem fornecidos argumentos na linha de comandos, estes têm prioridade sobre as configurações do ficheiro.

---

## Parar o Sistema

### macOS/Linux:
```bash
make stop-all
```

### Windows:
Fechar manualmente cada terminal ou usar `Ctrl+C` em cada janela.

---

## Arquitetura do Sistema

### Componentes

#### 1. **Gateway** (Porta de entrada)
- Ponto central de comunicação do sistema
- Faz load balancing entre os barrels ativos
- Gere failover automático de barrels
- Agrega resultados de pesquisa de múltiplos barrels
- Mantém estatísticas do sistema

#### 2. **Index Storage Barrels** (Armazenamento distribuído)
- Armazenam o índice invertido (palavra → URLs)
- Sincronizam dados entre si automaticamente
- Removem stop words de forma dinâmica usando IQR (Interquartile Range)
- Guardam progresso em disco para recuperação após falhas
- Podem ser adicionados/removidos dinamicamente

#### 3. **Downloader** (Workers de indexação)
- Descarregam páginas web e extraem conteúdo
- Tokenizam texto e enviam palavras para os barrels
- Extraem links e adicionam à fila de URLs
- Processam páginas de forma assíncrona
- Suportam retry automático em caso de erro

#### 4. **URL Queue** (Fila de URLs)
- Gere URLs a processar (BlockingDeque)
- Evita URLs duplicados
- Persiste estado em barrels ao encerrar
- Suporta priorização de URLs

#### 5. **Client** (Interface do utilizador)
- Interface de linha de comandos
- Permite pesquisas por uma ou múltiplas palavras
- Mostra páginas ordenadas por relevância
- Permite adicionar URLs manualmente
- Exibe estatísticas do sistema

---

## Funcionalidades

### Pesquisa
- **Pesquisa por palavra única:** Retorna todos os URLs que contêm a palavra
- **Pesquisa por múltiplas palavras:** Retorna URLs que contêm TODAS as palavras (interseção)
- **Ordenação por referências:** Resultados ordenados pelo número de links recebidos (popularidade)
- **Informação detalhada:** Título, URL, descrição (snippet) de cada página

### Indexação
- **Indexação distribuída:** Múltiplos barrels processam em paralelo
- **Stop words dinâmicas:** Remoção automática de palavras muito comuns usando IQR
- **Persistência:** Índices guardados em disco automaticamente
- **Sincronização:** Novos barrels recebem dados dos existentes

### Estatísticas
- Número de barrels ativos
- Tamanho do índice de cada barrel
- Tempo médio de resposta por barrel
- Top 10 pesquisas mais realizadas
- Páginas mais referenciadas

---

## Exemplo de Uso

### 1. Iniciar o sistema
```bash
make run-all
```

### 2. No Client, escolher uma opção do menu:
```
CLIENT MENU
============================================
1. Adicionar URL para indexar
2. Procurar uma palavra
3. Ver estatísticas
4. Consultar lista de ligações de uma página
0. Sair
============================================
```

### 3. Adicionar URL para indexar:
```
Escolha uma opção (1-5): 1
Digite o URL (http:// ou https://): https://pt.wikipedia.org/wiki/Java
URL adicionado à fila com sucesso!
```

### 4. Pesquisar palavras:
```
Escolha uma opção (1-5): 2
Palavra(s) a pesquisar: java programação
Encontrados 42 resultado(s) para: [java, programação]
------------------------------------------------------------
[1] - 15 Referência(s)
Java (linguagem de programação)
URL: https://pt.wikipedia.org/wiki/Java_(linguagem_de_programação)
    Java é uma linguagem de programação orientada a objetos...
------------------------------------------------------------
```

### 5. Ver estatísticas:
```
==================================================
Escolha uma opção (1-5): 3

TOP 10 PESQUISAS
------------------------------
Ainda não há pesquisas registradas

BARRELS ATIVOS
------------------------------
Barrel Ativo -> barrel1
Porta: 8182
Host: 127.0.0.1
Índice: 0

Barrel Ativo -> barrel2
Porta: 8184
Host: 127.0.0.1
Índice: 0


BARRELS REGISTRADOS
------------------------------
Barrel Registrado -> barrel2:8184:127.0.0.1
Barrel Registrado -> barrel1:8182:127.0.0.1

TEMPO MÉDIO DE RESPOSTA POR BARREL
------------------------------
Nenhum tempo de resposta registrado.
...
```

---

## Estrutura de Ficheiros

```
SD_Project/
├── src/main/java/
│   ├── barrel/              # Lógica dos barrels
│   ├── client/              # Interface do cliente
│   ├── common/              # Classes partilhadas (Utils, Config, etc.)
│   ├── downloader/          # Workers de indexação
│   ├── gateway/             # Gateway e conexões
│   └── queue/               # Fila de URLs
├── src/main/resources/
│   └── Config.cfg           # Configurações do sistema
├── Makefile                 # Comandos de compilação e execução
├── pom.xml                  # Configuração Maven
└── README.md                # Este ficheiro
```

---

## Testes

Para testar o sistema, pode usar URLs como:
- `https://pt.wikipedia.org/wiki/Wikipédia:Página_principal`
- `https://eden.dei.uc.pt/~rbarbosa/sd/`
- Qualquer página web pública

---

##  Resolução de Problemas

### Erro: "Address already in use"
- Verifique se algum componente já está em execução na mesma porta
- Execute `make stop-all` e tente novamente

### Erro: "Connection refused"
- Certifique-se de que o Gateway está em execução antes dos outros componentes
- Verifique as configurações de host/port no `Config.properties`

### Barrels não sincronizam
- Verifique se todos os barrels estão registados no Gateway
- Confirme que as portas e hosts estão corretos

### Stop words removem palavras importantes
- O sistema aprende dinamicamente após 5000 páginas indexadas
- Só remove palavras que são outliers em múltiplos ciclos consecutivos

---

## Notas Importantes

- **Persistência:** Os índices são guardados automaticamente ao encerrar os barrels
- **Failover:** O sistema continua a funcionar mesmo se um barrel falhar
- **Escalabilidade:** Podem ser adicionados mais barrels e downloaders dinamicamente
- **Stop Words:** São descobertas de forma distribuída usando análise estatística (IQR)

---
## Javadoc

Para gerar e visualizar a documentação Javadoc do projeto:

### Gerar Javadoc

#### Usando Maven:
```bash
mvn javadoc:javadoc
```

### Para correr
```bash
cd target/reports/apidocs

- MacOS/Linux 
    open index.html

- Windows
    start index.html
```
---
## Autores

- Henrique Diz
- Rodrigo Manão
- João Francisco

---

## Licença

Projeto académico - Universidade de Coimbra

---
