package gateway;

import java.rmi.*;
import java.util.*;

public interface GatewayInterface extends Remote {
    // Métodos principais para clientes
    public List<String> searchWord(String word) throws RemoteException;
    public void addURL(String url) throws RemoteException;
    
    // Estatísticas e administração
    public Map<String, Integer> getTop10Searches() throws RemoteException;
    public List<String> getActiveBarrels() throws RemoteException;
  
    
    // Método para barrels se registrarem
    public void registerBarrel(String host, int port, String name) throws RemoteException;
}