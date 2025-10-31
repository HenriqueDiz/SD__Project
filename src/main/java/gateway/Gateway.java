package gateway;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
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
    private List<BarrelInterface> barrelsRegisters;
    private URLQueueInterface urlQueue;
    private Map<String, Integer> registeredBarrelInfo;
    private Map<String, Integer> searchStats;
    private int currentBarrelIndex = 0;
    private Properties config;
    
    private final Map<String, Long> barrelTotalNanos = new ConcurrentHashMap<>();
    private final Map<String, Long> barrelCount = new ConcurrentHashMap<>();
    
    public Gateway() throws RemoteException {
        super();
        activeBarrels = new ArrayList<>();
        barrelsRegisters = new ArrayList<>();
        registeredBarrelInfo = new ConcurrentHashMap<>();
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
    
    
    public List<String> searchWord(String word) throws RemoteException {
        System.out.println("Gateway: Procurando '" + word + "'");
        
        List<String> results = searchWithFailover(word);
        
        if (results != null) {
            updateSearchStats(word);
            System.out.println("Resultado para '" + word + "': " + results.size() + " resultado(s)");
        }
        
        return results != null ? results : new ArrayList<>();
    }

    // Consultar barrel com failover em caso de falha
   private List<String> searchWithFailover(String word) throws RemoteException {
        List<BarrelInterface> activeBarrelsCopy = new ArrayList<>(activeBarrels);
        
        for (int attempt = 0; attempt < activeBarrelsCopy.size(); attempt++) {
            BarrelInterface barrel = getNextBarrel();
            if (barrel == null) break;
            
            try {
                long start = System.nanoTime();
                List<String> results = barrel.searchWord(word);
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
    public synchronized void registerBarrel(String host, int port, String name) throws RemoteException {
        GatewayConnections.registerBarrel(host, port, name, activeBarrels, barrelsRegisters, registeredBarrelInfo);
        notifyAll(); // Retomar os downloaders
    }

    @Override
    public synchronized boolean isBarrelRegistered(String name, int port) throws RemoteException {
        // Verificar primeiro no mapa de informações (mais confiável)
        Integer registeredPort = registeredBarrelInfo.get(name);
        if (registeredPort != null && registeredPort == port) {
            return true;
        }
        
        // Verificar na lista como backup
        return barrelsRegisters.stream()
            .anyMatch(barrel -> {
                try {
                    return barrel.getName().equals(name) && barrel.getPort() == port;
                } catch (RemoteException e) {
                    return false;
                }
            });
    }

    // __________________ STATISTICS _______________________ //


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

    @Override
    public synchronized List<String> getActiveBarrels() throws RemoteException {
        List<String> barrelInfo = new ArrayList<>();
        List<String> failedBarrelNames = new ArrayList<>();

        // Usar iterator para evitar ConcurrentModificationException
        Iterator<BarrelInterface> iterator = activeBarrels.iterator();
        while (iterator.hasNext()) {
            BarrelInterface barrel = iterator.next();
            try {
                String barrelName = barrel.getName();
                int barrelPort = barrel.getPort();
                long indexSize = barrel.getIndexSize(); // todo: Passar isto
                barrelInfo.add(barrelName + ":" + barrelPort);
            } catch (RemoteException e) {
                try {
                    String failedName = barrel.getName() + ":" + barrel.getPort();
                    failedBarrelNames.add(failedName);
                } catch (RemoteException ex) {
                    failedBarrelNames.add("unknown_barrel");
                }
                System.err.println("Erro ao obter informações do barrel ativo: " + e.getMessage());
                iterator.remove(); // Remove apenas da lista de ativos
            }
        }

        if (!failedBarrelNames.isEmpty()) {
            System.err.println("Removidos " + failedBarrelNames.size() + " barrel(s) falhado(s) da lista de ativos: " + failedBarrelNames);
        }

        return barrelInfo;
    }

    @Override
    public synchronized List<String> getRegisteredBarrels() throws RemoteException {
        List<String> barrelInfo = new ArrayList<>();

        // Usar o mapa de informações registradas como fonte principal
        for (Map.Entry<String, Integer> entry : registeredBarrelInfo.entrySet()) {
            String name = entry.getKey();
            Integer port = entry.getValue();
            barrelInfo.add(name + ":" + port);
        }

        // Se o mapa estiver vazio, usar a lista como fallback
        if (barrelInfo.isEmpty()) {
            for (BarrelInterface barrel : barrelsRegisters) {
                try {
                    String barrelName = barrel.getName();
                    int barrelPort = barrel.getPort();
                    barrelInfo.add(barrelName + ":" + barrelPort);
                } catch (RemoteException e) {
                    System.err.println("Erro ao obter informações do barrel registrado: " + e.getMessage());
                    // Não remover da lista de registrados aqui
                }
            }
        }

        return barrelInfo;
    }

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
}