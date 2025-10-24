package client;

import java.rmi.registry.*;
import gateway.GatewayInterface;
import java.util.*;

public class Client {

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("Uso: java Client <gatewayPort>");
                System.out.println("Exemplo: java Client 8183");
                return;
            }
            
            int gatewayPort = Integer.parseInt(args[0]);
            
            GatewayInterface gateway = (GatewayInterface) LocateRegistry.getRegistry("localhost", gatewayPort).lookup("gateway");
            Scanner keyboard = new Scanner(System.in);
            
            System.out.println("🚪 Conectado ao Gateway na porta " + gatewayPort);
            
            while (true) {
                System.out.println("\n" + "=".repeat(50));
                System.out.println("                   \u001B[31m\u001B[1mCLIENT MENU\u001B[0m\u001B[0m");
                System.out.println("=".repeat(50));
                System.out.println("\u001B[31m1.\u001B[0m  Adicionar URL para indexar");
                System.out.println("\u001B[31m2.\u001B[0m  Procurar uma palavra");
                System.out.println("\u001B[31m3.\u001B[0m  Ver estatísticas");
                System.out.println("\u001B[31m4.\u001B[0m  Ver barrels ativos");
                System.out.println("\u001B[31m5.\u001B[0m  Sair");
                System.out.println("=".repeat(50));
                System.out.print("Escolha uma opção (1-5): ");
                
                String choice = keyboard.nextLine().trim();
                
                switch (choice) {
                    case "1": 
                        System.out.println("\n\u001B[33mADICIONAR URL\u001B[0m");
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
                        System.out.println("\n\u001B[33mPROCURAR PALAVRA\u001B[0m");
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
                        System.out.println("\n\u001B[33mTOP 10 PESQUISAS\u001B[0m");
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
                        System.out.println("\n\u001B[33mBARRELS ATIVOS\u001B[0m");
                        System.out.println("-".repeat(30));
                        List<String> barrels = gateway.getActiveBarrels();
                        for (String barrel : barrels) {
                            System.out.println("Barrel -> " + barrel);
                        }
                        System.out.println("Cache size: " + gateway.getCacheSize() + " entradas");
                        break;
                        
                    case "5":
                        System.out.println("\n\u001B[33mEncerrando o cliente...\u001B[0m");
                        keyboard.close();
                        System.exit(0);
                        break;
                        
                    default:
                        System.out.println("Opção inválida! Por favor, escolha 1-5.");
                        break;
                }
                
                System.out.println("\n\u001B[33mPressione Enter para continuar...\u001B[0m");
                keyboard.nextLine();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}