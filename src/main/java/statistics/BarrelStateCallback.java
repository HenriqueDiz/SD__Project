package statistics;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface de callback para receber atualizações do estado dos barrels.
 * Permite que clientes sejam notificados quando barrels são adicionados ou removidos.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public interface BarrelStateCallback extends Remote {
    
    /**
     * Notifica o cliente de mudanças nos barrels ativos.
     * 
     * @param activeBarrels     Lista de barrels atualmente ativos.
     * @throws RemoteException  Se ocorrer um erro de comunicação remota.
     */
    void onActiveBarrelsUpdate(List<String> activeBarrels) throws RemoteException;
    
    /**
     * Notifica o cliente de mudanças nos barrels registrados.
     * 
     * @param registeredBarrels Lista de barrels registrados no sistema.
     * @throws RemoteException  Se ocorrer um erro de comunicação remota.
     */
    void onRegisteredBarrelsUpdate(List<String> registeredBarrels) throws RemoteException;
}