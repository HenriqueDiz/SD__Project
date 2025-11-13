package statistics;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import common.Utils;

/**
 * Implementação do callback de estado dos barrels que imprime atualizações no console.
 * Recebe notificações de mudanças no estado dos barrels e exibe-as formatadas.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class BarrelStateCallbackImpl extends UnicastRemoteObject implements BarrelStateCallback {

    /**
     * Construtor que exporta o objeto remoto.
     * 
     * @throws RemoteException  Se ocorrer um erro ao exportar o objeto remoto.
     */
    public BarrelStateCallbackImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized void onActiveBarrelsUpdate(List<String> activeBarrels) throws RemoteException {
        System.out.println("\n" + Utils.yellow("ATUALIZAÇÃO: BARRELS ATIVOS"));
        System.out.println("-".repeat(30));
        if (activeBarrels == null || activeBarrels.isEmpty()) {
            System.out.println("Nenhum barrel ativo no momento.");
            return;
        }
        for (String barrel : activeBarrels) {
            String[] parts = barrel.split(":");
            if (parts.length >= 4) {
                System.out.println("Barrel Ativo -> " + Utils.bold(parts[0]));
                System.out.println("Porta: " + parts[1]);
                System.out.println("Host: " + parts[2]);
                System.out.println("Índice: " + parts[3] + "\n");
            } else {
                System.out.println(barrel);
            }
        }
    }

    @Override
    public synchronized void onRegisteredBarrelsUpdate(List<String> registeredBarrels) throws RemoteException {
        System.out.println("\n" + Utils.yellow("ATUALIZAÇÃO: BARRELS REGISTRADOS"));
        System.out.println("-".repeat(30));
        if (registeredBarrels == null || registeredBarrels.isEmpty()) {
            System.out.println("Nenhum barrel registrado no momento.");
            return;
        }
        for (String barrel : registeredBarrels) {
            System.out.println("Barrel Registrado -> " + barrel);
        }
    }
}