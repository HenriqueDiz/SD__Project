package barrel;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.PageInfo;

public class IndexStorageBarrel extends UnicastRemoteObject implements BarrelInterface {

    protected IndexStorageBarrel() throws RemoteException {
        super();
    }

    @Override
    public void addPage(PageInfo page) throws RemoteException {
        System.out.println("\n[Barrel] Página recebida:");
        System.out.println("URL: " + page.getUrl());
        System.out.println("Título: " + page.getTitle());
        System.out.println("Snippet: " + page.getSnippet());
        System.out.println("Links encontrados: " + page.getLinks().size());
    }

    public static void main(String[] args) {
        try {
            IndexStorageBarrel barrel = new IndexStorageBarrel();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("BarrelService", barrel);
            System.out.println("[Barrel] Servidor RMI pronto e registado como 'BarrelService'");
            synchronized (barrel) {
                barrel.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}