package gateway;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import barrel.BarrelInterface;
import common.Utils;
import queue.URLQueueInterface;

public class GatewayConnections {

    public static URLQueueInterface connectToURLQueue(Properties config) {
        try {
            String queueHost = config.getProperty("queue.host");
            String queuePortStr = config.getProperty("queue.port");
            String queueName = config.getProperty("queue.name");
            if (queueHost == null || queuePortStr == null || queueName == null) {
                System.err.println("Propriedades do URLQueue incompletas.");
                return null;
            }
            int queuePort = Integer.parseInt(queuePortStr.trim());
            Registry queueRegistry = LocateRegistry.getRegistry(queueHost, queuePort);
            URLQueueInterface urlQueue = (URLQueueInterface) queueRegistry.lookup(queueName);
            System.out.println("Conectado ao URLQueue: " + queueHost + ":" + queuePort + "/" + queueName);
            return urlQueue;
        } catch (Exception e) {
            System.err.println("Erro ao conectar ao URLQueue: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return null;
        }
    }

    public static void registerBarrel(String host, int port, String name, List<BarrelInterface> activeBarrels, List<BarrelInterface> barrelsRegisters) throws RemoteException {
        try {
            
            Registry barrelRegistry = LocateRegistry.getRegistry(host, port);
            BarrelInterface newBarrel = (BarrelInterface) barrelRegistry.lookup(name);

            // Adicionar o Barrel à lista de registrados, se ainda não estiver
            if (!barrelsRegisters.contains(newBarrel)) {
                barrelsRegisters.add(newBarrel);
                System.out.println(Utils.green("Barrel registrado: " + name));
            }

            // Adicionar o Barrel à lista de ativos, se ainda não estiver
            if (!activeBarrels.contains(newBarrel)) {
                activeBarrels.add(newBarrel);
                System.out.println(Utils.green("Barrel ativo: " + name));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Erro ao registrar barrel: " + e.getMessage());
        }
    }
}