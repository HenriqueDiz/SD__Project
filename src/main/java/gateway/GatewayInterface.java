package gateway;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface GatewayInterface extends Remote {
    // Métodos principais para clientes
    public List<String> searchWord(String word) throws RemoteException;
    public void addURL(String url) throws RemoteException;
    
    // Estatísticas e administração
    public Map<String, Integer> getTop10Searches() throws RemoteException;
    public List<String> getActiveBarrels() throws RemoteException;
    public List<String> getRegisteredBarrels() throws RemoteException;
    public Map<String, Long> getAverageResponseTime() throws RemoteException;
    public HashSet<String> getUrlsForIndexedUrl(String url) throws RemoteException;
  
    // Método para barrels se registrarem
    public void registerBarrel(String host, int port, String name) throws RemoteException;
}