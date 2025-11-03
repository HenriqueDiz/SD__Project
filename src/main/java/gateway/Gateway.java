package gateway;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import barrel.BarrelInterface;
import common.Utils;
import queue.URLQueueInterface;

/**
 * Implementação do Gateway que gerencia a comunicação entre clientes e barrels.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class Gateway extends UnicastRemoteObject implements GatewayInterface {

    /**
     * A lista de barrels ativos.
     */
    private List<BarrelInterface> activeBarrels;

    /**
     * A lista de barrels registrados.
     */
    private List<BarrelInterface> barrelsRegisters;

    /**
     * A instância remota do URL Queue.
     */
    private URLQueueInterface urlQueue;

    /**
     * Mapa de informações dos barrels registrados (nome -> porta).
     */
    private Map<String, Integer> registeredBarrelInfo;

    /**
     * Mapa de estatísticas de busca (palavra -> contagem).
     */
    private Map<String, Integer> searchStats;

    /**
     * Índice do barrel atual para balanceamento de carga.
     */
    private int currentBarrelIndex = 0;

    /**
     * Propriedades de configuração do Gateway.
     */
    private Properties config;

    /**
     * Mapa de tempos totais de processamento dos barrels (nome -> tempo em nanos).
     */
    private final Map<String, Long> barrelTotalNanos = new ConcurrentHashMap<>();

    /**
     * Mapa de contagem de requisições dos barrels (nome -> contagem).
     */
    private final Map<String, Long> barrelCount = new ConcurrentHashMap<>();
    
    /**
     * Construtor do Gateway.
     * 
     * @throws RemoteException Se ocorrer um erro remoto.
     */
    public Gateway() throws RemoteException {
        super();
        activeBarrels = new ArrayList<>();
        barrelsRegisters = new ArrayList<>();
        registeredBarrelInfo = new ConcurrentHashMap<>();
        searchStats = new ConcurrentHashMap<>();
        config = Utils.loadConfiguration();
    }
    
    /**
     * Método principal para iniciar o Gateway.
     * Configura o Gateway, conecta-se ao URLQueue e aguarda conexões de clientes.
     * 
     * @param args Argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        try {
            Gateway gateway = new Gateway();
            
            String gatewayHost = gateway.config.getProperty("gateway.host");
            int gatewayPort = Integer.parseInt(gateway.config.getProperty("gateway.port"));
            String gatewayName = gateway.config.getProperty("gateway.name");
            
            System.out.println("Iniciando Gateway...");
            System.out.println("Host: " + gatewayHost);
            System.out.println("Porta: " + gatewayPort);
            System.out.println("Nome: " + gatewayName);
            
            // Conectar ao URLQueue
            gateway.urlQueue = GatewayConnections.connectToURLQueue(gateway.config);
            // Registrar o Gateway
            Registry registry = LocateRegistry.createRegistry(gatewayPort);
            registry.rebind(gatewayName, gateway);
            
            System.out.println("Gateway registrado com sucesso!");
            System.out.println("Barrels ativos: " + gateway.activeBarrels.size());
            System.out.println("Aguardando conexões de clientes...");
            System.out.println("Use Ctrl+C para encerrar");
            
            // Shutdown hook para limpeza
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nEncerrando Gateway...");
                try {
                    registry.unbind(gatewayName);
                    System.out.println("Gateway desregistrado");
                } catch (Exception e) {
                    Utils.printLogException("Erro ao desregistrar o Gateway", e);
                }
                System.out.println("Gateway encerrado!");
            }));
            
            // Manter ativo
            Object lock = new Object();
            synchronized (lock) {
                lock.wait();
            }
            
        } catch (Exception e) {
            Utils.printLogException("Erro ao iniciar o Gateway", e);
        }
    }

    /**
     * Obtém o próximo barrel ativo em round-robin.
     * 
     * @return O próximo BarrelInterface ou null se não houver barrels ativos.
     */
    private BarrelInterface getNextBarrel() {
        if (activeBarrels.isEmpty()) {
            return null;
        }
        BarrelInterface barrel = activeBarrels.get(currentBarrelIndex);
        currentBarrelIndex = (currentBarrelIndex + 1) % activeBarrels.size();
        return barrel;
    }
    
    /**
     * Método de busca de palavra no Gateway.
     * 
     * @param word              A palavra a ser buscada.
     * @return                  Lista de URLs que contêm a palavra.
     * @throws RemoteException  Se ocorrer um erro remoto.
     */  
    @Override
    public List<String> searchWordGateway(String word) throws RemoteException {
        System.out.println("Gateway: Procurando '" + word + "'");
        
        List<String> results = searchWithFailover(word);
        
        if (results != null) {
            updateSearchStats(word);
            System.out.println("Resultado para '" + word + "': " + results.size() + " resultado(s)");
        }
        
        return results != null ? results : new ArrayList<>();
    }

    /**
     * Realiza a busca com failover entre os barrels ativos.
     * 
     * @param word              A palavra a ser buscada.
     * @return                  Lista de URLs que contêm a palavra.
     * @throws RemoteException  Se todos os barrels falharem.
     */
   private List<String> searchWithFailover(String word) throws RemoteException {
        List<BarrelInterface> activeBarrelsCopy = new ArrayList<>(activeBarrels);
        
        String normalized = word == null ? "" : word.toLowerCase();

        for (int attempt = 0; attempt < activeBarrelsCopy.size(); attempt++) {
            BarrelInterface barrel = getNextBarrel();
            if (barrel == null) break;
            
            try {
                long start = System.nanoTime();
                List<String> results = barrel.searchWord(normalized);
                long elapsed = System.nanoTime() - start;
                recordResponseTime(barrel, elapsed);
                System.out.println("Barrel " + (attempt + 1) + " respondeu com " + results.size() + " resultado(s)");
                return results;
                
            } catch (RemoteException e) {
                try {
                    String barrelName = barrel.getName();
                    int barrelPort = barrel.getPort();
                    System.err.println("Barrel " + barrelName + ":" + barrelPort + " falhou, tentando próximo...");
                } catch (RemoteException ex) {
                    System.err.println("Barrel " + (attempt + 1) + " falhou, tentando próximo...");
                    // Remove pela referência se não conseguir obter nome/porta
                    activeBarrels.remove(barrel);
                }
            }
        }
        
        if (activeBarrels.isEmpty()) {
            throw new RemoteException("Todos os barrels falharam!");
        }
        
        return new ArrayList<>();
    }


    /**
     * Método de busca de múltiplas palavras no Gateway.
     * 
     * @param words             A lista de palavras a serem buscadas.
     * @return                  Lista de arrays contendo URLs e contagens de inbound links.
     * @throws RemoteException  Se ocorrer um erro remoto.
     */
    @Override
    public List<String[]> searchWords(List<String> words) throws RemoteException {
        if (words == null || words.isEmpty()) return new ArrayList<>();

        // Normalizar palavras de pesquisa
        List<String> norm = new ArrayList<>();
        for (String w : words) {
            if (w != null && !w.isBlank()) norm.add(w.toLowerCase());
        }
        if (norm.isEmpty()) return new ArrayList<>();

        List<HashSet<String>> resultsPerWord = new ArrayList<>();
        for (String w : norm) {
            List<String> result = searchWordGateway(w);
            resultsPerWord.add(new HashSet<>(result));
        }

        // Interseção
        HashSet<String> intersection = new HashSet<>(resultsPerWord.get(0));
        for (int i = 1; i < resultsPerWord.size(); i++) {
            intersection.retainAll(resultsPerWord.get(i));
            if (intersection.isEmpty()) break;
        }
        if (intersection.isEmpty()) return new ArrayList<>();

        // Recolher contagens inbound (0 por omissão)
        Map<String, Integer> inbound = new HashMap<>();
        for (BarrelInterface barrel : new ArrayList<>(activeBarrels)) {
            try {
                Map<String, Integer> m = barrel.getInboundLinkCounts();
                if (m != null) {
                    for (Map.Entry<String, Integer> e : m.entrySet()) {
                        inbound.merge(e.getKey(), e.getValue(), Integer::sum);
                    }
                }
            } catch (RemoteException ignore) {}
        }

        // Construir e ordenar
        List<String[]> ranked = new ArrayList<>();
        for (String url : intersection) {
            int refs = inbound.getOrDefault(url, 0);
            ranked.add(new String[]{url, String.valueOf(refs)});
        }
        ranked.sort((a, b) -> Integer.compare(Integer.parseInt(b[1]), Integer.parseInt(a[1])));
        return ranked;
    }
        
    /**
     * Método para adicionar uma URL ao Gateway.
     * 
     * @param url               A URL a ser adicionada.
     * @param indexAnyway      Se true, força a reindexação mesmo que já esteja indexada.
     * @return                  True se a URL já estava indexada, false se foi adicionada
     * @throws RemoteException  Se ocorrer um erro remoto.
     */
    @Override
    public synchronized boolean addURL(String url, boolean indexAnyway) throws RemoteException {
        if (url == null || url.isBlank()) {
            throw new RemoteException("URL inválido");
        }

        System.out.println("Gateway: Tentando adicionar URL '" + url + "'" + (indexAnyway ? " [reindex=true]" : ""));

        if (urlQueue == null) {
            throw new RemoteException("URLQueue não disponível");
        }

        // Se não for para forçar, só adiciona se ainda não estiver indexado
        if (!indexAnyway) {
            if (isUrlIndexedAcrossBarrels(url)) {
                System.out.println(Utils.yellow("URL já indexado. Não será adicionado à fila: ") + url);
                return true; // já visto
            }
        } else {
            System.out.println(Utils.yellow("Forçando reindexação do URL: ") + url);
        }

        // Enfileirar (prioridade true para processamento preferencial)
        urlQueue.putNew(url, true);
        System.out.println(Utils.green("URL adicionado à fila" + (indexAnyway ? " para reindexação" : "")));
        return false;
    }

    /**
     * Verifica se uma URL já está indexada em qualquer barrel ativo.
     * 
     * @param url   A URL a ser verificada.
     * @return      True se a URL estiver indexada, false caso contrário.
     */
    private boolean isUrlIndexedAcrossBarrels(String url) {
        if (url == null || url.isBlank()) return false;

        HashSet<String> seen = new HashSet<>();
        for (BarrelInterface barrel : activeBarrels) {
            String key = "unknown";
            try {
                key = barrel.getName() + ":" + barrel.getPort();
                if (!seen.add(key)) continue;
                if (barrel.hasIndexedUrl(url)) return true;
            } catch (Exception e) {
                Utils.printLogException("Erro ao verificar URL ('" + url + "') no barrel " + key, e);
            }
        }
    return false;
}
    
    /**
     * Método para registrar um barrel no Gateway.
     * 
     * @param host              O host do barrel.
     * @param port              A porta do barrel.
     * @param name              O nome do barrel.
     * @throws RemoteException  Se ocorrer um erro remoto.
     */
    @Override
    public synchronized void registerBarrel(String host, int port, String name) throws RemoteException {
        GatewayConnections.registerBarrel(host, port, name, activeBarrels, barrelsRegisters, registeredBarrelInfo);
        notifyAll(); // Retomar os downloaders
    }


    // __________________ STATISTICS _______________________ //


    /**
     * Atualiza as estatísticas de busca para uma palavra.
     * 
     * @param word  A palavra buscada.
     */
    private void updateSearchStats(String word) {
        searchStats.merge(word, 1, Integer::sum);
    }
    
    /**
     * Obtém as 10 palavras mais buscadas.
     * 
     * @return                  Mapa das 10 palavras mais buscadas e suas contagens.
     * @throws RemoteException  Se ocorrer um erro remoto.
     */
    @Override
    public Map<String, Integer> getTop10Searches() throws RemoteException {
        return searchStats.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .collect(LinkedHashMap::new,
                (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                LinkedHashMap::putAll);
    }

    /**
     * Obtém a lista de barrels ativos.
     * 
     * @return                  Lista de strings com informações dos barrels ativos.
     * @throws RemoteException  Se ocorrer um erro remoto.
     */
    @Override
    public synchronized List<String> getActiveBarrels() throws RemoteException {
        List<String> barrelInfo = new ArrayList<>();

        // Usar iterator para evitar ConcurrentModificationException
        Iterator<BarrelInterface> iterator = activeBarrels.iterator();
        while (iterator.hasNext()) {
            BarrelInterface barrel = iterator.next();
            try {
                String barrelName = barrel.getName();
                int barrelPort = barrel.getPort();
                String barrelHost = barrel.getHost();
                int indexSize = barrel.getIndexSize();
                barrelInfo.add(barrelName + ":" + barrelPort + ":" + barrelHost + ":" + indexSize);
            } catch (RemoteException e) {
                System.err.println("Erro ao obter informações do barrel ativo: " + e.getMessage());
                iterator.remove(); // Remove apenas da lista de ativos
            }
        }
        return barrelInfo;
    }

    /**
     * Obtém a lista de barrels registrados.
     * 
     * @return                  Lista de strings com informações dos barrels registrados.
     * @throws RemoteException  Se ocorrer um erro remoto.
     */
    @Override
    public synchronized List<String> getRegisteredBarrels() throws RemoteException {
        List<String> barrelInfo = new ArrayList<>();

        // Usar o mapa de informações registradas como fonte principal
        for (Map.Entry<String, Integer> entry : registeredBarrelInfo.entrySet()) {
            String name = entry.getKey();
            Integer port = entry.getValue();
            // Procura o host correspondente na lista de barrelsRegisters
            String host = barrelsRegisters.stream()
                .filter(b -> {
                    try {
                        return b.getName().equals(name) && b.getPort() == port;
                    } catch (RemoteException e) {
                        return false;
                    }
                })
                .findFirst()
                .map(b -> {
                    try {
                        return b.getHost();
                    } catch (RemoteException e) {
                        return "unknown_host";
                    }
                })
                .orElse("unknown_host");
            barrelInfo.add(name + ":" + port + ":" + host);
        }

        // Se o mapa estiver vazio, usar a lista como fallback
        if (barrelInfo.isEmpty()) {
            for (BarrelInterface barrel : barrelsRegisters) {
                try {
                    String barrelName = barrel.getName();
                    int barrelPort = barrel.getPort();
                    String barrelHost = barrel.getHost();
                    barrelInfo.add(barrelName + ":" + barrelPort + ":" + barrelHost);
                } catch (RemoteException e) {
                    System.err.println("Erro ao obter informações do barrel registrado: " + e.getMessage());
                }
            }
        }

        return barrelInfo;
    }

    /**
     * Registra o tempo de resposta de um barrel.
     * 
     * @param barrel            O barrel que respondeu.
     * @param durationNanos     A duração da resposta em nanossegundos.
     */
    private void recordResponseTime(BarrelInterface barrel, long durationNanos) {
        String key;
        try {
            key = barrel.getName() + ":" + barrel.getPort();
        } catch (RemoteException e) {
            key = "unknown_barrel";
            Utils.printLogException("Erro ao obter nome/porta do barrel para estatísticas de tempo de resposta", e);
        }
        barrelTotalNanos.merge(key, durationNanos, Long::sum);
        barrelCount.merge(key, 1L, Long::sum);
    }

    /**
     * Obtém o tempo médio de resposta dos barrels.
     * 
     * @return                  Mapa de nomes de barrels para tempos médios de resposta em nanos.
     * @throws RemoteException  Se ocorrer um erro remoto.
     */
    @Override
    public synchronized Map<String, Long> getAverageResponseTime() throws RemoteException {
        Map<String, Long> averages = new LinkedHashMap<>();
        barrelCount.keySet().stream()
            .sorted()
            .forEach(key -> {
                long count = barrelCount.getOrDefault(key, 0L);
                long total = barrelTotalNanos.getOrDefault(key, 0L);
                if (count > 0) {
                    long avgNanos = total / count;
                    averages.put(key, avgNanos);
                }
            });
        return averages;
    }
    
    /**
     *  Obtém os URLs associados a um URL indexado.
     * @param url               O URL a ser pesquisado.
     * @return                  Lista de URLs associados.
     * @throws RemoteException  Se ocorrer um erro remoto.
    */
    @Override
    public synchronized HashSet<String> getUrlsForIndexedUrl(String url) throws RemoteException {
        List<BarrelInterface> activeBarrelsCopy = new ArrayList<>(activeBarrels);
        HashSet<String> aggregatedUrls = new HashSet<>();
        for (int attempt = 0; attempt < activeBarrelsCopy.size(); attempt++) {
            BarrelInterface barrel = getNextBarrel();
            if (barrel == null) break;
            try {
                HashSet<String> urls = barrel.getUrlsForIndexedUrl(url);
                aggregatedUrls.addAll(urls);
            } catch (RemoteException e) {
                try {
                    String barrelName = barrel.getName();
                    int barrelPort = barrel.getPort();
                    System.err.println("Barrel " + barrelName + ":" + barrelPort + " falhou ao obter URLs, tentando próximo...");
                } catch (RemoteException ex) {
                    System.err.println("Barrel " + (attempt + 1) + " falhou ao obter URLs, tentando próximo...");
                    activeBarrels.remove(barrel);
                }
            }

        }
        return aggregatedUrls;
    }
}