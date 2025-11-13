package barrel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import common.Utils;

/**
 * Classe responsável por gerenciar stopwords em um barrel.
 * Identifica palavras que aparecem com alta frequência em muitos documentos
 * e palavras outliers (muito frequentes em páginas individuais).
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class BarrelStopWords {

    /**
     * Interface para notificar quando stopwords são atualizadas
     */
    public interface StopwordsUpdateListener {
        /**
         * Método chamado quando as stopwords são atualizadas.
         */
        void onStopwordsUpdated();
    }

    /**
     * Percentual de documentos para considerar uma palavra como stopword
     */
    private static final double STOPWORD_THRESHOLD = 0.70; // 70% dos documentos
    /**
     * Fator para definir outliers usando IQR
     */
    private static final double OUTLIER_K = 2.0; // Fator multiplicativo para o IQR

    /**
     * Frequência mínima para considerar uma palavra como outlier
     */
    private static final int MIN_WORD_FREQUENCY = 5; // Frequência mínima para considerar outlier
    
    /**
     * Mapa de contagens de palavras por URL
     */
    private final ConcurrentHashMap<String, List<String>> urlWordCounts; // url -> list of outlier words

    /**
     * Lista de stopwords identificadas
     */
    private final List<String> stopwords;

    /**
     * Nome do barrel (para logs)
     */
    private final String barrelName;
    
    /**
     * Listener para notificar mudanças nas stopwords
     */
    private StopwordsUpdateListener listener;
    
    /**
     * Construtor
     * 
     * @param barrelName Nome do barrel (para logs)
     */
    public BarrelStopWords(String barrelName) {
        this.urlWordCounts = new ConcurrentHashMap<>();
        this.stopwords = new ArrayList<>();
        this.barrelName = barrelName;
        this.listener = null;
    }
    
    /**
     * Adiciona contagens de palavras de uma URL e identifica outliers
     * 
     * @param wordCounts    Mapa com palavra -> frequência
     * @param url           URL da página
     */
    public void addWordCounts(Map<String, Integer> wordCounts, String url) {
        List<String> outliers = new ArrayList<>();

        if (wordCounts == null || wordCounts.isEmpty()) {
            urlWordCounts.put(url, outliers);
            return;
        }
        
        // Filtra palavras com frequência mínima antes de calcular quartis
        List<Integer> frequencias = wordCounts.values().stream()
            .filter(count -> count >= MIN_WORD_FREQUENCY)
            .sorted()
            .collect(Collectors.toList());
        
        if (frequencias.isEmpty()) {
            urlWordCounts.put(url, outliers);
            return;
        }

        // Calcula os quartis
        double Q1 = calcularPercentil(frequencias.stream().mapToInt(Integer::intValue).toArray(), 25);
        double Q3 = calcularPercentil(frequencias.stream().mapToInt(Integer::intValue).toArray(), 75);
        double IQR = Q3 - Q1;
        
        // Define o limite superior usando o fator K
        double limiteSuperior = Q3 + (OUTLIER_K * IQR);
        
        // Filtra palavras realmente extremas
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            int freq = entry.getValue();
            if (freq > limiteSuperior && freq >= MIN_WORD_FREQUENCY) {
                outliers.add(entry.getKey());
            }
        }
        
        urlWordCounts.put(url, outliers);

        if (!outliers.isEmpty()) {
            findStopwords();
        }
    }
    
    /**
     * Calcula um percentil de um array de dados ordenados
     * 
     * @param dados         Array de dados (já ordenado)
     * @param percentil     Percentil desejado (0-100)
     * @return              Valor do percentil
     */
    private double calcularPercentil(int[] dados, double percentil) {
        if (dados.length == 0) {
            return 0.0;
        }
        
        int n = dados.length;
        double posicao = (percentil / 100.0) * (n - 1);
        int indiceInferior = (int) Math.floor(posicao);
        int indiceSuperior = (int) Math.ceil(posicao);
        
        if (indiceInferior == indiceSuperior) {
            return dados[indiceInferior];
        }
        
        // Interpolação linear entre os dois pontos mais próximos
        double valorInferior = dados[indiceInferior];
        double valorSuperior = dados[indiceSuperior];
        double fracao = posicao - indiceInferior;
        
        return valorInferior + fracao * (valorSuperior - valorInferior);
    }

    /**
     * Identifica stopwords baseado na frequência em documentos
     * Uma palavra se torna stopword se aparecer como outlier em X% dos documentos
     */
    private void findStopwords() {
        if (urlWordCounts == null || urlWordCounts.isEmpty()) {
            return;
        }
        
        Set<String> newStopwordsSet = new HashSet<>();
        int totalDocs = urlWordCounts.size();
        int limiar = (int) Math.ceil(totalDocs * STOPWORD_THRESHOLD);
        
        // Conta ocorrências de cada palavra
        Map<String, Integer> contagemPalavras = new HashMap<>();
        for (List<String> lista : urlWordCounts.values()) {
            if (lista != null) {
                Set<String> palavrasUnicas = new HashSet<>(lista);
                for (String palavra : palavrasUnicas) {
                    contagemPalavras.merge(palavra, 1, Integer::sum);
                }
            }
        }
        
        // Verifica quais palavras atingem o limiar
        for (Map.Entry<String, Integer> entry : contagemPalavras.entrySet()) {
            if (entry.getValue() >= limiar) {
                newStopwordsSet.add(entry.getKey());
            }
        }
        
        // Verifica se houve mudanças
        Set<String> added = new HashSet<>(newStopwordsSet);
        added.removeAll(stopwords);
        
        Set<String> removed = new HashSet<>(stopwords);
        removed.removeAll(newStopwordsSet);
        
        if (!added.isEmpty() || !removed.isEmpty()) {
            printStopwordsUpdate(added, removed, contagemPalavras, totalDocs, limiar);
            
            // Atualiza a lista de stopwords
            stopwords.clear();
            stopwords.addAll(newStopwordsSet);
            
            // Notifica listener se configurado
            if (listener != null) {
                listener.onStopwordsUpdated();
            }
        }
    }
    
    /**
     * Imprime um relatório formatado das mudanças nas stopwords
     * 
     * @param added                Palavras adicionadas como stopwords
     * @param removed              Palavras removidas das stopwords
     * @param contagemPalavras     Mapa de contagem de palavras
     * @param totalDocs            Total de documentos analisados
     * @param limiar               Limiar usado para definir stopwords
     */
    private void printStopwordsUpdate(Set<String> added, Set<String> removed, Map<String, Integer> contagemPalavras, int totalDocs, int limiar) {
        System.out.println("\n" + "=".repeat(68));
        System.out.println(Utils.bold(Utils.yellow("STOPWORDS UPDATE")));
        System.out.println("=".repeat(68));
        System.out.println(Utils.blue("Barrel:") + " " + Utils.bold(barrelName));
        System.out.println(Utils.blue("Total de documentos:") + " " + Utils.bold(String.valueOf(totalDocs)));
        
        String limiarStr = limiar + " docs (" + (int)(STOPWORD_THRESHOLD * 100) + "%)";
        System.out.println(Utils.blue("Limiar para stopwords:") + " " + Utils.bold(limiarStr));
        System.out.println("-".repeat(68));
        
        if (!added.isEmpty()) {
            System.out.println(Utils.green("ADICIONADAS") + " (" + added.size() + "):");
            List<String> addedList = new ArrayList<>(added);
            addedList.sort(String::compareTo);
            
            int maxShow = Math.min(10, addedList.size());
            for (int i = 0; i < maxShow; i++) {
                String word = addedList.get(i);
                int count = contagemPalavras.get(word);
                System.out.println("   - " + Utils.bold(word) + " (" + count + " docs)");
            }
            
            if (addedList.size() > 10) {
                System.out.println(Utils.yellow("   ... e mais " + (addedList.size() - 10) + " palavra(s)"));
            }
        }
        
        if (!removed.isEmpty()) {
            if (!added.isEmpty()) {
                System.out.println();
            }
            System.out.println(Utils.red("REMOVIDAS") + " (" + removed.size() + "):");
            List<String> removedList = new ArrayList<>(removed);
            removedList.sort(String::compareTo);
            
            int maxShow = Math.min(10, removedList.size());
            for (int i = 0; i < maxShow; i++) {
                String word = removedList.get(i);
                System.out.println("   - " + Utils.bold(word));
            }
            
            if (removedList.size() > 10) {
                System.out.println(Utils.yellow("   ... e mais " + (removedList.size() - 10) + " palavra(s)"));
            }
        }
        
        System.out.println("-".repeat(68));
        System.out.println(Utils.blue("Total de stopwords ativas:") + " " + Utils.bold(String.valueOf(stopwords.size())));
        System.out.println("=".repeat(68) + "\n");
    }
    
    /**
     * Verifica se uma palavra é stopword
     * 
     * @param palavra   Palavra a verificar
     * @return          true se for stopword
     */
    public boolean isStopword(String palavra) {
        return stopwords.contains(palavra);
    }
    
    /**
     * Obtém a lista de stopwords
     * 
     * @return Cópia da lista de stopwords
     */
    public List<String> getStopwords() {
        return new ArrayList<>(stopwords);
    }
    
    /**
     * Obtém o número de stopwords
     * 
     * @return Número de stopwords
     */
    public int getStopwordsCount() {
        return stopwords.size();
    }
    
    /**
     * Limpa todos os dados
     */
    public void clear() {
        urlWordCounts.clear();
        stopwords.clear();
    }
    
    /**
     * Define o listener para notificações de mudanças nas stopwords
     * 
     * @param listener Listener a ser notificado
     */
    public void setListener(StopwordsUpdateListener listener) {
        this.listener = listener;
    }
    
    /**
     * Faz merge de stopwords de outro barrel
     * 
     * @param externalStopwords Stopwords de outro barrel
     */
    public synchronized void mergeStopwords(List<String> externalStopwords) {
        if (externalStopwords == null || externalStopwords.isEmpty()) {
            return;
        }
        
        Set<String> added = new HashSet<>();
        for (String word : externalStopwords) {
            if (!stopwords.contains(word)) {
                stopwords.add(word);
                added.add(word);
            }
        }
        
        if (!added.isEmpty()) {
            System.out.println("\n" + "=".repeat(68));
            System.out.println(Utils.bold(Utils.blue("STOPWORDS SYNC FROM PEER")));
            System.out.println("=".repeat(68));
            System.out.println(Utils.blue("Barrel:") + " " + Utils.bold(barrelName));
            System.out.println(Utils.blue("Recebidas de outro barrel:") + " " + Utils.bold(String.valueOf(added.size()) + " novas stopwords"));
            System.out.println("-".repeat(68));
            
            List<String> addedList = new ArrayList<>(added);
            addedList.sort(String::compareTo);
            int maxShow = Math.min(10, addedList.size());
            for (int i = 0; i < maxShow; i++) {
                System.out.println("   - " + Utils.bold(addedList.get(i)));
            }
            if (addedList.size() > 10) {
                System.out.println(Utils.yellow("   ... e mais " + (addedList.size() - 10) + " palavra(s)"));
            }
            
            System.out.println("-".repeat(68));
            System.out.println(Utils.blue("Total de stopwords ativas:") + " " + Utils.bold(String.valueOf(stopwords.size())));
            System.out.println("=".repeat(68));
        }
    }
}