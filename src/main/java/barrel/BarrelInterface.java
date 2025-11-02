package barrel;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Interface remota para o Barrel.
 * Define os métodos que podem ser invocados remotamente.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public interface BarrelInterface extends Remote {
    public void addToIndex(String word, String url, boolean shouldRetransmit) throws java.rmi.RemoteException;
    public List<String> searchWord(String word) throws java.rmi.RemoteException;

    public void addUrlsForIndexedUrl(String url, HashSet<String> associatedUrls) throws RemoteException;
    public HashSet<String> getUrlsForIndexedUrl(String url) throws RemoteException;

    public void backupQueueState(Queue<String> pendingUrls, Set<String> seenUrls) throws RemoteException;
    public Map<String, Object> restoreQueueState() throws RemoteException;

     
    public void addWordCounts(Map<String, Integer> wordCounts, String url) throws RemoteException;
    public boolean isStopword(String palavra) throws RemoteException;

    public void syncIndex(Map<String, HashSet<String>> otherIndex) throws RemoteException;
    public String getName() throws RemoteException;
    public int getPort() throws RemoteException;
    public String getHost() throws RemoteException;

    public Map<String, HashSet<String>> getIndex() throws RemoteException;
    public Map<String, Integer> getInboundLinkCounts() throws RemoteException;

    public int getIndexSize() throws RemoteException;
    public boolean hasIndexedUrl(String url) throws java.rmi.RemoteException;
}