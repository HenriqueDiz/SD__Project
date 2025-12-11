package downloader;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import barrel.BarrelInterface;
import common.ConfigReader;
import common.TextLanguageDetector;
import common.Utils;
import gateway.GatewayInterface;
import queue.URLQueueInterface;


/**
 * Classe responsável por baixar e processar páginas web.
 * Conecta-se ao Gateway para obter a lista de barrels ativos e ao URLQueue para obter URLs a processar.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class Downloader extends UnicastRemoteObject implements DownloaderInterface {

    /**
     * Contador de URLs processadas.
     */
    private static int counter = 0;


    /**
     * Construtor da classe Downloader.
     * 
     * @throws java.rmi.RemoteException Se ocorrer um erro de comunicação remota.
     */
    public Downloader() throws java.rmi.RemoteException {
        super();
    }


    /**    
     * Obtém o número de URLs processadas pelo downloader.
     * 
     * @return Número de URLs processadas.
     * @throws java.rmi.RemoteException Se ocorrer um erro de comunicação remota.
     */
    public int getProcessorURLsCount () throws java.rmi.RemoteException {
        return counter;
    }


    /**     
     * Método principal para iniciar o downloader.
     * Conecta-se ao Gateway e ao URLQueue, processa URLs continuamente.
     * 
     * @param args Argumentos da linha de comando (opcionais: gatewayPort, queuePort, gatewayHost, queueHost)
     */
    public static void main(String[] args) {
        try {

            int gatewayPort;
            int queuePort;
            String gatewayHost;
            String queueHost;

            if (args.length == 4) {
                gatewayPort = Utils.validatePort(args[0]);
                queuePort = Utils.validatePort(args[1]);
                gatewayHost = args[2];
                queueHost = args[3];
            } else {
                ConfigReader gatewayConfig = new ConfigReader("gateway");
                gatewayPort = gatewayConfig.getPort();
                gatewayHost = gatewayConfig.getHost();

                ConfigReader queueConfig = new ConfigReader("queue");
                queueHost = queueConfig.getHost();
                queuePort = queueConfig.getPort();
            }
        
            // MUDANÇA: Conectar ao Gateway em vez do Barrel direto
            GatewayInterface gateway = (GatewayInterface) LocateRegistry.getRegistry(gatewayHost, gatewayPort).lookup("gateway");
            URLQueueInterface urlQueue = (URLQueueInterface) LocateRegistry.getRegistry(queueHost, queuePort).lookup("urlqueue");
            
            System.out.println("Downloader iniciado");
            System.out.println("Conectado ao Gateway na porta " + gatewayPort);
            System.out.println("Conectado ao URLQueue na porta " + queuePort);
            
            while (true) {
                String url = urlQueue.takeNext();                
                System.out.println("Processando: " + url);
                boolean success = false;
                int attempts = 0;

                while (!success && attempts <= 3) {
                    try {
                        Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") 
                            .get();
                        String documentText = doc.text();
                        String language = TextLanguageDetector.detectLanguage(documentText);
                        StringTokenizer tokens = new StringTokenizer(documentText);
                        
                        // Buscar lista de barrels ativos
                        List<String> activeBarrels = gateway.getActiveBarrels();
                        if (activeBarrels.isEmpty()) {
                            System.out.println(Utils.red("Nenhum barrel ativo disponível. Aguardando..."));
                            Thread.sleep(2000); // Espera antes de tentar novamente
                            continue;
                        }

                        HashSet<String> linksFound = new HashSet<>();
                        Map<String, Integer> wordCounts = new HashMap<>(); // Contador de frequência
                        while (tokens.hasMoreElements()) {
                            String novaPalavra = tokens.nextToken().toLowerCase();
                            novaPalavra = novaPalavra.replaceAll("[^a-zA-Z0-9]", "");
                            if (!novaPalavra.isEmpty() && novaPalavra.length() > 2) {
                                wordCounts.merge(novaPalavra, 1, Integer::sum);
                                for (String barrel : activeBarrels) {
                                    try {
                                        // Validar o formato "nome:porta:host:indexSize"
                                        String[] barrelParts = barrel.split(":");
                                        if (barrelParts.length != 4) {
                                            System.err.println(Utils.red("Formato inválido para barrel: " + barrel));
                                            continue;
                                        }

                                        String barrelName = barrelParts[0];
                                        int barrelPort = Integer.parseInt(barrelParts[1]);
                                        String barrelHost = barrelParts[2];
                                        Registry barrelRegistry = LocateRegistry.getRegistry(barrelHost, barrelPort);
                                        BarrelInterface barrelInterface = (BarrelInterface) barrelRegistry.lookup(barrelName);
                                        barrelInterface.addToIndex(novaPalavra, url, true);
                            
                                    } catch (Exception e) {
                                        System.err.println(Utils.red("Erro ao enviar palavra para o barrel: " + barrel));
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        Elements links = doc.select("a[href]");
                        int linkCount = 0;
                        for (Element link : links) {
                            String linkUrl = link.attr("abs:href");
                            if (isValidUrl(linkUrl)) {
                                urlQueue.putNew(linkUrl, false);
                                linkCount++;
                                linksFound.add(linkUrl);
                            }
                        }

                        // Só agora associa os links encontrados ao URL (apenas uma vez por página!)
                        for (String barrel : activeBarrels) {
                            try {
                                String[] barrelParts = barrel.split(":");
                                if (barrelParts.length != 4) {
                                    System.err.println(Utils.red("Formato inválido para barrel: " + barrel));
                                    continue;
                                }

                                String barrelName = barrelParts[0];
                                int barrelPort = Integer.parseInt(barrelParts[1]);
                                String barrelHost = barrelParts[2];
                                Registry barrelRegistry = LocateRegistry.getRegistry(barrelHost, barrelPort);
                                BarrelInterface barrelInterface = (BarrelInterface) barrelRegistry.lookup(barrelName);
                                barrelInterface.addUrlsForIndexedUrl(url, linksFound);
                                barrelInterface.addWordCounts(wordCounts, url, language);
                            } catch (Exception e) {
                                System.err.println(Utils.red("Erro ao enviar URLs para o barrel: " + barrel));
                                e.printStackTrace();
                            }
                        }
                        
                        System.out.println("Processado: " + url + " | Palavras: " + wordCounts.size() + " | Links: " + linkCount + " | Língua: " + language);
                        success = true;
                        counter++;
                        
                    } catch (Exception e) {
                        attempts++;
                        if (attempts == 1) {
                            System.out.println(Utils.red("Erro ao processar " + url + ". Tentando novamente..."));
                            System.out.println("\n" + "=".repeat(70));
                            System.out.println(Utils.red("                               ERRO"));
                            System.out.println("=".repeat(70));
                            e.printStackTrace();
                            System.out.println("=".repeat(70));
                        } 
                        if (attempts > 3) {
                            System.out.println(Utils.red("Falha ao processar " + url + " após 3 tentativas. Pulando..."));
                        } else {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Valida uma URL para garantir que não contém padrões indesejados.
     * 
     * @param url URL a ser validada.
     * @return true se a URL for válida, false caso contrário.
     */
    private static boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        
        String[] blockedPatterns = {
            "facebook.com/sharer", "twitter.com/intent", "linkedin.com/sharing",
            "mailto:", "lnkd.in/", "javascript:", "phabricator.wikimedia.org", "#"
        };
        
        for (String pattern : blockedPatterns) {
            if (url.contains(pattern)) {
                return false;
            }
        }
        
        return url.startsWith("http://") || url.startsWith("https://");
    }
}