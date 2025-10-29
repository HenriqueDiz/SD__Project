package downloader;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import common.ConfigReader;
import common.Utils;
import gateway.GatewayInterface;
import queue.URLQueueInterface;

public class Downloader extends UnicastRemoteObject implements DownloaderInterface {

    private static int counter = 0;

    public Downloader() throws java.rmi.RemoteException {
        super();
    }

    public int getProcessorURLsCount () throws java.rmi.RemoteException {
        return counter;
    }

    public static void main(String[] args) {
        try {

            int gatewayPort;
            int queuePort;

            if (args.length == 2) {
                gatewayPort = Utils.validatePort(args[0]);
                queuePort = Utils.validatePort(args[1]);
            } else {
                gatewayPort = new ConfigReader("gateway").getPort();
                queuePort = new ConfigReader("queue").getPort();
            }
        
            // MUDANÇA: Conectar ao Gateway em vez do Barrel direto
            GatewayInterface gateway = (GatewayInterface) LocateRegistry.getRegistry("localhost", gatewayPort).lookup("gateway");
            URLQueueInterface urlQueue = (URLQueueInterface) LocateRegistry.getRegistry("localhost", queuePort).lookup("urlqueue");
            
            System.out.println("Downloader iniciado");
            System.out.println("Conectado ao Gateway na porta " + gatewayPort);
            System.out.println("Conectado ao URLQueue na porta " + queuePort);
            
            while (true) {
                String url = urlQueue.takeNext();
                if (url.equals("")) {
                    try {
                        System.out.println("Fila vazia, dormindo...");
                        Thread.sleep(1000);
                        continue;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                System.out.println("Processando: " + url);
                boolean success = false;
                int attempts = 0;

                while (!success && attempts <= 3) {
                    try {
                        Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") 
                            .get();
                        StringTokenizer tokens = new StringTokenizer(doc.text());
                        
                        int wordCount = 0;
                        while (tokens.hasMoreElements()) {
                            String novaPalavra = tokens.nextToken().toLowerCase();
                            novaPalavra = novaPalavra.replaceAll("[^a-zA-Z0-9]", "");
                            if (!novaPalavra.isEmpty() && novaPalavra.length() > 2) {
                                gateway.addToIndex(novaPalavra, url);
                                wordCount++;
                            }
                        }

                        Elements links = doc.select("a[href]");
                        int linkCount = 0;
                        for (Element link : links) {
                            String linkUrl = link.attr("abs:href");
                            if (isValidUrl(linkUrl)) {
                                urlQueue.putNew(linkUrl, false);
                                linkCount++;
                            }
                        }
                        
                        System.out.println("Processado: " + wordCount + " palavras, " + linkCount + " links");
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