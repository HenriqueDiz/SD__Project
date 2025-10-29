package client;

import java.rmi.registry.LocateRegistry;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import common.ConfigReader;
import common.Utils;
import gateway.GatewayInterface;

public class Client {

    public static void main(String[] args) {
        try {
            int gatewayPort;

            if (args.length == 1) {
                gatewayPort = Utils.validatePort(args[0]);
            } else {
                ConfigReader config = new ConfigReader("gateway");
                gatewayPort = config.getPort();
            }
                        
            GatewayInterface gateway = (GatewayInterface) LocateRegistry.getRegistry("localhost", gatewayPort).lookup("gateway");
            System.out.println("Conectado ao Gateway na porta " + gatewayPort);
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
                System.out.println(Utils.red("4.") + "  Ver barrels ativos");
                System.out.println(Utils.red("5.") + "  Sair");
                System.out.println("=".repeat(50));
                System.out.print("Escolha uma opção (1-5): ");
                
                String choice = keyboard.nextLine().trim();
                
                switch (choice) {
                    case "1": 
                        System.out.println("\n" + Utils.yellow("ADICIONAR URL"));
                        System.out.println("-".repeat(30));
                        System.out.print("Digite o URL (http:// ou https://): ");
                        String url = keyboard.nextLine().trim();
                        
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            gateway.addURL(url);
                            System.out.println("URL adicionado com sucesso: " + url);
                        } else {
                            System.out.println("URL inválido! Deve começar com http:// ou https://");
                        }
                        break;
                        
                    case "2": 
                        System.out.println("\n" + Utils.yellow("PROCURAR PALAVRA"));
                        System.out.println("-".repeat(30));
                        System.out.print("Digite a palavra a procurar: ");
                        String word = keyboard.nextLine().trim();
                        
                        if (!word.isEmpty()) {
                            List<String> results = gateway.searchWord(word);
                            if (results.isEmpty()) {
                                System.out.println("Nenhum resultado encontrado para: " + word);
                            } else {
                                System.out.println("Encontrados " + results.size() + " resultado(s) para: " + word);
                                System.out.println("-".repeat(50));
                                for (int i = 0; i < results.size(); i++) {
                                    System.out.println((i + 1) + ". " + results.get(i));
                                }
                            }
                        } else {
                            System.out.println("Por favor, digite uma palavra válida!");
                        }
                        break;
                        
                    case "3":
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
                        break;
                        
                    case "4":
                        System.out.println("\n" + Utils.yellow("BARRELS ATIVOS"));
                        System.out.println("-".repeat(30));
                        List<String> barrels = gateway.getActiveBarrels();
                        for (String barrel : barrels) {
                            System.out.println("Barrel -> " + barrel);
                        }
                        break;
                        
                    case "5":
                        System.out.println("\n" + Utils.yellow("Encerrando o cliente..."));
                        keyboard.close();
                        System.exit(0);
                        break;
                        
                    default:
                        System.out.println("Opção inválida! Por favor, escolha 1-5.");
                        break;
                }
                
                System.out.println("\n" + Utils.yellow("Pressione Enter para continuar..."));
                keyboard.nextLine();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}