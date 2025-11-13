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
    /**
     * Adiciona uma palavra e sua URL ao índice.
     * @param word                 A palavra a ser adicionada.
     * @param url                  A URL associada à palavra.
     * @param shouldRetransmit     Indica se a adição deve ser retransmitida.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public void addToIndex(String word, String url, boolean shouldRetransmit) throws java.rmi.RemoteException;

    /**
     * Pesquisa uma palavra no índice.
     * @param word                 A palavra a ser pesquisada.
     * @return                     Uma lista de URLs onde a palavra foi encontrada.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public List<String> searchWord(String word) throws java.rmi.RemoteException;



    /**
     * Adiciona URLs associadas a uma URL indexada.
     * @param url                  A URL indexada.
     * @param associatedUrls       O conjunto de URLs associadas.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public void addUrlsForIndexedUrl(String url, HashSet<String> associatedUrls) throws RemoteException;

    /**
     * Obtém URLs associadas a uma URL indexada.
     * @param url                  A URL indexada.
     * @return                     O conjunto de URLs associadas.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public HashSet<String> getUrlsForIndexedUrl(String url) throws RemoteException;


    /**
     * Faz backup do estado da fila de URLs.
     * @param pendingUrls          A fila de URLs pendentes.
     * @param seenUrls             O conjunto de URLs já vistas.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public void backupQueueState(Queue<String> pendingUrls, Set<String> seenUrls) throws RemoteException;

    /**
     * Restaura o estado da fila de URLs a partir do backup.
     * @return                     Um mapa contendo a fila de URLs pendentes e o conjunto de URLs vistas.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public Map<String, Object> restoreQueueState() throws RemoteException;

     
    /**
     * Adiciona contagens de palavras ao índice.
     * @param wordCounts           Um mapa contendo as contagens de palavras.
     * @param url                  A URL associada às contagens de palavras.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public void addWordCounts(Map<String, Integer> wordCounts, String url) throws RemoteException;

    /**
     * Verifica se uma palavra é uma stopword.
     * @param palavra              A palavra a ser verificada.
     * @return                     true se for uma stopword, false caso contrário.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public boolean isStopword(String palavra) throws RemoteException;

    /**
     * Obtém a lista de stopwords do barrel.
     * @return                     Lista de stopwords.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public List<String> getStopwords() throws RemoteException;

    /**
     * Sincroniza stopwords com outro barrel.
     * @param stopwords            Lista de stopwords a serem sincronizadas.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public void syncStopwords(List<String> stopwords) throws RemoteException;

    /**
     * Sincroniza o índice com outro índice fornecido.
     * @param otherIndex           O índice a ser sincronizado.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public void syncIndex(Map<String, HashSet<String>> otherIndex) throws RemoteException;

    /**
     * Obtém o nome do barrel.
     * @return                     O nome do barrel.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public String getName() throws RemoteException;

    /**
     * Obtém a porta do barrel.
     * @return                     A porta do barrel.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public int getPort() throws RemoteException;

    /**
     * Obtém o host do barrel.
     * @return                     O host do barrel.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public String getHost() throws RemoteException;

    /**
     * Obtém o índice de URLs do barrel.
     * @return                     O índice de URLs do barrel.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public Map<String, HashSet<String>> getIndex() throws RemoteException;

    /**
     * Obtém a contagem de links de entrada para cada URL do barrel.
     * @return                     Um mapa contendo a contagem de links de entrada.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public Map<String, Integer> getInboundLinkCounts() throws RemoteException;

    /**
     * Obtém o tamanho do índice de URLs do barrel.
     * @return                     O tamanho do índice de URLs.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public int getIndexSize() throws RemoteException;

    /**
     * Verifica se uma URL está indexada no barrel.
     * @param url                  A URL a ser verificada.
     * @return                     true se a URL estiver indexada, false caso contrário.
     * @throws RemoteException     Se ocorrer um erro de comunicação remota.
     */
    public boolean hasIndexedUrl(String url) throws java.rmi.RemoteException;
}