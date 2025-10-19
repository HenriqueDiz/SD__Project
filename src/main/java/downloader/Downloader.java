package downloader;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import barrel.BarrelInterface;
import common.PageInfo;

public class Downloader {

    private final BarrelInterface barrel;

    public Downloader(String barrelHost, int barrelPort) throws Exception {
        Registry registry = LocateRegistry.getRegistry(barrelHost, barrelPort);
        this.barrel = (BarrelInterface) registry.lookup("BarrelService");
        System.out.println("[Downloader] Ligado ao Barrel em " + barrelHost + ":" + barrelPort);
    }

    public void downloadPage(String url) {
        try {
            System.out.println("[Downloader] A descarregar: " + url);

            Document doc = Jsoup.connect(url).get();

            String title = doc.title();
            String text = doc.body().text();

            String snippet = text.length() > 200 ? text.substring(0, 200) + "..." : text;

            Set<String> links = new HashSet<>();
            Elements linkElements = doc.select("a[href]");
            for (Element link : linkElements) {
                String href = link.attr("abs:href");
                if (href.startsWith("http"))
                    links.add(href);
            }

            PageInfo page = new PageInfo(url, title, snippet, links);

            barrel.addPage(page);

            System.out.println("[Downloader] Página enviada para o Barrel: " + title + " (" + links.size() + " links)");

        } catch (IOException e) {
            System.err.println("[Downloader] Erro ao descarregar página: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[Downloader] Erro RPC: " + e.getMessage());
        }
    }

        public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: java Downloader <barrelHost> <barrelPort> <url>");
            return;
        }

        try {
            Downloader d = new Downloader(args[0], Integer.parseInt(args[1]));
            d.downloadPage(args[2]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
