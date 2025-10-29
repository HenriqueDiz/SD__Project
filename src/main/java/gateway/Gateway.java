package gateway;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import barrel.BarrelInterface;
import common.Utils;
import queue.URLQueueInterface;

public class Gateway extends UnicastRemoteObject implements GatewayInterface {
    
    private List<BarrelInterface> activeBarrels;
    private URLQueueInterface urlQueue;
    private Map<String, Integer> searchStats;
    private int currentBarrelIndex = 0;
    private Properties config;
    
    public Gateway() throws RemoteException {
        super();
        activeBarrels = new ArrayList<>();
        searchStats = new ConcurrentHashMap<>();
        config = Utils.loadConfiguration();
    }
    
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


    // Load balancer - round robin - escolher próximo barrel para consulta, indexação, etc.
    private BarrelInterface getNextBarrel() {
        if (activeBarrels.isEmpty()) {
            return null;
        }
        BarrelInterface barrel = activeBarrels.get(currentBarrelIndex);
        currentBarrelIndex = (currentBarrelIndex + 1) % activeBarrels.size();
        return barrel;
    }
    
    // Adicionar palavra ao índice (replicação em todos os barrels)
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
        
        List<String> results = searchWithFailover(word);
        
        if (results != null) {
            updateSearchStats(word);
            System.out.println("Resultado cached para '" + word + "': " + results.size() + " resultado(s)");
        }
        
        return results != null ? results : new ArrayList<>();
    }

    // Consultar barrel com failover em caso de falha
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
    
    // Adicionar URL à fila de URLs
    public void addURL(String url) throws RemoteException {
        System.out.println("Gateway: Adicionando URL '" + url + "'");
        if (urlQueue != null) {
            urlQueue.putNew(url, true); // Cliente tem prioridade
            System.out.println(Utils.green("URL adicionado à fila com prioridade"));
        } else {
            throw new RemoteException("URLQueue não disponível");
        }
    }
    
    @Override
    public void registerBarrel(String host, int port, String name) throws RemoteException {
        BarrelInterface newBarrel = GatewayConnections.registerBarrel(host, port, name, activeBarrels);
        if (newBarrel != null) {
            activeBarrels.add(newBarrel);
        } else {
            throw new RemoteException("Falha ao registrar o barrel: " + name);
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

    //Todo: Consultar barrels ativos, não está thread-safe (acho eu), ver melhor isto
    public List<String> getActiveBarrels() throws RemoteException {
        List<String> barrelInfo = new ArrayList<>();
        for (int i = 0; i < activeBarrels.size(); i++) {
            barrelInfo.add(Utils.green("Barrel " + (i + 1) + " - Ativo"));
        }
        return barrelInfo;
    }
}