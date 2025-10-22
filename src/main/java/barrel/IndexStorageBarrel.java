package barrel;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class IndexStorageBarrel extends UnicastRemoteObject implements BarrelInterface {

    private ConcurrentHashMap<String, HashSet<String>> indexedItems; // Hashset for non repeated URLS

    public IndexStorageBarrel() throws RemoteException {
        super();
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
            
            // Servidor fica em execução sem interface
            Object lock = new Object();
            synchronized (lock) {
                lock.wait(); // Mantém o servidor ativo
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //private long counter = 0, timestamp = System.currentTimeMillis();

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
        System.out.println("\u001B[33mResultados da palavra " + word + " dados ao cliente\u001B[0m");
        if(indexedItems.containsKey(word)){
            ArrayList<String> resultadoPesquisa = new ArrayList<String>(indexedItems.get(word));
            return resultadoPesquisa;
        }
        return new ArrayList<String>();
    }
}