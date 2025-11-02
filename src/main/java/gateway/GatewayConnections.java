package gateway;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import barrel.BarrelInterface;
import common.Utils;
import queue.URLQueueInterface;

/**
 * Classe utilitária para conexões do Gateway.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class GatewayConnections {

    /**
     * Conecta-se ao URL Queue usando as propriedades fornecidas.
     * 
     * @param config    Propriedades de configuração contendo host, porta e nome da URL Queue
     * @return          Instância remota da URL Queue ou null em caso de falha
     */
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

    /**
     * Registra um barrel no Gateway.
     * @param host
     * @param port
     * @param name
     * @param activeBarrels
     * @param barrelsRegisters
     * @param registeredBarrelInfo
     * @throws RemoteException
    */
    public static void registerBarrel(String host, int port, String name, List<BarrelInterface> activeBarrels, List<BarrelInterface> barrelsRegisters, Map<String, Integer> registeredBarrelInfo) throws RemoteException {
        try {
            Registry barrelRegistry = LocateRegistry.getRegistry(host, port);
            BarrelInterface newBarrel = (BarrelInterface) barrelRegistry.lookup(name);

            // Verificar se já está registrado usando o mapa de informações
            boolean alreadyRegistered = registeredBarrelInfo.containsKey(name) && 
                                    registeredBarrelInfo.get(name) == port;

            if (!alreadyRegistered) {
                barrelsRegisters.add(newBarrel);
                registeredBarrelInfo.put(name, port);
                System.out.println(Utils.green("Barrel registrado: " + name));
            } else {
                System.out.println(Utils.yellow("Barrel já registrado, atualizando referência e sincronizando: " + name));
                
                // Sincronizar com outros barrels ativos
                syncBarrelWithOthers(newBarrel, barrelsRegisters);
                
                // Usar método utilitário para remover barrel antigo
                removeBarrelByNameAndPort(barrelsRegisters, name, port);
                barrelsRegisters.add(newBarrel);
            }

            // Sempre atualizar a lista de ativos usando método utilitário
            removeBarrelByNameAndPort(activeBarrels, name, port);
            activeBarrels.add(newBarrel);
            System.out.println(Utils.green("Barrel ativo: " + name));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Erro ao registrar barrel: " + e.getMessage());
        }
    }

    /**
     * Sincroniza o índice do novo barrel com os outros barrels registrados.
     * 
     * @param newBarrel         O novo barrel a ser sincronizado
     * @param barrelsRegisters  A lista de barrels registrados para comparação
     */
    private static void syncBarrelWithOthers(BarrelInterface newBarrel, List<BarrelInterface> barrelsRegisters) {
        try {
            Map<String, HashSet<String>> newBarrelIndex = newBarrel.getIndex();
            Map<String, HashSet<String>> missingData = new HashMap<>();
            
            System.out.println(Utils.yellow("Iniciando sincronização do barrel: " + newBarrel.getName()));
            System.out.println("Barrel atual tem " + newBarrelIndex.size() + " palavra(s)");
            
            // Contadores para estatísticas detalhadas
            int completelyNewWords = 0;
            int wordsWithAdditionalUrls = 0;
            int totalMissingUrls = 0;
            
            // Coletar dados em falta de todos os outros barrels
            for (BarrelInterface barrel : barrelsRegisters) {
                try {
                    if (!barrel.getName().equals(newBarrel.getName())) {
                        Map<String, HashSet<String>> otherBarrelIndex = barrel.getIndex();
                        System.out.println("Comparando com barrel " + barrel.getName() + " que tem " + otherBarrelIndex.size() + " palavra(s)");
                        
                        // Encontrar palavras que o novo barrel não tem
                        for (Map.Entry<String, HashSet<String>> entry : otherBarrelIndex.entrySet()) {
                            String word = entry.getKey();
                            HashSet<String> urls = entry.getValue();
                            
                            if (!newBarrelIndex.containsKey(word)) {
                                // Palavra completamente nova
                                missingData.put(word, new HashSet<>(urls));
                                completelyNewWords++;
                                totalMissingUrls += urls.size();
                            } else {
                                // Verificar se há URLs em falta para esta palavra
                                HashSet<String> existingUrls = newBarrelIndex.get(word);
                                HashSet<String> missingUrls = new HashSet<>(urls);
                                missingUrls.removeAll(existingUrls); // Remove URLs que já existem
                                
                                if (!missingUrls.isEmpty()) {
                                    missingData.computeIfAbsent(word, k -> new HashSet<>()).addAll(missingUrls);
                                    wordsWithAdditionalUrls++;
                                    totalMissingUrls += missingUrls.size();
                                }
                            }
                        }
                    }
                } catch (RemoteException e) {
                    System.err.println("Erro ao sincronizar com barrel " + e.getMessage());
                }
            }
            
            // Sincronizar apenas os dados em falta
            if (!missingData.isEmpty()) {
                newBarrel.syncIndex(missingData);
                
                // Verificar o tamanho final
                Map<String, HashSet<String>> finalIndex = newBarrel.getIndex();
                
                System.out.println(Utils.green("=== ESTATÍSTICAS DE SINCRONIZAÇÃO ==="));
                System.out.println("Palavras completamente novas: " + completelyNewWords);
                System.out.println("Palavras com URLs adicionais: " + wordsWithAdditionalUrls);
                System.out.println("Total de entradas sincronizadas: " + missingData.size());
                System.out.println("Total de URLs sincronizadas: " + totalMissingUrls);
                System.out.println("Palavras antes: " + newBarrelIndex.size());
                System.out.println("Palavras depois: " + finalIndex.size());
                System.out.println("Palavras adicionadas: " + (finalIndex.size() - newBarrelIndex.size()));
                System.out.println("\n" + "=".repeat(70));

                
            } else {
                System.out.println(Utils.green("Barrel já está atualizado - nenhuma sincronização necessária"));
            }
            
        } catch (RemoteException e) {
            System.err.println("Erro durante sincronização: " + e.getMessage());
        }
    }

    /**
     * Remove um barrel da lista com base no nome e porta.
     * 
     * @param barrelList    A lista de barrels
     * @param name          O nome do barrel a ser removido
     * @param port          A porta do barrel a ser removido
     */
    private static void removeBarrelByNameAndPort(List<BarrelInterface> barrelList, String name, int port) {
        barrelList.removeIf(barrel -> {
            try {
                return barrel.getName().equals(name) && barrel.getPort() == port;
            } catch (RemoteException e) {
                return true; // Remove se não conseguir acessar
            }
        });
    }
}