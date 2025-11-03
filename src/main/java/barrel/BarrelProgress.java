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
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import common.Utils;

/**
 * Metodo de armazenamento do progresso do barrel.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class BarrelProgress implements Serializable {

    /**
     * Itens indexados (palavra-chave -> conjunto de URLs).
     */
    private Map<String, HashSet<String>> indexedItems;

    /**
     * URLs indexadas (URL -> conjunto de URLs associadas).
     */
    private Map<String, HashSet<String>> urlsIndexed;

    /**
     * URLs a serem indexadas (não processadas).
     */
    private BlockingDeque<String> urlsToIndex;

    /**
     * URLs já vistas.
     */
    private Set<String> seenUrls;

    /**
     * Construtor da classe BarrelProgress.
     */
    public BarrelProgress() {
        this.indexedItems = new HashMap<>();
        this.urlsIndexed = new HashMap<>();
        this.urlsToIndex = new LinkedBlockingDeque<>();
        this.seenUrls = new HashSet<>();
    }

    /**
     * Construtor da classe BarrelProgress com parâmetros.
     * 
     * @param indexedItems          Mapa de itens indexados.
     * @param urlsIndexed           Mapa de URLs indexadas.
     * @param urlsToIndex           Deque de URLs a serem indexadas.
     * @param seenUrls              Conjunto de URLs já vistas.
     * 
     */
    public BarrelProgress(Map<String, HashSet<String>> indexedItems, Map<String, HashSet<String>> urlsIndexed, BlockingDeque<String> urlsToIndex, Set<String> seenUrls) {
        this.indexedItems = indexedItems != null ? indexedItems : new HashMap<>();
        this.urlsIndexed = urlsIndexed != null ? urlsIndexed : new HashMap<>();
        this.urlsToIndex = urlsToIndex != null ? urlsToIndex : new LinkedBlockingDeque<>();
        this.seenUrls = seenUrls != null ? new HashSet<>(seenUrls) : new HashSet<>();
    }

    /**
     * Getter para os itens indexados.
     * 
     * @return Mapa de itens indexados.
     */
    public Map<String, HashSet<String>> getIndexedItems() {
        return indexedItems;
    }


    /**
     * Getter para as URLs indexadas.
     * 
     * @return Mapa de URLs indexadas.
     */
    public Map<String, HashSet<String>> getUrlsIndexed() {
        return urlsIndexed;
    }

    /**
     * Getter para as URLs a serem indexadas.
     * 
     * @return Deque de URLs a serem indexadas.
     */
    public BlockingDeque<String> getUrlsToIndex() {
        return urlsToIndex;
    }

    /**
     * Getter para as URLs já vistas.
     * 
     * @return Conjunto de URLs já vistas.
     */
    public Set<String> getSeenUrls() {
        return seenUrls;
    }    

    /**
     * Gera o nome do ficheiro de progresso com base no nome do barrel.
     * 
     * @param barrelName        Nome do barrel.
     * @return                  Nome do ficheiro de progresso.
     */
    private static String fileName(String barrelName) {
        return barrelName + "_progress.dat";
    }

    /**
     * Verifica se o ficheiro de progresso existe para o barrel especificado.
     * 
     * @param barrelName        Nome do barrel.
     * @return                  true se o ficheiro existir, false caso contrário.
     */
    public static boolean exists(String barrelName) {
        return new File(fileName(barrelName)).exists();
    }

    /**
     * Salva o progresso do barrel em um ficheiro.
     * 
     * @param barrelName        Nome do barrel.
     * @param progress          Progresso do barrel a ser salvo.
     */
    public static void save(String barrelName, BarrelProgress progress) {
        String path = fileName(barrelName);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(progress);
            System.out.println(Utils.green("Progresso do barrel salvo: " + path));
        } catch (Exception e) {
            Utils.printLogException("Erro ao salvar progresso do barrel (" + barrelName + "): " + e.getMessage(), e);
        }
    }

    /**
     * Carrega o progresso do barrel a partir de um ficheiro.
     * 
     * @param barrelName        Nome do barrel.
     * @return                  Progresso do barrel carregado.
     */
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
            Utils.printLogException("Erro ao carregar progresso do barrel (" + barrelName + "): " + e.getMessage(), e);
        }
        return new BarrelProgress();
    }
}