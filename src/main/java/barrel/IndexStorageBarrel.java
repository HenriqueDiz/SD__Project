package barrel;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private ConcurrentHashMap<String, HashSet<String>> indexedItems; // word -> set of urls
    private ConcurrentHashMap<String, HashSet<String>> urlsIndexed; // url -> set of associated links
    private final String name;
    private final int port;
    private final String host;
    private BarrelStopWords stopWordsManager; 


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
                    BarrelProgress.save(name, barrel.getProgress());
                } catch (Exception e) {
                    Utils.printLogException("Failed to save barrel progress", e);
                }
                System.out.println(Utils.green("Progresso salvo com sucesso!"));
            }));

            Object lock = new Object();
            synchronized (lock) {
                lock.wait();
            }

        } catch (Exception e) {
            Utils.printLogException("Erro no Index Storage Barrel", e);
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
    public synchronized void addToIndex(String word, String url) throws java.rmi.RemoteException {

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
        System.out.println(Utils.yellow("Resultados da palavra " + word + " dados ao cliente"));
        if(indexedItems.containsKey(word)){
            ArrayList<String> resultadoPesquisa = new ArrayList<String>(indexedItems.get(word));
            return resultadoPesquisa;
        }
        return new ArrayList<String>();
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

    public synchronized BarrelProgress getProgress() {
        Map<String, HashSet<String>> indexedItemsSnap = new HashMap<>();
        for (Map.Entry<String, HashSet<String>> e : indexedItems.entrySet()) {
            indexedItemsSnap.put(e.getKey(), new HashSet<>(e.getValue()));
        }

        Map<String, HashSet<String>> urlsIndexedSnap = new HashMap<>();
        for (Map.Entry<String, HashSet<String>> e : urlsIndexed.entrySet()) {
            urlsIndexedSnap.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        return new BarrelProgress(indexedItemsSnap, urlsIndexedSnap);
    }

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
    }
}