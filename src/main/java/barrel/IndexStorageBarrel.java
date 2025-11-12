package barrel;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import common.ConfigReader;
import common.Utils;
import gateway.GatewayInterface;

/**
 * Implementação do Index Storage Barrel.
 * Responsável por armazenar e gerenciar o índice invertido de palavras para URLs.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 */
public class IndexStorageBarrel extends UnicastRemoteObject implements BarrelInterface {

    /**
     * Índice invertido: palavra -> conjunto de URLs
     */
    private ConcurrentHashMap<String, HashSet<String>> indexedItems; // word -> set of urls

    /**
     * URLs indexadas e seus links associados
     */
    private ConcurrentHashMap<String, HashSet<String>> urlsIndexed; // url -> set of associated links

    /**
     * Nome, porta e host do barrel
     */
    private final String name;

    /**
     * Porta do barrel
     */
    private final int port;

    /**
     * Host do barrel
     */
    private final String host;

    /**
     * Conjunto de mensagens recebidas para evitar duplicatas
     */
    private Set<String> receivedMessages; // Para detectar duplicados

    /**
     * Gerenciador de stopwords
     */
    private BarrelStopWords stopWordsManager; 

    /**
     * Estado da Queue para backup e restauração
     */
    private BlockingDeque<String> queueBackup; // URLs pendentes da queue

    /**
     * URLs já vistas pela queue
     */
    private Set<String> queueSeenUrls; // URLs já vistas pela queue

    /**
     * Executor para retransmissão de mensagens
     */
    private final ExecutorService retransmitExecutor;

    /**
     * Construtor da classe IndexStorageBarrel.
     *
     * @param name Nome do barrel
     * @param port Porta do barrel
     * @param host Host do barrel
     * @throws RemoteException Se ocorrer um erro de comunicação remota
     */
    public IndexStorageBarrel(String name, int port, String host) throws RemoteException {
        super();
        indexedItems = new ConcurrentHashMap<>();
        urlsIndexed = new ConcurrentHashMap<>();
        stopWordsManager = new BarrelStopWords(name);
        receivedMessages = ConcurrentHashMap.newKeySet(); // Thread-safe set
        queueBackup = new LinkedBlockingDeque<>();
        queueSeenUrls = ConcurrentHashMap.newKeySet();
        retransmitExecutor = Executors.newFixedThreadPool(5);
        this.port = port;
        this.name = name;
        this.host = host;
    }

    /**
     * Obtém o nome do barrel.
     * 
     * @return Nome do barrel
     * @throws RemoteException Se ocorrer um erro de comunicação remota
     */
    @Override
    public String getName() throws RemoteException {
        return name;
    }

    /**
     * Obtém a porta do barrel.
     * 
     * @return Porta do barrel
     * @throws RemoteException Se ocorrer um erro de comunicação remota
     */
    @Override
    public int getPort() throws RemoteException {
        return port;
    }

    /**
     * Obtém o host do barrel.
     * 
     * @return Host do barrel
     * @throws RemoteException Se ocorrer um erro de comunicação remota
     */
    @Override
    public String getHost() throws RemoteException {
        return host;
    }

    /**
     * Adiciona um conjunto de URLs associados a um URL indexado
     * 
     * @param url                   URL indexado
     * @param associatedUrls        Conjunto de URLs associados
     * @throws RemoteException      Se ocorrer um erro de comunicação remota
     */
    @Override
    public synchronized void addUrlsForIndexedUrl(String url, HashSet<String> associatedUrls) throws RemoteException {
        urlsIndexed.put(url, new HashSet<>(associatedUrls));
    }

    /**
     * Verifica se um URL está indexado
     * 
     * @param url                   URL a ser verificado
     * @return                      true se o URL estiver indexado, false caso contrário
     * @throws RemoteException      Se ocorrer um erro de comunicação remota
     */
    @Override
    public boolean hasIndexedUrl(String url) throws RemoteException {
        // Verificamos direto no mapa de URLs indexadas
        if (urlsIndexed.containsKey(url)) return true;

        // Em caso negativo, verificamos no índice completo
        for (HashSet<String> urls : indexedItems.values()) {
            if (urls.contains(url)) return true;
        }
        return false;
    }

