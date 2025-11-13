package statistics;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface de callback para receber atualizações de estatísticas em tempo real.
 * Permite que clientes sejam notificados quando as estatísticas são atualizadas.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public interface StatsCallback extends Remote {
    
    /**
     * Notifica o cliente de uma atualização nas estatísticas.
     * 
     * @param stats             Objeto com as estatísticas atualizadas.
     * @throws RemoteException  Se ocorrer um erro de comunicação remota.
     */
    void onStatsUpdate(Statistics stats) throws RemoteException;
}