package client;

import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import common.ConfigReader;
import common.PageInfo;
import common.Utils;
import gateway.GatewayInterface;

public class Client {

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
                            System.out.println("Como deseja visualizar?");
                            System.out.println("1) Mostrar todos");
                            System.out.println("2) Mostrar até um número máximo");
                            System.out.print("Escolha (1-2): ");
                            String viewChoice = keyboard.nextLine().trim();

                            int toShow = results.size();
                            if ("2".equals(viewChoice)) {
                                System.out.print("Digite o número máximo de urls a mostrar: ");
                                String maxStr = keyboard.nextLine().trim();
                                int maxResults;
                                try {
                                    maxResults = Integer.parseInt(maxStr);
                                    if (maxResults <= 0) {
                                        System.out.println("Valor inválido! Será utilizado o valor padrão: 10");
                                        maxResults = 10;
                                    }
                                } catch (NumberFormatException ex) {
                                    System.out.println("Entrada inválida! Será utilizado o valor padrão: 10");
                                    maxResults = 10;
                                }

                                toShow = Math.min(maxResults, results.size());
                                if (results.size() < maxResults) {
                                    System.out.println("Apenas " + results.size() + " resultado(s) encontrados (pedido: " + maxResults + ").");
                                }
                            }
                            System.out.println(Utils.yellow("-".repeat(60)));
                            for (int i = 0; i < toShow; i++) {
                                String urlRes = results.get(i)[0];
                                String refs = results.get(i)[1];

                                System.out.println(Utils.bold(Utils.green("[" + (i + 1) + "]")) + " - " + Utils.yellow(refs + " Referência(s)"));
                                System.out.println(new PageInfo(urlRes) + "\n");
                            }
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
                        System.out.println("\n" + Utils.yellow("TOP 10 PESQUISAS"));
                        System.out.println("-".repeat(30));

                        Map<String, Integer> top10 = gateway.getTop10Searches();
                        if (top10.isEmpty()) {
                            System.out.println("Ainda não há pesquisas registradas");
                        } else {
                            int rank = 1;
                            for (Map.Entry<String, Integer> entry : top10.entrySet()) {
                                System.out.println(rank + ". " + entry.getKey() + " (" + entry.getValue() + " pesquisas)");
                                rank++;
                            }
                        }
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

                        System.out.println("\n" + Utils.yellow("TEMPO MÉDIO DE RESPOSTA POR BARREL"));
                        System.out.println("-".repeat(30));
                        Map<String, Long> responseTimes = gateway.getAverageResponseTime();
                        if (responseTimes.isEmpty()) {
                            System.out.println("Nenhum tempo de resposta registrado.");
                        } else {
                            for (Map.Entry<String, Long> entry : responseTimes.entrySet()) {
                                System.out.println("Barrel: " + entry.getKey() + " - Tempo médio: " + entry.getValue() + " ns");
                            }
                        }
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