package barrel;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface BarrelInterface extends Remote {
    public void addToIndex(String word, String url) throws java.rmi.RemoteException;
    public List<String> searchWord(String word) throws java.rmi.RemoteException;

    public void addUrlsForIndexedUrl(String url, HashSet<String> associatedUrls) throws RemoteException;
    public HashSet<String> getUrlsForIndexedUrl(String url) throws RemoteException;
     
    public void addWordCounts(Map<String, Integer> wordCounts, String url) throws RemoteException;
    public boolean isStopword(String palavra) throws RemoteException;

    // Novo método: sincronizar índice com outro barrel
    public void syncIndex(Map<String, HashSet<String>> otherIndex) throws RemoteException;
    public String getName() throws RemoteException;
    public int getPort() throws RemoteException;
    public String getHost() throws RemoteException;

    public Map<String, HashSet<String>> getIndex() throws RemoteException;
    public Map<String, Integer> getInboundLinkCounts() throws RemoteException;

    public int getIndexSize() throws RemoteException;
    public boolean hasIndexedUrl(String url) throws java.rmi.RemoteException;
}