    /**
     * Obtém o conjunto de URLs associados a um URL indexado
     * 
     * @param url                   URL indexado
     * @return                      Conjunto de URLs associados
     * @throws RemoteException      Se ocorrer um erro de comunicação remota
     */
    @Override
    public synchronized HashSet<String> getUrlsForIndexedUrl(String url) throws RemoteException {
        return urlsIndexed.getOrDefault(url, new HashSet<>());
    }

    /**
     * Método principal para iniciar o Index Storage Barrel.
     * Configura o barrel, carrega o progresso se existir, registra no Gateway e aguarda conexões.
     * 
     * @param args Argumentos da linha de comando (port, name, host ou barrelNumber)
     */
    public static void main(String args[]) {
        try {
            int port;
            String name;
            String host;

            // Configuração inicial
            switch (args.length) {
                case 1 -> {
                    ConfigReader config = new ConfigReader(args[0]);
                    port = config.getPort();
                    name = config.getName();
                    host = config.getHost();
                }
                case 3 -> {
                    port = Utils.validatePort(args[0]);
                    name = Utils.validateName(args[1]);
                    host = args[2];
                }
                default -> {
                    System.out.println("Usage: java IndexStorageBarrel <port> <name> or java IndexStorageBarrel <barrelNumber>");
                    return;
                }
            }

            // Criar o Barrel
            IndexStorageBarrel barrel = new IndexStorageBarrel(name, port, host);

            // Carregar progresso completo se existir (BarrelProgress)
            if (BarrelProgress.exists(name)) {
                System.out.println(Utils.yellow("Carregando progresso do barrel: " + name));
                BarrelProgress progress = BarrelProgress.load(name);
                barrel.loadProgress(progress);
                System.out.println(Utils.green("Progresso carregado com sucesso!"));
            } else {
                System.out.println(Utils.yellow("Nenhum progresso encontrado. Criando novo barrel: " + name));
                barrel.copyAllData(); // Se não exitir progresso, copia tudo
            }

            System.setProperty("java.rmi.server.hostname", host);

            // Criar Registry
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind(name, barrel);
            
            System.out.println("=".repeat(50));
            System.out.println("Index Storage Barrel iniciado:");
            System.out.println("Porta: " + port);
            System.out.println("Nome: " + name);
            System.out.println("Host: " + host);
            System.out.println("=".repeat(50));
            System.out.println("Aguardando conexões...");
            System.out.println("Use Ctrl+C para encerrar");

            // Registrar no Gateway
            ConfigReader gatewayConfig = new ConfigReader("gateway");
            String gatewayHost = gatewayConfig.getHost();
            int gatewayPort = gatewayConfig.getPort();
            String gatewayName = gatewayConfig.getName();

            Registry gatewayRegistry = LocateRegistry.getRegistry(gatewayHost, gatewayPort);
            GatewayInterface gateway = (GatewayInterface) gatewayRegistry.lookup(gatewayName);
            
            gateway.registerBarrel(host, port, name);
            System.out.println(Utils.green("Barrel registrado no Gateway!"));

            // Shutdown Hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println(Utils.yellow("Encerrando Barrel... Salvando progresso completo."));
                try {

                    // Desligar o executor de retransmissão
                    barrel.retransmitExecutor.shutdown();

                    BarrelProgress.save(name, barrel.getProgress());
                } catch (Exception e) {
                    Utils.printLogException("Failed to save barrel progress on shutdown (" + name + "): " + e.getMessage(), e);
                }
                System.out.println(Utils.green("Progresso salvo com sucesso!"));
            }));

            Object lock = new Object();
            synchronized (lock) {
                lock.wait();
            }

        } catch (Exception e) {
            Utils.printLogException("Erro no Index Storage Barrel: " + e.getMessage(), e);
        }
    }


    /**
     * Retransmite a adição de uma palavra e sua URL associada para outros barrels ativos.
     * 
     * @param word                  Progresso do barrel a ser carregado
     * @param url                   URL associada à palavra
     * @throws RemoteException      Se ocorrer um erro de comunicação remota
     */
    private void retransmitToOtherBarrels(String word, String url) {
        try {
            // Obter lista de barrels do gateway
            ConfigReader gatewayConfig = new ConfigReader("gateway");
            Registry gatewayRegistry = LocateRegistry.getRegistry(
                gatewayConfig.getHost(), 
                gatewayConfig.getPort()
            );
            GatewayInterface gateway = (GatewayInterface) gatewayRegistry.lookup(gatewayConfig.getName());
            List<String> activeBarrels = gateway.getActiveBarrels();
            
            for (String barrelInfo : activeBarrels) {
                String[] parts = barrelInfo.split(":");
                String barrelName = parts[0];
                int barrelPort = Integer.parseInt(parts[1]);
                String barrelHost = parts[2];

                // Não retransmitir para si mesmo
                if (barrelName.equals(this.name) && barrelPort == this.port && barrelHost.equals(this.host)) {
                    continue;
                }
                
                try {
                    Registry barrelRegistry = LocateRegistry.getRegistry(barrelHost, barrelPort);
                    BarrelInterface otherBarrel = (BarrelInterface) barrelRegistry.lookup(barrelName);
                    otherBarrel.addToIndex(word, url,false); // Retransmite
                } catch (Exception e) {
                   Utils.printLogException("Erro ao retransmitir para o barrel " + barrelName + ": " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            Utils.printLogException("Erro ao obter lista de barrels do gateway: " + e.getMessage(), e);
        }
    }

    


    /**
     * Adiciona uma palavra e sua URL associada ao índice. 
     * Verifica se a palavra já existe no índice, e atualiza o conjunto de URLs conforme necessário.
     *
     * @param word                  Palavra a ser adicionada
     * @param url                   URL associada à palavra
     * @throws RemoteException      Se ocorrer um erro de comunicação remota
     */
    @Override
    public synchronized void addToIndex(String word, String url, boolean shouldRetransmit) throws java.rmi.RemoteException {

        String messageId = word + ":" + url; // ID único da mensagem
    
        // Verificar se já recebeu esta mensagem
        if (receivedMessages.contains(messageId)) {
            return; // Duplicado, ignorar
        }
        
        // Marcar como recebida
        receivedMessages.add(messageId);

        if (stopWordsManager.isStopword(word)) {
            return; // Ignorar stopwords
        }

        if (indexedItems.containsKey(word)){
            HashSet<String> urlsForWord = indexedItems.get(word);
            urlsForWord.add(url);
            indexedItems.put(word, urlsForWord);
        } else {
            HashSet<String> newUrlsForWord = new HashSet<String>();
            newUrlsForWord.add(url);
            indexedItems.put(word, newUrlsForWord);
        }

        // RETRANSMITIR de forma assíncrona (fora do synchronized)
        if (shouldRetransmit) {
            retransmitExecutor.submit(() -> retransmitToOtherBarrels(word, url));
        }
    }


    /**
     * Procura por uma palavra no índice e retorna a lista de URLs associados.
     * 
     * @param word                      Palavra a ser pesquisada
     * @return                          Lista de URLs associadas à palavra
     * @throws RemoteException          Se ocorrer um erro de comunicação remota
     */
    @Override
    public List<String> searchWord(String word) throws java.rmi.RemoteException {
        System.out.println("Procurando por " + word);
        HashSet<String> urls = indexedItems.get(word);
        if (urls != null && !urls.isEmpty()) {
            ArrayList<String> resultadoPesquisa = new ArrayList<>(urls);
            System.out.println(Utils.green("Encontrados " + resultadoPesquisa.size() + " resultados para '" + word + "'. Enviados ao cliente."));
            return resultadoPesquisa;
        } else {
            System.out.println(Utils.yellow("Nenhum resultado encontrado para '" + word + "'."));
            return new ArrayList<>();
        }
    }

    /**
     * Sincroniza o índice com novos dados recebidos, quando um barrel é adicionado/restaurado.
     * Verifica cada entrada no mapa de novos índices e atualiza o índice existente conforme necessário.
     * 
     * @param newIndexes                Mapa de novos índices a serem sincronizados
     * @throws RemoteException          Se ocorrer um erro de comunicação remota
     */
    @Override
    public synchronized void syncIndex(Map<String, HashSet<String>> newIndexes) throws RemoteException {
        for (Map.Entry<String, HashSet<String>> entry : newIndexes.entrySet()) {
            String word = entry.getKey();
            HashSet<String> urls = entry.getValue();

            if (!indexedItems.containsKey(word)) {
                indexedItems.put(word, new HashSet<>(urls));
            } else {
                indexedItems.get(word).addAll(urls); // Adicionar URLs novos
            }
        }
        System.out.println(Utils.green("Índices sincronizados com sucesso!"));
    }

    /**
     * Obtém o índice atual do barrel.
     * 
     * @return                          Mapa do índice atual
     * @throws RemoteException          Se ocorrer um erro de comunicação remota
     */
    @Override
    public synchronized Map<String, HashSet<String>> getIndex() throws RemoteException {
        return new HashMap<>(indexedItems); // Retorna uma cópia do índice atual
    }

    /**
     * Obtém o tamanho do índice, ou seja, o número de palavras indexadas.
     * 
     * @return                          Tamanho do índice
     * @throws RemoteException          Se ocorrer um erro de comunicação remota
     */
    @Override
    public int getIndexSize() throws RemoteException {
        return indexedItems.size();
    }

    /**
     * Obtém as contagens de links inbound para cada URL indexada.
     * 
     * @return                          Mapa de URLs e suas contagens de links inbound
     * @throws RemoteException          Se ocorrer um erro de comunicação remota
     */
    @Override
    public Map<String, Integer> getInboundLinkCounts() throws RemoteException {
        Map<String, Integer> inboundCounts = new HashMap<>();
        for (HashSet<String> links : urlsIndexed.values()) {
            for (String link : links) {
                inboundCounts.merge(link, 1, Integer::sum);
            }
        }
        return inboundCounts;
    }


    /**
     * Adiciona contagens de palavras para uma URL específica.
     * 
     * @param wordCounts                Mapa de palavras e suas contagens
     * @param url                       URL associada às contagens
     * @throws RemoteException          Se ocorrer um erro de comunicação remota
     */
    @Override
    public void addWordCounts(Map<String, Integer> wordCounts, String url) throws RemoteException {
        stopWordsManager.addWordCounts(wordCounts, url);
    }
    
    /**
     * Verifica se uma palavra é uma stopword.
     * 
     * @param palavra                   Palavra a ser verificada
     * @return                          true se for uma stopword, false caso contrário
     * @throws RemoteException          Se ocorrer um erro de comunicação remota
     */
    @Override
    public boolean isStopword(String palavra) throws RemoteException {
        return stopWordsManager.isStopword(palavra);
    }
    
    /**
     * Obtém a lista de stopwords gerenciadas pelo Barrel.
     * 
     * @return                          Lista de stopwords
     */
    public List<String> getStopwords() {
        return stopWordsManager.getStopwords();
    }


    // <<<<<<<<<<<<<<<< Barrel Progress Methods >>>>>>>>>>>>>>

     /**
     * Salva o estado da Queue quando ela morre
     * 
     * @param pendingUrls               Fila de URLs pendentes
     * @param seenUrls                  Conjunto de URLs já vistas
     * @throws RemoteException          Se ocorrer um erro de comunicação remota
     */
    @Override
    public synchronized void backupQueueState(Queue<String> pendingUrls, Set<String> seenUrls) throws RemoteException {
        this.queueBackup = new LinkedBlockingDeque<>(pendingUrls);
        this.queueSeenUrls = new HashSet<>(seenUrls);
        
        System.out.println("┌" + "─".repeat(50) + "┐");
        System.out.println("│" + Utils.bold(" BACKUP DA QUEUE RECEBIDO") + " ".repeat(25) + "│");
        System.out.println("├" + "─".repeat(50) + "┤");
        System.out.println("│ URLs pendentes: " + Utils.bold(String.valueOf(pendingUrls.size())) + " ".repeat(33 - String.valueOf(pendingUrls.size()).length()) + "│");
        System.out.println("│ URLs vistas: " + Utils.bold(String.valueOf(seenUrls.size())) + " ".repeat(36 - String.valueOf(seenUrls.size()).length()) + "│");
        System.out.println("└" + "─".repeat(50) + "┘");
        
        // Salvar imediatamente no disco
        BarrelProgress.save(name, getProgress());
    }

    /**
     * Restaura o estado da Queue a partir do backup
     * 
     * @return                          Mapa com o estado da Queue (pendingUrls e seenUrls)
     * @throws RemoteException          Se ocorrer um erro de comunicação remota
     */
    @Override
    public synchronized Map<String, Object> restoreQueueState() throws RemoteException {
        Map<String, Object> queueState = new HashMap<>();
        queueState.put("pendingUrls", new LinkedList<>(queueBackup));
        queueState.put("seenUrls", new HashSet<>(queueSeenUrls));
        
        System.out.println(Utils.green("Estado da Queue restaurado do backup!"));
        System.out.println("URLs pendentes restauradas: " + queueBackup.size());
        System.out.println("URLs vistas restauradas: " + queueSeenUrls.size());
        
        return queueState;
    }

    /**
     * Obtém o progresso atual do barrel, incluindo itens indexados e estado da queue.
     * 
     * @return                          Objeto BarrelProgress com o estado atual
     */
    public synchronized BarrelProgress getProgress() {
        Map<String, HashSet<String>> indexedItemsSnap = new HashMap<>();
        for (Map.Entry<String, HashSet<String>> e : indexedItems.entrySet()) {
            indexedItemsSnap.put(e.getKey(), new HashSet<>(e.getValue()));
        }

        Map<String, HashSet<String>> urlsIndexedSnap = new HashMap<>();
        for (Map.Entry<String, HashSet<String>> e : urlsIndexed.entrySet()) {
            urlsIndexedSnap.put(e.getKey(), new HashSet<>(e.getValue()));
        }

        return new BarrelProgress(indexedItemsSnap, urlsIndexedSnap, queueBackup, queueSeenUrls);
    }

    /**
     * Carrega o progresso do barrel a partir de um objeto BarrelProgress.
     * 
     * @param progress                  Objeto BarrelProgress com o estado a ser carregado
     */
    public synchronized void loadProgress(BarrelProgress progress) {
        if (progress == null) return;

        this.indexedItems = new ConcurrentHashMap<>();
        for (Map.Entry<String, HashSet<String>> e : progress.getIndexedItems().entrySet()) {
            this.indexedItems.put(e.getKey(), new HashSet<>(e.getValue()));
        }

        this.urlsIndexed = new ConcurrentHashMap<>();
        for (Map.Entry<String, HashSet<String>> e : progress.getUrlsIndexed().entrySet()) {
            this.urlsIndexed.put(e.getKey(), new HashSet<>(e.getValue()));
        }

        this.queueBackup = new LinkedBlockingDeque<>(progress.getUrlsToIndex());
        this.queueSeenUrls = new HashSet<>(progress.getSeenUrls());

        System.out.println(Utils.green("Queue backup carregado:"));
        System.out.println("  - URLs pendentes: " + queueBackup.size());
        System.out.println("  - URLs vistas: " + queueSeenUrls.size());
    }

    /**
     * Copia dados (índices) de outros barrels ativos ao iniciar quando não há progresso local.
     * Se não conseguir contatar o Gateway ou outros barrels, falha de forma silenciosa deixando o barrel vazio.
     */
    public synchronized void copyAllData() {
        try {
            // Obter lista de barrels via Gateway
            ConfigReader gatewayConfig = new ConfigReader("gateway");
            Registry gatewayRegistry = LocateRegistry.getRegistry(gatewayConfig.getHost(), gatewayConfig.getPort());
            GatewayInterface gateway = (GatewayInterface) gatewayRegistry.lookup(gatewayConfig.getName());
            List<String> activeBarrels = gateway.getActiveBarrels();

            for (String barrelInfo : activeBarrels) {
                try {
                    String[] parts = barrelInfo.split(":");
                    String barrelName = parts[0];
                    int barrelPort = Integer.parseInt(parts[1]);
                    String barrelHost = parts[2];

                    // Não copiar de si mesmo
                    if (barrelName.equals(this.name) && barrelPort == this.port && barrelHost.equals(this.host)) {
                        continue;
                    }

                    try {
                        Registry otherRegistry = LocateRegistry.getRegistry(barrelHost, barrelPort);
                        BarrelInterface otherBarrel = (BarrelInterface) otherRegistry.lookup(barrelName);

                        // Obter índice do outro barrel e sincronizar
                        Map<String, HashSet<String>> otherIndex = otherBarrel.getIndex();
                        if (otherIndex != null && !otherIndex.isEmpty()) {
                            this.syncIndex(otherIndex);
                            System.out.println(Utils.green("Índice copiado do barrel: " + barrelName));
                        }
                    } catch (Exception e) {
                        Utils.printLogException("Erro ao copiar dados do barrel " + barrelName + ": " + e.getMessage(), e);
                    }
                } catch (Exception e) {
                    Utils.printLogException("Formato inválido de barrel recebido do Gateway: " + barrelInfo + " (" + e.getMessage() + ")", e);
                }
            }
        } catch (Exception e) {
            // Se não for possível contactar o gateway ou obter barrels, apenas logamos e seguimos com índice vazio.
            Utils.printLogException("Não foi possível copiar dados iniciais via Gateway: " + e.getMessage(), e);
        }
    }
}