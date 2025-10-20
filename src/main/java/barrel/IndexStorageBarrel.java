package barrel;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import downloader.DownloaderInterface;

public class IndexStorageBarrel extends UnicastRemoteObject implements BarrelInterface {

    private BlockingQueue<String> urlsToIndex;
    private ConcurrentHashMap<String, HashSet<String>> indexedItems; // Hashset for non repeated URLS
    private DownloaderInterface robot;

    public IndexStorageBarrel() throws RemoteException {
        super();
        urlsToIndex = new LinkedBlockingQueue<String>();
        indexedItems = new ConcurrentHashMap<>();
               
    }

   public static void main(String args[]) {
        try {
            IndexStorageBarrel server = new IndexStorageBarrel();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("index", server);
            
            System.out.println("Index Storage Barrel iniciado na porta 1099");
            System.out.println("Aguardando conexões...");
            System.out.println("Use Ctrl+C para encerrar o servidor");
        
            // Shutdown hook - executa quando o processo é terminado
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\u001B[33mEncerrando servidor...\u001B[0m");
            try {
                // Unbind do registry
                registry.unbind("index");
                System.out.println("\u001B[33mServidor desregistrado do registry\u001B[0m");
                
                // Cleanup adicional se necessário
                if (server.robot != null) {
                    System.out.println("\u001B[33mDesconectando downloader...\u001B[0m");
                }
                
            } catch (Exception e) {
                System.err.println("Erro durante shutdown: " + e.getMessage());
            }
            System.out.println("\u001B[32mServidor encerrado com sucesso!\u001B[0m");
            }));
            
            // Servidor fica em execução sem interface
            Object lock = new Object();
            synchronized (lock) {
                lock.wait(); // Mantém o servidor ativo
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long counter = 0, timestamp = System.currentTimeMillis();

    public synchronized String takeNext() throws RemoteException {

        String nextUrl = urlsToIndex.poll();
        if (nextUrl == null) {
            nextUrl = "";
        }

        return nextUrl;
    }

    public synchronized void putNew(String url) throws java.rmi.RemoteException {
        urlsToIndex.add(url);

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

    
    public synchronized List<String> searchWord(String word) throws java.rmi.RemoteException {
        System.out.println("Procurando por " + word);
        robot.printOnWorker("\u001B[33mResultados da palavra " + word + " dados ao cliente\u001B[0m");
        System.out.println("\u001B[33mResultados da palavra " + word + " dados ao cliente\u001B[0m");
        if(indexedItems.containsKey(word)){
            ArrayList<String> resultadoPesquisa = new ArrayList<String>(indexedItems.get(word));
            return resultadoPesquisa;
        }
        return new ArrayList<String>();
    }

    public void subscribeRobot(DownloaderInterface robot) throws java.rmi.RemoteException {
        this.robot = robot;
    }
}