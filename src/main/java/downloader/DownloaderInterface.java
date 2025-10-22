package downloader;

import java.rmi.*;

public interface DownloaderInterface extends Remote{
	public int getProcessorURLsCount () throws java.rmi.RemoteException;
}
