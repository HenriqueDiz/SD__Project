package gateway;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

import barrel.BarrelInterface;
import queue.URLQueueInterface;

public class Gateway extends UnicastRemoteObject implements GatewayInterface {
    
    private List<BarrelInterface> activeBarrels;
    private URLQueueInterface urlQueue;
    private Map<String, List<String>> searchCache;
    private Map<String, Integer> searchStats;
    private int currentBarrelIndex = 0;
    private Properties config;
    
    public Gateway() throws RemoteException {
        super();
        activeBarrels = new ArrayList<>();
        searchCache = new ConcurrentHashMap<>();
        searchStats = new ConcurrentHashMap<>();
        loadConfiguration();
    }
    
    private void loadConfiguration() {
        config = new Properties();
        try {
            // Carregar do classpath
            InputStream configStream = getClass().getResourceAsStream("gateway.properties");
            if (configStream != null) {
                config.load(configStream);
                configStream.close();
                System.out.println("Configuração carregada: gateway.properties");
            } else {
                throw new IOException("Ficheiro gateway.properties não encontrado");
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar configuração: " + e.getMessage());
            System.err.println("Usando valores padrão");
            setDefaultConfiguration();
        }
    }
    
    private void setDefaultConfiguration() {
        config.setProperty("gateway.host", "localhost");
        config.setProperty("gateway.port", "8183");
        config.setProperty("gateway.name", "gateway");
        config.setProperty("queue.host", "localhost");
        config.setProperty("queue.port", "8181");
        config.setProperty("queue.name", "queue");
        config.setProperty("barrel1.host", "localhost");
        config.setProperty("barrel1.port", "8182");
        config.setProperty("barrel1.name", "barrel1");
        config.setProperty("barrel2.host", "localhost");
        config.setProperty("barrel2.port", "8183");
        config.setProperty("barrel2.name", "barrel2");
    }
    
    public static void main(String[] args) {
        try {
            Gateway gateway = new Gateway();
            
            // Ler configuração do Gateway
            String gatewayHost = gateway.config.getProperty("gateway.host");
            int gatewayPort = Integer.parseInt(gateway.config.getProperty("gateway.port"));
            String gatewayName = gateway.config.getProperty("gateway.name");
            
            System.out.println("Iniciando Gateway...");
            System.out.println("Host: " + gatewayHost);
            System.out.println("Porta: " + gatewayPort);
            System.out.println("Nome: " + gatewayName);
            
            // Conectar ao URLQueue
            gateway.urlQueue = GatewayConnections.connectToURLQueue(gateway.config);
            
            
            // Conectar aos Barrels
            List<BarrelInterface> barrels = GatewayConnections.connectToBarrels(gateway.config);
            if (barrels != null) gateway.activeBarrels.addAll(barrels);
            
            if (gateway.activeBarrels.isEmpty()) {
                System.err.println("Nenhum barrel conectado! Gateway não pode iniciar.");
                return;
            }
            
            // Registrar o Gateway
            Registry registry = LocateRegistry.createRegistry(gatewayPort);
            registry.rebind(gatewayName, gateway);
            
            System.out.println("Gateway registrado com sucesso!");
            System.out.println("Barrels ativos: " + gateway.activeBarrels.size());
            System.out.println("Aguardando conexões de clientes...");
            System.out.println("Use Ctrl+C para encerrar");
            
            // Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nEncerrando Gateway...");
                try {
                    registry.unbind(gatewayName);
                    System.out.println("Gateway desregistrado");
                } catch (Exception e) {
                    System.err.println("Erro durante shutdown: " + e.getMessage());
                }
                System.out.println("Gateway encerrado!");
            }));
            
