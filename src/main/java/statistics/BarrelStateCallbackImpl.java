package statistics;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import common.Utils;

public class BarrelStateCallbackImpl extends UnicastRemoteObject implements BarrelStateCallback {

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