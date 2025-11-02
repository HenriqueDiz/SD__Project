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
    public String takeNext() throws RemoteException;
    public void putNew(String url, boolean priority) throws RemoteException;
    public int getQueueSize() throws RemoteException;
}