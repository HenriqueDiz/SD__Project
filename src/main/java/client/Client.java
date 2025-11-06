package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import common.ConfigReader;
import common.PageInfo;
import common.Utils;
import gateway.GatewayInterface;
import statistics.BarrelStateCallback;
import statistics.BarrelStateCallbackImpl;
import statistics.Statistics;
import statistics.StatsCallback;
import statistics.StatsCallbackImpl;

/**
 * Cliente que interage com o Gateway para realizar operações como adicionar URLs,
 * procurar palavras, ver estatísticas e consultar ligações de páginas.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class Client {

    /**
     * Construtor padrão.
     */
    public Client() {}

    /**
     * Método principal para iniciar o cliente.
     * Fornece um menu interativo para o usuário realizar várias operações através do Gateway.
     * 
     * @param args Argumentos da linha de comando (opcionais: gatewayPort, queuePort, gatewayHost, queueHost)
     */
    public static void main(String[] args) {
        try {
            String gatewayHost;
            int gatewayPort;
            String gatewayName;

            if (args.length == 1) {
                gatewayPort = Utils.validatePort(args[0]);
                ConfigReader config = new ConfigReader("gateway");
                gatewayHost = config.getHost();
                gatewayName = config.getName();
            } else {
                ConfigReader config = new ConfigReader("gateway");
                gatewayHost = config.getHost();
                gatewayPort = config.getPort();
                gatewayName = config.getName();
            }

            GatewayInterface gateway = (GatewayInterface) LocateRegistry.getRegistry(gatewayHost, gatewayPort).lookup(gatewayName);
            System.out.println("Conectado ao Gateway em " + gatewayHost + ":" + gatewayPort);
            System.out.println("\n" + "=".repeat(50) + "\n");
            System.out.println(Utils.red(" ▄████  ▒█████   ▒█████    ▄████  ▒█████   ██▓    "));
            System.out.println(Utils.red(" ██▒ ▀█▒▒██▒  ██▒▒██▒  ██▒ ██▒ ▀█▒▒██▒  ██▒▓██▒    "));
            System.out.println(Utils.red("▒██░▄▄▄░▒██░  ██▒▒██░  ██▒▒██░▄▄▄░▒██░  ██▒▒██░    "));
            System.out.println(Utils.red("░▓█  ██▓▒██   ██░▒██   ██░░▓█  ██▓▒██   ██░▒██░    "));
            System.out.println(Utils.red("░▒▓███▀▒░ ████▓▒░░ ████▓▒░░▒▓███▀▒░ ████▓▒░░██████▒"));
            System.out.println(Utils.red(" ░▒   ▒ ░ ▒░▒░▒░ ░ ▒░▒░▒░  ░▒   ▒ ░ ▒░▒░▒░ ░ ▒░▓  ░"));
            System.out.println(Utils.red("  ░   ░   ░ ▒ ▒░   ░ ▒ ▒░   ░   ░   ░ ▒ ▒░ ░ ░ ▒  ░"));
            System.out.println(Utils.red("░ ░   ░ ░ ░ ░ ▒  ░ ░ ░ ▒  ░ ░   ░ ░ ░ ░ ▒    ░ ░   "));
            System.out.println(Utils.red("      ░     ░ ░      ░ ░        ░     ░ ░      ░  ░"));
            System.out.println("\n" + "=".repeat(50) + "\n");
            Scanner keyboard = new Scanner(System.in);
            while (true) {
                System.out.println("\n" + "=".repeat(50));
                System.out.println(Utils.red(Utils.bold("                   CLIENT MENU")));
                System.out.println("=".repeat(50));
                System.out.println(Utils.red("1.") + "  Adicionar URL para indexar");
                System.out.println(Utils.red("2.") + "  Procurar uma palavra");
                System.out.println(Utils.red("3.") + "  Ver estatísticas");
                System.out.println(Utils.red("4.") + "  Consultar lista de ligações de uma página");
                System.out.println(Utils.red("5.") + "  Sair");
                System.out.println("=".repeat(50));
                System.out.print("Escolha uma opção (1-5): ");
                
                List<String> activeBarrelsBeforeSearch;
                String choice = keyboard.nextLine().trim();
                
                switch (choice) {
                    case "1" -> { 
                        // Verificar se há barrels ativos antes de tentar a consulta
                        activeBarrelsBeforeSearch = gateway.getActiveBarrels();
                        if (activeBarrelsBeforeSearch == null || activeBarrelsBeforeSearch.isEmpty()) {
                            System.out.println(Utils.red("Não foi possível fazer a consulta: não há barrels ativos."));
                            break;
                        }
                        System.out.println("\n" + Utils.yellow("ADICIONAR URL"));
                        System.out.println("-".repeat(30));
                        System.out.print("Digite o URL (http:// ou https://): ");
                        String url = keyboard.nextLine().trim();
                        
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            boolean alreadySeen = gateway.addURL(url, false);
                            if (alreadySeen) {
                                System.out.println("URL já foi indexado anteriormente: " + url);
                                System.out.print("Pretende reindexar mesmo assim? (S/N): ");
                                String resp = keyboard.nextLine().trim().toLowerCase();
                                if (resp.equals("s") || resp.equals("sim") || resp.equals("y") || resp.equals("yes")) {
                                    gateway.addURL(url, true);
                                    System.out.println("URL adicionado novamente para reindexação: " + url);
                                } else {
                                    System.out.println("Reindexação cancelada.");
                                }
                            } else {
                                System.out.println("URL adicionado com sucesso: " + url);
                            }
                        } else {
                            System.out.println("URL inválido! Deve começar com http:// ou https://");
                        }
                    }
                    case "2" -> { 

                        // Verificar se há barrels ativos antes de tentar a consulta
                        activeBarrelsBeforeSearch = gateway.getActiveBarrels();
                        if (activeBarrelsBeforeSearch == null || activeBarrelsBeforeSearch.isEmpty()) {
                            System.out.println(Utils.red("Não foi possível fazer a consulta: não há barrels ativos."));
                            break;
                        }
                        System.out.println("\n" + Utils.yellow("PROCURAR PALAVRA(S)"));
                        System.out.println("-".repeat(30));


                        System.out.print("Digite a(s) palavra(s) a procurar: ");
                        String word = keyboard.nextLine().trim();
                        
                        if (!word.isEmpty()) {
                            List<String> words = Arrays.asList(word.split("\\s+"));
                            List<String[]> results = gateway.searchWords(words);
                            if (results == null || results.isEmpty()) {
                                System.out.println("Nenhum resultado encontrado para: " + words);
                                break;
                            }

                            System.out.println("Encontrados " + results.size() + " resultado(s) para: " + words);
        
                            // Paginação: mostrar de 10 em 10
                            int pageSize = 10;
                            int totalPages = (int) Math.ceil((double) results.size() / pageSize);
                            int currentPage = 0;

                            while (currentPage < totalPages) {
                                int start = currentPage * pageSize;
                                int end = Math.min(start + pageSize, results.size());
                                
                                System.out.println("\n" + Utils.yellow("=".repeat(60)));
                                System.out.println(Utils.yellow("Página " + (currentPage + 1) + " de " + totalPages));
                                System.out.println(Utils.yellow("=".repeat(60)));
                                
                                for (int i = start; i < end; i++) {
                                    String urlRes = results.get(i)[0];
                                    String refs = results.get(i)[1];

                                    System.out.println(Utils.bold(Utils.green("[" + (i + 1) + "]")) + " - " + Utils.yellow(refs + " Referência(s)"));
                                    System.out.println(new PageInfo(urlRes) + "\n");
                                }
                                
                                currentPage++;
                                
                                if (currentPage < totalPages) {
                                    System.out.print(Utils.yellow("Pressione Enter para ver mais resultados (ou digite 'q' para sair): "));
                                    String input = keyboard.nextLine().trim();
                                    if (input.equalsIgnoreCase("q")) {
                                        break;
                                    }
                                }
                            }
                            
                            System.out.println(Utils.green("\nFim dos resultados."));
                            
                        } else {
                            System.out.println("Por favor, digite uma palavra válida!");
                        }
                    }
                        
                    case "3" -> {

                        // Verificar se há barrels ativos antes de tentar a consulta
                        activeBarrelsBeforeSearch = gateway.getActiveBarrels();
                        if (activeBarrelsBeforeSearch == null || activeBarrelsBeforeSearch.isEmpty()) {
                            System.out.println(Utils.red("Não foi possível fazer a consulta: não há barrels ativos."));
                            break;
                        }
                        
                        Statistics stats = gateway.getBarrelStatistics();

                        System.out.println("\n" + Utils.yellow("BARRELS ATIVOS"));
                        System.out.println("-".repeat(30));
                        List<String> activeBarrels = gateway.getActiveBarrels();
                        if (activeBarrels.isEmpty()) {
                            System.out.println("Nenhum barrel ativo no momento.");
                        } else {
                            for (String barrel : activeBarrels) {
                                String[] barrelParts = barrel.split(":");
                                System.out.println("Barrel Ativo -> " + Utils.bold(barrelParts[0]));
                                System.out.println("Porta: " + barrelParts[1]);
                                System.out.println("Host: " + barrelParts[2]);
                                System.out.println("Índice: " + barrelParts[3] + "\n");
                            }
                        }

                        System.out.println("\n" + Utils.yellow("BARRELS REGISTRADOS"));
                        System.out.println("-".repeat(30));
                        List<String> registeredBarrels = gateway.getRegisteredBarrels();
                        if (registeredBarrels.isEmpty()) {
                            System.out.println("Nenhum barrel registrado no momento.");
                        } else {
                            for (String barrel : registeredBarrels) {
                                System.out.println("Barrel Registrado -> " + barrel);
                            }
                        }

                        System.out.println("\n" + Utils.yellow("TOP 10 PESQUISAS"));
                        System.out.println("-".repeat(30));
                        Map<String, Integer> top10 = stats.getTop10Searches();
                        if (top10.isEmpty()) {
                            System.out.println("Ainda não há pesquisas registradas");
                        } else {
                            int rank = 1;
                            for (Map.Entry<String, Integer> entry : top10.entrySet()) {
                                System.out.println(rank + ". " + entry.getKey() + " (" + entry.getValue() + " pesquisas)");
                                rank++;
                            }
                        }

                        System.out.println("\n" + Utils.yellow("TEMPO MÉDIO DE RESPOSTA POR BARREL"));
                        System.out.println("-".repeat(30));
                        Map<String, Long> responseTimes = stats.getAverageResponseTime();
                        if (responseTimes.isEmpty()) {
                            System.out.println("Nenhum tempo de resposta registrado.");
                        } else {
                            for (Map.Entry<String, Long> entry : responseTimes.entrySet()) {
                                System.out.println("Barrel: " + entry.getKey() + " - Tempo médio: " + entry.getValue() + " ns");
                            }
                        }

                        // Registrar callbacks para atualizações em tempo real
                        System.out.println("\n" + Utils.blue("A ouvir atualizações em tempo real (estatísticas e estado dos barrels)...") + "\n");
                        System.out.println(Utils.yellow("Digite 'q' + Enter para parar de ouvir."));
                        StatsCallback statsListener = new StatsCallbackImpl();
                        BarrelStateCallback stateListener = new BarrelStateCallbackImpl();
                        gateway.registerStatsCallback(statsListener);
                        gateway.registerBarrelStateCallback(stateListener);

                        // Loop simples até utilizador parar
                        while (true) {
                            String line = keyboard.nextLine().trim();
                            if (line.equalsIgnoreCase("q")) {
                                break;
                            }
                        }

                        // Cleanup dos callbacks
                        try { gateway.unregisterStatsCallback(statsListener); } 
                        catch (Exception e) { Utils.printLogException("Erro ao desregistrar callback de estatísticas: " + e.getMessage(), e); }
                        try { gateway.unregisterBarrelStateCallback(stateListener); } 
                        catch (Exception e) { Utils.printLogException("Erro ao desregistrar callback de estado: " + e.getMessage(), e); }

                        try { UnicastRemoteObject.unexportObject(statsListener, true); } 
                        catch (Exception e) { Utils.printLogException("Erro ao desexportar objeto remoto (stats): " + e.getMessage(), e); }
                        try { UnicastRemoteObject.unexportObject(stateListener, true); } 
                        catch (Exception e) { Utils.printLogException("Erro ao desexportar objeto remoto (estado): " + e.getMessage(), e); }
                    }

                    case "4" -> {

                        activeBarrelsBeforeSearch = gateway.getActiveBarrels();
                        if (activeBarrelsBeforeSearch == null || activeBarrelsBeforeSearch.isEmpty()) {
                            System.out.println(Utils.red("Não foi possível fazer a consulta: não há barrels ativos."));
                            break;
                        }   
                        System.out.println("\n" + Utils.yellow("CONSULTAR LISTA DE LIGAÇÕES DE UMA PÁGINA"));
                        System.out.println("-".repeat(30));
                        
                        System.out.print("Digite o URL (http:// ou https://): ");
                        String targetUrl = keyboard.nextLine().trim();

                        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                            System.out.println("URL inválido! Deve começar com http:// ou https://");
                            break;
                        }

                        try {
                            HashSet<String> links = gateway.getUrlsForIndexedUrl(targetUrl);
                            if (links == null || links.isEmpty()) {
                                System.out.println("Nenhuma ligação encontrada (página ainda não indexada ou sem links).");
                            } else {
                                System.out.println("Encontrados " + links.size() + " link(s):");
                                System.out.println("-".repeat(60));
                                int i = 0;
                                for (String link : links) {
                                    System.out.println(Utils.bold(Utils.green("[" + (++i) + "]")));
                                    System.out.println(new PageInfo(link) + "\n");
                                }
                            }
                        } catch (Exception e) {
                            Utils.printLogException("Erro ao obter ligações para a página especificada (" + targetUrl + "): " + e.getMessage(), e);
                        }
                    }

                    case "5" -> {
                        System.out.println("\n" + Utils.yellow("Encerrando o cliente..."));
                        keyboard.close();
                        System.exit(0);
                    }
                        
                    default -> System.out.println("Opção inválida! Por favor, escolha 1-5.");
                }
                
                System.out.println("\n" + Utils.yellow("Pressione Enter para continuar..."));
                keyboard.nextLine();
            }
            
        } catch (Exception e) {
            Utils.printLogException(e);
        }
    }
}