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

            // Verificar se o barrel já foi registrado
            boolean isRegistered = barrelsRegisters.stream()
                .anyMatch(barrel -> {
                    try {
                        String registeredName = barrel.getName();
                        int registeredPort = barrel.getPort();
                        String newBarrelName = newBarrel.getName();
                        int newBarrelPort = newBarrel.getPort();

                        System.out.println("Comparando Barrel:");
                        System.out.println("Registrado -> Nome: " + registeredName + ", Porta: " + registeredPort);
                        System.out.println("Novo -> Nome: " + newBarrelName + ", Porta: " + newBarrelPort);

                        return registeredName.equals(newBarrelName) && registeredPort == newBarrelPort;
                    } catch (RemoteException e) {
                        System.err.println("Erro ao comparar barrels: " + e.getMessage());
                        return false;
                    }
                });

            if (isRegistered) {
                System.out.println(Utils.green("Barrel já registrado: " + name));
                // Carregar progresso salvo do ficheiro de objetos
                Map<String, HashSet<String>> savedProgress = Utils.loadBarrelProgress(name);
                newBarrel.syncIndex(savedProgress);

                // Sincronizar apenas os índices que faltam
                if (!activeBarrels.isEmpty()) {
                    BarrelInterface sourceBarrel = activeBarrels.get(0); // Escolhe o primeiro barrel ativo
                    Map<String, HashSet<String>> sourceIndex = sourceBarrel.getIndex();
                    Map<String, HashSet<String>> missingIndexes = getMissingIndexes(savedProgress, sourceIndex);
                    newBarrel.syncIndex(missingIndexes);
                }

                System.out.println(Utils.green("Dados sincronizados para o barrel: " + name));
            } else {
                System.out.println(Utils.yellow("Registrando novo barrel: " + name));
                barrelsRegisters.add(newBarrel);
                System.out.println("Barrel adicionado à lista de registrados: " + name);

                // Sincronizar todos os dados com um barrel ativo (se existir)
                if (!activeBarrels.isEmpty()) {
                    BarrelInterface sourceBarrel = activeBarrels.get(0); // Escolhe o primeiro barrel ativo
                    Map<String, HashSet<String>> sourceIndex = sourceBarrel.getIndex();
                    newBarrel.syncIndex(sourceIndex);
                    System.out.println(Utils.green("Dados sincronizados com o barrel ativo: " + sourceBarrel));
                }
            }

            // Adicionar o novo barrel à lista de barrels ativos
            if (!activeBarrels.contains(newBarrel)) {
                activeBarrels.add(newBarrel);
            }
        } catch (Exception e) {
            throw new RemoteException("Erro ao registrar barrel: " + e.getMessage());
            
        }
    }

    private static Map<String, HashSet<String>> getMissingIndexes(Map<String, HashSet<String>> savedProgress, Map<String, HashSet<String>> sourceIndex) {
        Map<String, HashSet<String>> missingIndexes = new HashMap<>();

        for (Map.Entry<String, HashSet<String>> entry : sourceIndex.entrySet()) {
            String word = entry.getKey();
            HashSet<String> urls = entry.getValue();

            if (!savedProgress.containsKey(word)) {
                // Palavra não existe no progresso salvo, adicionar tudo
                missingIndexes.put(word, new HashSet<>(urls));
            } else {
                // Palavra existe, adicionar apenas URLs que faltam
                HashSet<String> savedUrls = savedProgress.get(word);
                HashSet<String> newUrls = new HashSet<>(urls);
                newUrls.removeAll(savedUrls); // Remover URLs já existentes
                if (!newUrls.isEmpty()) {
                    missingIndexes.put(word, newUrls);
                }
            }
        }

        return missingIndexes;
    }
}