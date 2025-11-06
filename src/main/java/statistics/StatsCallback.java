package statistics;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface StatsCallback extends Remote {
    void onStatsUpdate(Statistics stats) throws RemoteException;
}