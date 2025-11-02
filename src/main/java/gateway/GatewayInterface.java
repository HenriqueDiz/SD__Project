package gateway;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Interface remota para o Gateway.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public interface GatewayInterface extends Remote {
    // Métodos principais para clientes
    public List<String> searchWordGateway(String word) throws RemoteException;
    public boolean addURL(String url, boolean indexAnyway) throws RemoteException;
    public List<String[]> searchWords(List<String> words) throws RemoteException;
    // Estatísticas e administração
    public Map<String, Integer> getTop10Searches() throws RemoteException;
    public List<String> getActiveBarrels() throws RemoteException;
    public List<String> getRegisteredBarrels() throws RemoteException;
    public Map<String, Long> getAverageResponseTime() throws RemoteException;
    public HashSet<String> getUrlsForIndexedUrl(String url) throws RemoteException;
  
    // Método para barrels se registrarem
    public void registerBarrel(String host, int port, String name) throws RemoteException;
}