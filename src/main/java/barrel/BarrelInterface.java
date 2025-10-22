package barrel;

import java.rmi.*;
import java.util.*;

public interface BarrelInterface extends Remote {
    public void addToIndex(String word, String url) throws java.rmi.RemoteException;
    public List<String> searchWord(String word) throws java.rmi.RemoteException;
}