package queue;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import barrel.BarrelInterface;
import common.ConfigReader;
import common.Utils;
import gateway.GatewayInterface;

/**
 * Implementação da URL Queue.
 * Gerencia uma fila de URLs a serem processadas e mantém o controle das URLs já vistas.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class URLQueue extends UnicastRemoteObject implements URLQueueInterface {

    /**
     * Fila de URLs a serem indexadas.
     */
    private BlockingDeque<String> urlsToIndex;

    /**
     * Conjunto de URLs já vistas.
     */
    private Set<String> seenUrls;

    /** 
     * Construtor da URL Queue.
     * 
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public URLQueue() throws RemoteException {
        super();
        urlsToIndex = new LinkedBlockingDeque<String>();
        seenUrls = ConcurrentHashMap.newKeySet();
    }

    /**
     * Método principal da URL Queue.
     * 
     * @param args Argumentos da linha de comando (opcionais: host, port, name)
    */
    public static void main(String args[]) {
        try {
            int port;
            String host;
            String name;

            if (args.length == 3) {
                host = args[0];
                port = Utils.validatePort(args[1]);
                name = Utils.validateName(args[2]);
            } else {
                ConfigReader config = new ConfigReader("queue");
                host = config.getHost();
                port = config.getPort();
                name = config.getName();
            }

            // IMPORTANTE: Definir hostname ANTES de criar o objeto remoto
            System.setProperty("java.rmi.server.hostname", host);
            
            URLQueue urlQueue = new URLQueue();

            // Tentar restaurar estado da queue a partir do gateway -> barrel
            restoreStateFromGatewayAsync(urlQueue);

            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind(name, urlQueue);

            System.out.println("URL Queue iniciado em " + host + ":" + port + " com nome '" + name + "'");
            System.out.println("URLs na fila: " + urlQueue.getQueueSize());
            System.out.println("Aguardando conexões...");
            System.out.println("Use Ctrl+C para encerrar");

            // Shutdown Hook - Fazer backup antes de morrer
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println(Utils.yellow("\n┌" + "─".repeat(50) + "┐"));
                System.out.println("│" + Utils.bold(" ENCERRANDO QUEUE - SALVANDO BACKUP") + " ".repeat(15) + "│");
                System.out.println("└" + "─".repeat(50) + "┘");
                
                try {
                    // Conectar ao Gateway e pegar barrels ativos
                    ConfigReader gatewayConfig = new ConfigReader("gateway");
                    Registry gatewayRegistry = LocateRegistry.getRegistry(
                        gatewayConfig.getHost(), 
                        gatewayConfig.getPort()
                    );
                    GatewayInterface gateway = (GatewayInterface) gatewayRegistry.lookup(gatewayConfig.getName());
                    java.util.List<String> activeBarrels = gateway.getActiveBarrels();
                    
                    if (activeBarrels.isEmpty()) {
                        System.out.println(Utils.red("Nenhum barrel ativo para salvar backup!"));
                        return;
                    }
                    
                    // Salvar estado em todos os barrels ativos
                    Queue<String> pendingUrls = new LinkedList<>(urlQueue.urlsToIndex);
                    Set<String> seenUrlsCopy = new java.util.HashSet<>(urlQueue.seenUrls);
                    
                    for (String barrelInfo : activeBarrels) {
                        try {
                            String[] parts = barrelInfo.split(":");
                            String barrelName = parts[0];
                            int barrelPort = Integer.parseInt(parts[1]);
                            String barrelHost = parts[2];
                            
                            Registry barrelRegistry = LocateRegistry.getRegistry(barrelHost, barrelPort);
                            BarrelInterface barrel = (BarrelInterface) barrelRegistry.lookup(barrelName);
                            barrel.backupQueueState(pendingUrls, seenUrlsCopy);
                            
                            System.out.println(Utils.green("Backup salvo em: " + barrelName));
                        } catch (Exception e) {
                            System.err.println(Utils.red("Erro ao salvar em barrel: " + e.getMessage()));
                        }
                    }
                    
                    System.out.println(Utils.green("\nBackup concluído!"));
                } catch (Exception e) {
                    Utils.printLogException("Erro ao salvar backup da Queue", e);
                }
            }));

            Object lock = new Object();
            synchronized (lock) {
                lock.wait();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Método para pegar a próxima URL da fila.
     *
     * @return A próxima URL a ser processada.
     * @throws RemoteException Se ocorrer um erro de rede.
     */
    @Override
    public String takeNext() throws RemoteException {
        String nextUrl = "";
        try {
            nextUrl = urlsToIndex.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Interrupted while taking URL from queue", e);
        }
        return nextUrl;
    }

    /**
     * Método para adicionar uma nova URL à fila.
     *
     * @param url       A URL a ser adicionada.
     * @param priority  Se true, a URL é adicionada ao início da fila; caso contrário, ao final.
     * @throws RemoteException  Se ocorrer um erro de rede.
     */
    @Override
    public void putNew(String url, boolean priority) throws RemoteException {
        if (url == null || url.isEmpty()) {
            return;
        }
        if (!seenUrls.add(url)) {
            return;
        }
        try {
            if (priority) { // Quando o cliente adiciona mete em primeiro na queue
                urlsToIndex.putFirst(url);
            } else { // Quando é o downloader a meter mete no fim
                urlsToIndex.putLast(url);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Interrupted while adding URL to queue", e);
        }
    }

    /**
     * Método para obter o tamanho atual da fila.
     *
     * @return O número de URLs na fila.
     * @throws RemoteException Se ocorrer um erro de rede.
     */
    @Override
    public int getQueueSize() throws RemoteException {
        return urlsToIndex.size();
    }

    /**
     * Tenta restaurar o estado da URL Queue a partir do Gateway e dos Barrels ativos.
     * Executa em uma thread separada para não bloquear o início da Queue.
     * 
     * @param urlQueue  A instância da URL Queue a ser restaurada.
     */
    private static void restoreStateFromGatewayAsync(URLQueue urlQueue) {
        new Thread(() -> {
            int attempts = 0;
            int maxAttempts = 60;
            long delayMs = 1000;

            while (attempts < maxAttempts) {
                try {
                    ConfigReader gatewayConfig = new ConfigReader("gateway");
                    Registry gatewayRegistry = LocateRegistry.getRegistry(
                        gatewayConfig.getHost(),
                        gatewayConfig.getPort()
                    );
                    GatewayInterface gateway = (GatewayInterface) gatewayRegistry.lookup(gatewayConfig.getName());

                    java.util.List<String> activeBarrels = gateway.getActiveBarrels();
                    if (activeBarrels == null || activeBarrels.isEmpty()) {
                        System.out.println(Utils.yellow("Gateway disponível, mas sem barrels ativos. A tentar novamente..."));
                    } else {
                        String[] parts = activeBarrels.get(0).split(":");
                        String barrelName = parts[0];
                        int barrelPort = Integer.parseInt(parts[1]);
                        String barrelHost = parts[2];

                        Registry barrelRegistry = LocateRegistry.getRegistry(barrelHost, barrelPort);
                        BarrelInterface barrel = (BarrelInterface) barrelRegistry.lookup(barrelName);

                        java.util.Map<String, Object> queueState = barrel.restoreQueueState();
                        java.util.Queue<String> restoredUrls = (java.util.Queue<String>) queueState.get("pendingUrls");
                        java.util.Set<String> restoredSeen = (java.util.Set<String>) queueState.get("seenUrls");

                        if (restoredUrls != null) urlQueue.urlsToIndex.addAll(restoredUrls);
                        if (restoredSeen != null) urlQueue.seenUrls.addAll(restoredSeen);

                        System.out.println(Utils.green("Estado da Queue restaurado do barrel! URLs: " + urlQueue.urlsToIndex.size()));
                        return;
                    }
                } catch (Exception e) {
                    if (attempts % 5 == 0) {
                        System.out.println(Utils.yellow("Gateway ainda indisponível. Retentando... (" + (attempts + 1) + ")"));
                    }
                }
                attempts++;
                try { Thread.sleep(delayMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
            }

            System.out.println(Utils.yellow("Não foi possível restaurar estado da Queue. Prosseguindo vazio."));
        }, "queue-restore-retry").start();
    }
}