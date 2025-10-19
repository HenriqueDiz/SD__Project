package barrel;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.PageInfo;

public interface BarrelInterface extends Remote {
    void addPage(PageInfo page) throws RemoteException;
}