            // Manter ativo
            Object lock = new Object();
            synchronized (lock) {
                lock.wait();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    // Load balancer - round robin
    private BarrelInterface getNextBarrel() {
        if (activeBarrels.isEmpty()) {
            return null;
        }
        BarrelInterface barrel = activeBarrels.get(currentBarrelIndex);
        currentBarrelIndex = (currentBarrelIndex + 1) % activeBarrels.size();
        return barrel;
    }
    
    public void addToIndex(String word, String url) throws RemoteException {
        
        List<BarrelInterface> failedBarrels = new ArrayList<>();
        int successCount = 0;
        
        // Adicionar a TODOS os barrels ativos (REPLICAÇÃO)
        for (BarrelInterface barrel : activeBarrels) {
            try {
                barrel.addToIndex(word, url);
                successCount++;
            } catch (RemoteException e) {
                System.err.println("Barrel falhou ao indexar '" + word + "': " + e.getMessage());
                failedBarrels.add(barrel);
            }
        }
        
        // Remover barrels que falharam
        if (!failedBarrels.isEmpty()) {
            activeBarrels.removeAll(failedBarrels);
            System.err.println("Removidos " + failedBarrels.size() + " barrel(s) falhado(s)");
        }
        
        if (successCount == 0) {
            throw new RemoteException("Todos os barrels falharam ao indexar '" + word + "'!");
        }
    }
    
    public List<String> searchWord(String word) throws RemoteException {
        System.out.println("Gateway: Procurando '" + word + "'");
        
        // 1. Verificar cache
        if (searchCache.containsKey(word)) {
            System.out.println("Cache HIT para '" + word + "'");
            updateSearchStats(word);
            return searchCache.get(word);
        }
        
        // 2. Cache MISS - usar qualquer barrel (todos têm a mesma info)
        System.out.println("Cache MISS para '" + word + "' - consultando barrel");
        
        List<String> results = searchWithFailover(word);
        
        // 3. Guardar no cache
        if (results != null) {
            searchCache.put(word, results);
            updateSearchStats(word);
            System.out.println("Resultado cached para '" + word + "': " + results.size() + " resultado(s)");
        }
        
        return results != null ? results : new ArrayList<>();
    }
    
    private List<String> searchWithFailover(String word) throws RemoteException {
        List<BarrelInterface> failedBarrels = new ArrayList<>();
        
        // Como todos os barrels têm a mesma info, basta um responder
        for (int attempt = 0; attempt < activeBarrels.size(); attempt++) {
            BarrelInterface barrel = getNextBarrel();
            if (barrel == null) break;
            
            try {
                List<String> results = barrel.searchWord(word);
                System.out.println("Barrel " + (attempt + 1) + " respondeu com " + results.size() + " resultado(s)");
                return results;
                
            } catch (RemoteException e) {
                System.err.println("Barrel " + (attempt + 1) + " falhou, tentando próximo...");
                failedBarrels.add(barrel);
            }
        }
        
        // Remover barrels falhados
        activeBarrels.removeAll(failedBarrels);
        
        if (activeBarrels.isEmpty()) {
            throw new RemoteException("Todos os barrels falharam!");
        }
        
        return new ArrayList<>();
    }
    
    public void addURL(String url) throws RemoteException {
        System.out.println("Gateway: Adicionando URL '" + url + "'");
        if (urlQueue != null) {
            urlQueue.putNew(url, true); // Cliente tem prioridade
            System.out.println("URL adicionado à fila com prioridade");
        } else {
            throw new RemoteException("URLQueue não disponível");
        }
    }
    
    public void registerBarrel(String host, int port, String name) throws RemoteException {
        try {
            Registry barrelRegistry = LocateRegistry.getRegistry(host, port);
            BarrelInterface barrel = (BarrelInterface) barrelRegistry.lookup(name);
            activeBarrels.add(barrel);
            System.out.println("Novo barrel registrado: " + host + ":" + port + "/" + name);
        } catch (Exception e) {
            throw new RemoteException("Erro ao registrar barrel: " + e.getMessage());
        }
    }
    
    private void updateSearchStats(String word) {
        searchStats.merge(word, 1, Integer::sum);
    }
    
    public Map<String, Integer> getTop10Searches() throws RemoteException {
        return searchStats.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .collect(LinkedHashMap::new,
                (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                LinkedHashMap::putAll);
    }
    
    public List<String> getActiveBarrels() throws RemoteException {
        List<String> barrelInfo = new ArrayList<>();
        for (int i = 0; i < activeBarrels.size(); i++) {
            barrelInfo.add("\u001B[32mBarrel " + (i + 1) + " - Ativo\u001B[0m");
        }
        return barrelInfo;
    }
    
    public int getCacheSize() throws RemoteException {
        return searchCache.size();
    }
    
    public void clearCache() throws RemoteException {
        searchCache.clear();
        System.out.println("Cache limpo pelo administrador");
    }
}