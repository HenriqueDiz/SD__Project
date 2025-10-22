package queue;

import java.rmi.*;

public interface URLQueueInterface extends Remote {
    public String takeNext() throws RemoteException;
    public void putNew(String url, boolean priority) throws RemoteException;
    public int getQueueSize() throws RemoteException;
}
