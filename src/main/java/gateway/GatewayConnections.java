package gateway;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import barrel.BarrelInterface;
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

    public static BarrelInterface registerBarrel(String host, int port, String name, List<BarrelInterface> activeBarrels) {
        try {
            Registry barrelRegistry = LocateRegistry.getRegistry(host, port);
            BarrelInterface newBarrel = (BarrelInterface) barrelRegistry.lookup(name);

            // Sincronizar dados do novo barrel com um barrel ativo (se existir)
            if (!activeBarrels.isEmpty()) {
                BarrelInterface sourceBarrel = activeBarrels.get(0); // Escolhe o primeiro barrel ativo
                System.out.println("Sincronizando índice do novo barrel com " + sourceBarrel);
                Map<String, HashSet<String>> sourceIndex = sourceBarrel.getIndex();
                newBarrel.syncIndex(sourceIndex);
            }

            System.out.println("Novo barrel registrado: " + host + ":" + port + "/" + name);
            return newBarrel;
        } catch (Exception e) {
            System.err.println("Erro ao registrar barrel: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return null;
        }
    }
}