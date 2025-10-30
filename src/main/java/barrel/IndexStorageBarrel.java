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

public class IndexStorageBarrel extends UnicastRemoteObject implements BarrelInterface {

    private ConcurrentHashMap<String, HashSet<String>> indexedItems; // Hashset for non repeated URLS
    private final String name;
    private final int port;

    public IndexStorageBarrel(String name, int port) throws RemoteException {
        super();
        indexedItems = new ConcurrentHashMap<>();
        this.port = port;
        this.name = name;
               
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public int getPort() throws RemoteException {
        return port;
    }

    public void setIndexedItems(Map<String, HashSet<String>> indexedItems) {
        this.indexedItems = new ConcurrentHashMap<>(indexedItems);
    }

    public static void main(String args[]) {
        try {
            int port;
            String name;

            // Configuração inicial
            switch (args.length) {
                case 1 -> {
                    ConfigReader config = new ConfigReader(args[0]);
                    port = config.getPort();
                    name = config.getName();
                }
                case 2 -> {
                    port = Utils.validatePort(args[0]);
                    name = Utils.validateName(args[1]);
                }
                default -> {
                    System.out.println("Usage: java IndexStorageBarrel <port> <name> or java IndexStorageBarrel <barrelNumber>");
                    return;
                }
            }

            // Carregar progresso se existir
            Map<String, HashSet<String>> indexedItems = new HashMap<>();
            if (Utils.progressFileExists(name)) {
                System.out.println(Utils.yellow("Carregando progresso do barrel: " + name));
                indexedItems = Utils.loadBarrelProgress(name);
                System.out.println(Utils.green("Progresso carregado com sucesso!"));
            } else {
                System.out.println(Utils.yellow("Nenhum progresso encontrado. Criando novo barrel: " + name));
            }

            // Criar o Barrel
            IndexStorageBarrel barrel = new IndexStorageBarrel(name, port);
            barrel.setIndexedItems(indexedItems);

            // Criar Registry
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind(name, barrel);
            
            System.out.println("=".repeat(50));
            System.out.println("Index Storage Barrel iniciado:");
            System.out.println("Porta: " + port);
            System.out.println("Nome: " + name);
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
            
            gateway.registerBarrel(gatewayHost, port, name);
            System.out.println(Utils.green("Barrel registrado no Gateway!"));

            // Shutdown Hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println(Utils.yellow("Encerrando Barrel... Salvando progresso."));
                try {
                    Utils.saveBarrelProgress(name, barrel.getIndex());
                } catch (RemoteException e) {
                    System.err.println("Failed to save barrel progress: " + e.getMessage());
                    e.printStackTrace();
                }
                System.out.println(Utils.green("Progresso salvo com sucesso!"));
            }));

            Object lock = new Object();
            synchronized (lock) {
                lock.wait();
            }

        } catch (Exception e) {
            System.err.println(Utils.red("Erro fatal no Barrel: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    public synchronized void addToIndex(String word, String url) throws java.rmi.RemoteException {
        if(indexedItems.containsKey(word)){
            HashSet<String> palavrasParaUrls = indexedItems.get(word);
            palavrasParaUrls.add(url);
            indexedItems.put(word, palavrasParaUrls);
        }else {
            HashSet<String> palavrasNovas = new HashSet<String>();
            palavrasNovas.add(url);
            indexedItems.put(word, palavrasNovas);
        }
    }

    public List<String> searchWord(String word) throws java.rmi.RemoteException {
        System.out.println("Procurando por " + word);
        System.out.println(Utils.yellow("Resultados da palavra " + word + " dados ao cliente"));
        if(indexedItems.containsKey(word)){
            ArrayList<String> resultadoPesquisa = new ArrayList<String>(indexedItems.get(word));
            return resultadoPesquisa;
        }
        return new ArrayList<String>();
    }

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

    @Override
    public synchronized Map<String, HashSet<String>> getIndex() throws RemoteException {
        return new HashMap<>(indexedItems); // Retorna uma cópia do índice atual
    }
}