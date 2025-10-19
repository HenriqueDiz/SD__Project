package downloader;

import java.rmi.registry.*;
import barrel.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Downloader extends UnicastRemoteObject implements DownloaderInterface {

    public Downloader() throws java.rmi.RemoteException {
        super();
    }

    public void printOnWorker(String toPrint) throws java.rmi.RemoteException {
        System.out.println(toPrint);
    }

    public static void main(String[] args) {
        try {
            if (args.length < 3) {
            System.out.println("Uso: java Downloader <palavra> <barrelPort> <url>");
            return;
            }
            Downloader robot = new Downloader();
            int port = Integer.parseInt(args[1]);
            BarrelInterface index = (BarrelInterface) LocateRegistry.getRegistry(port).lookup("index");
            index.subscribeRobot(robot);
            index.addToIndex(args[0],args[2]);
            index.putNew(args[2]);
            while (true) {
                String url = index.takeNext();
                if(url==""){
                    try {
                        System.out.println("No URLs, sleeping...");
                        Thread.sleep(1000);
                        continue;
                    } catch (Exception e) {
                        // todo: handle exception
                    }
                }
                System.out.println(url);
                Document doc = Jsoup.connect(url).get();

                StringTokenizer tokens = new StringTokenizer(doc.text());

                int countTokens = 0;
                while (tokens.hasMoreElements()){
                    String novaPalavra = tokens.nextToken().toLowerCase();
                    index.addToIndex(novaPalavra, url);
                }

                Elements links = doc.select("a[href]");
                for (Element link : links){
                    index.putNew(link.attr("abs:href"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}