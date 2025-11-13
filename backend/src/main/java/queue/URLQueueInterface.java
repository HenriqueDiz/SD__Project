package queue;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface remota para a URL Queue.
 * Define os métodos que podem ser invocados remotamente.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * @version 1.0
 */
public interface URLQueueInterface extends Remote {
    /**
     * Remove e retorna a próxima URL da fila.
     * @return A próxima URL da fila.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public String takeNext() throws RemoteException;

    /**
     * Adiciona uma nova URL à fila.
     * @param url A URL a ser adicionada.
     * @param priority Indica se a URL deve ter prioridade na fila.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public void putNew(String url, boolean priority) throws RemoteException;

    /**
     * Obtém o tamanho da fila de URLs.
     * @return O tamanho da fila.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public int getQueueSize() throws RemoteException;
}