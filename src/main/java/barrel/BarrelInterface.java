package barrel;

import java.rmi.*;
import java.util.*;

public interface BarrelInterface extends Remote {
    public void addToIndex(String word, String url) throws java.rmi.RemoteException;
    public List<String> searchWord(String word) throws java.rmi.RemoteException;

    // Novo método: sincronizar índice com outro barrel
    public void syncIndex(Map<String, HashSet<String>> otherIndex) throws RemoteException;
    String getName() throws RemoteException;
    int getPort() throws RemoteException;   

    // Novo método: obter índice atual
    public Map<String, HashSet<String>> getIndex() throws RemoteException;
}