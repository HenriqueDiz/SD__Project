package downloader;

import java.rmi.Remote;

/**
 * Interface remota para o downloader.
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public interface DownloaderInterface extends Remote{
	public int getProcessorURLsCount () throws java.rmi.RemoteException;
}
