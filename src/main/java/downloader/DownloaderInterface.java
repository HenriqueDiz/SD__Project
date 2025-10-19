package downloader;

import java.rmi.*;

public interface DownloaderInterface extends Remote{
	public void printOnWorker(String toPrint) throws java.rmi.RemoteException;
}
