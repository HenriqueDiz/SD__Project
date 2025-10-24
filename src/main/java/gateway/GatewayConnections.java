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

    public static List<BarrelInterface> connectToBarrels(Properties config) {
        List<BarrelInterface> barrels = new ArrayList<>();
        for (String propertyName : config.stringPropertyNames()) {
            if (propertyName.startsWith("barrel") && propertyName.endsWith(".host")) {
                String barrelPrefix = propertyName.substring(0, propertyName.lastIndexOf(".host"));
                BarrelInterface b = connectToBarrel(config, barrelPrefix);
                if (b != null) barrels.add(b);
            }
        }
        return barrels;
    }

    private static BarrelInterface connectToBarrel(Properties config, String barrelPrefix) {
        try {
            String host = config.getProperty(barrelPrefix + ".host");
            String portStr = config.getProperty(barrelPrefix + ".port");
            String name = config.getProperty(barrelPrefix + ".name");
            if (host == null || portStr == null || name == null) {
                System.err.println("Propriedades incompletas para " + barrelPrefix);
                return null;
            }
            int port = Integer.parseInt(portStr.trim());
            Registry barrelRegistry = LocateRegistry.getRegistry(host, port);
            BarrelInterface barrel = (BarrelInterface) barrelRegistry.lookup(name);
            System.out.println("Conectado ao " + barrelPrefix + ": " + host + ":" + port + "/" + name);
            return barrel;
        } catch (Exception e) {
            System.err.println("Erro ao conectar ao " + barrelPrefix + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return null;
        }
    }
}