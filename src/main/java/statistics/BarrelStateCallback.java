package statistics;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BarrelStateCallback extends Remote {
    void onActiveBarrelsUpdate(List<String> activeBarrels) throws RemoteException;
    void onRegisteredBarrelsUpdate(List<String> registeredBarrels) throws RemoteException;
}