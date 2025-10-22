package downloader;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.StringTokenizer;
import queue.URLQueueInterface;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import barrel.BarrelInterface;

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
            if (args.length < 4) {
                System.out.println("Uso: java Downloader <palavra> <barrelPort> <queuePort> <url>");
                return;
            }
        
            int port = Integer.parseInt(args[1]);
            int queuePort = Integer.parseInt(args[2]);
        
            BarrelInterface server = (BarrelInterface) LocateRegistry.getRegistry("localhost",port).lookup("index");
            URLQueueInterface urlQueue = (URLQueueInterface) LocateRegistry.getRegistry("localhost", queuePort).lookup("urlqueue");
            server.addToIndex(args[0],args[3]);
            urlQueue.putNew(args[3],false);
            while (true) {
                String url = urlQueue.takeNext();
                System.out.println(url);
                boolean success = false;
                int attempts = 0;

                // Tenta baixar e processar a página até 3 vezes em caso de falha
                while (!success && attempts <= 3) {
                    try {
                    Document doc = Jsoup.connect(url)
                    // Basicamente o userAgent é para o trafico parecer que veio do firefox porque as vezes trafico que é o Curl ou do jsoup pode ser bloqueado
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") 
                    .get();
                    StringTokenizer tokens = new StringTokenizer(doc.text());
                    
                    // Indexa as palavras da página
                    while (tokens.hasMoreElements()){
                        String novaPalavra = tokens.nextToken().toLowerCase();
                        server.addToIndex(novaPalavra, url);
                    }

                    // Extrai e indexa os links da página
                    Elements links = doc.select("a[href]");
                    for (Element link : links){
                        String linkUrl = link.attr("abs:href");
                        if (isValidUrl(linkUrl)) {
                            urlQueue.putNew(linkUrl, false);
                        }
                    }
                    success = true;
                    counter++;
                    } catch (Exception e) {
                        attempts++;
                        if(attempts == 1) {
                            System.err.println("\n\u001B[31mErro ao processar " + url + ". Tentando novamente...\u001B[0m");
                            System.out.println("\n" + "=".repeat(50));
                            System.out.println("                     \u001B[31m\u001B[1mERRO\u001B[0m\u001B[0m");
                            System.out.println("=".repeat(50));
                            e.printStackTrace();
                            System.out.println("=".repeat(50));

                        } 
                        if(attempts > 3) {
                            System.err.println("\u001B[31mFalha ao processar " + url + " após 3 tentativas. Pulando...\u001B[0m");
                        } else {
                            try {
                                System.out.println("Aguardando antes de tentar novamente...");
                                Thread.sleep(2000);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método auxiliar para validar URLs
    private static boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        
        // Filtrar URLs problemáticos
        String[] blockedPatterns = {
        "facebook.com/sharer",
        "twitter.com/intent", 
        "linkedin.com/sharing",
        "mailto:",
        "lnkd.in/",
        "javascript:",
        "phabricator.wikimedia.org", // Adicionar sites problemáticos
        "#"
        };
        
        for (String pattern : blockedPatterns) {
            if (url.contains(pattern)) {
                return false;
            }
        }
        
        return url.startsWith("http://") || url.startsWith("https://");
    }

}