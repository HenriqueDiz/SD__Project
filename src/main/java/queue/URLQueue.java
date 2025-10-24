package queue;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class URLQueue extends UnicastRemoteObject implements URLQueueInterface {

    private BlockingDeque<String> urlsToIndex;

    public URLQueue() throws RemoteException {
        super();
        urlsToIndex = new LinkedBlockingDeque<String>();
    }

    public static void main(String args[]) {
        try {
            URLQueue urlQueue = new URLQueue();
            
            // MUDANÇA: Suportar porta como argumento
            int port = 8181; // porta padrão
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
            
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("urlqueue", urlQueue); // MUDANÇA: nome "queue" em vez de "urlqueue"
            
            System.out.println("URL Queue iniciado na porta " + port);
            System.out.println("Aguardando conexões...");
            System.out.println("Use Ctrl+C para encerrar");
            
            Object lock = new Object();
            synchronized (lock) {
                lock.wait();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String takeNext() throws RemoteException {
        String nextUrl = urlsToIndex.poll();
        if (nextUrl == null) {
            nextUrl = "";
        }
        return nextUrl;
    }

    public void putNew(String url, boolean priority) throws RemoteException {
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

    public int getQueueSize() throws RemoteException {
        return urlsToIndex.size();
    }
}