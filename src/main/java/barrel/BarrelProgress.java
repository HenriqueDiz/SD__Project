package barrel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import common.Utils;

public class BarrelProgress implements Serializable {

    private Map<String, HashSet<String>> indexedItems;
    private Map<String, HashSet<String>> urlsIndexed;

    public BarrelProgress() {
        this.indexedItems = new HashMap<>();
        this.urlsIndexed = new HashMap<>();
    }

    public BarrelProgress(Map<String, HashSet<String>> indexedItems, Map<String, HashSet<String>> urlsIndexed) {
        this.indexedItems = indexedItems != null ? indexedItems : new HashMap<>();
        this.urlsIndexed = urlsIndexed != null ? urlsIndexed : new HashMap<>();
    }

    public Map<String, HashSet<String>> getIndexedItems() {
        return indexedItems;
    }

    public Map<String, HashSet<String>> getUrlsIndexed() {
        return urlsIndexed;
    }

    private static String fileName(String barrelName) {
        return barrelName + "_progress.dat";
    }

    public static boolean exists(String barrelName) {
        return new File(fileName(barrelName)).exists();
    }

    public static void save(String barrelName, BarrelProgress progress) {
        String path = fileName(barrelName);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(progress);
            System.out.println(Utils.green("Progresso do barrel salvo: " + path));
        } catch (Exception e) {
            Utils.printLogException("Erro ao salvar progresso do barrel", e);
        }
    }

    public static BarrelProgress load(String barrelName) {
        String path = fileName(barrelName);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            Object obj = ois.readObject();
            if (obj instanceof BarrelProgress) {
                return (BarrelProgress) obj;
            } else {
                System.err.println(Utils.yellow("Ficheiro de progresso inválido para " + barrelName + ". A iniciar progresso vazio."));
            }
        } catch (Exception e) {
            Utils.printLogException("Erro ao carregar progresso do barrel", e);
        }
        return new BarrelProgress();
    }
}