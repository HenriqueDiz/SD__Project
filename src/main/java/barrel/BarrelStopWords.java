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
    
    private static final double STOPWORD_THRESHOLD = 0.25; // 25% dos documentos
    private static final double OUTLIER_K = 2.0; // Fator multiplicativo para o IQR
    private static final int MIN_WORD_FREQUENCY = 5; // Frequência mínima para considerar outlier
    
    private final ConcurrentHashMap<String, List<String>> urlWordCounts; // url -> list of outlier words
    private final List<String> stopwords;
    private final String barrelName;
    
    /**
     * Construtor
     * 
     * @param barrelName Nome do barrel (para logs)
     */
    public BarrelStopWords(String barrelName) {
        this.urlWordCounts = new ConcurrentHashMap<>();
        this.stopwords = new ArrayList<>();
        this.barrelName = barrelName;
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
        }
    }
    
    /**
     * Imprime um relatório formatado das mudanças nas stopwords
     */
    private void printStopwordsUpdate(Set<String> added, Set<String> removed, Map<String, Integer> contagemPalavras, int totalDocs, int limiar) {
        System.out.println("\n" + "╔" + "═".repeat(68) + "╗");
        System.out.println("║" + Utils.bold(Utils.yellow(" ".repeat(20) + "STOPWORDS UPDATE" + " ".repeat(32))) + "║");
        System.out.println("╠" + "═".repeat(68) + "╣");
        System.out.println(Utils.blue("Barrel:") + " " + Utils.bold(barrelName) + " ".repeat(61 - barrelName.length()));
        System.out.println(Utils.blue("Total de documentos:") + " " + Utils.bold(String.valueOf(totalDocs)) + " ".repeat(47 - String.valueOf(totalDocs).length()));
        System.out.println(Utils.blue("Limiar para stopwords:") + " " + Utils.bold(limiar + " documentos (" + (int)(STOPWORD_THRESHOLD * 100) + "%)") + " ".repeat(30 - String.valueOf(limiar).length()));
        
        if (!added.isEmpty()) {
            System.out.println(Utils.green("ADICIONADAS") + " (" + added.size() + "):" + " ".repeat(49 - String.valueOf(added.size()).length()));
            List<String> addedList = new ArrayList<>(added);
            addedList.sort(String::compareTo);
            
            int maxShow = Math.min(10, addedList.size());
            for (int i = 0; i < maxShow; i++) {
                String word = addedList.get(i);
                int count = contagemPalavras.get(word);
                String line = "   - " + Utils.bold(word) + " (" + count + " docs)";
                System.out.println(line + " ".repeat(67 - Utils.stripAnsi(line).length()));
            }
            
            if (addedList.size() > 10) {
                String more = "   ... e mais " + (addedList.size() - 10) + " palavra(s)";
                System.out.println(Utils.yellow(more) + " ".repeat(67 - more.length()));
            }
        }
        
        if (!removed.isEmpty()) {
            if (!added.isEmpty()) {
            }
            System.out.println(Utils.red("REMOVIDAS") + " (" + removed.size() + "):" + " ".repeat(51 - String.valueOf(removed.size()).length()));
            List<String> removedList = new ArrayList<>(removed);
            removedList.sort(String::compareTo);
            
            int maxShow = Math.min(10, removedList.size());
            for (int i = 0; i < maxShow; i++) {
                String word = removedList.get(i);
                String line = "   - " + Utils.bold(word);
                System.out.println(line + " ".repeat(67 - Utils.stripAnsi(line).length()));
            }
            
            if (removedList.size() > 10) {
                String more = "   ... e mais " + (removedList.size() - 10) + " palavra(s)";
                System.out.println(Utils.yellow(more) + " ".repeat(67 - more.length()));
            }
        }
        
        System.out.println(Utils.blue("Total de stopwords ativas:") + " " + Utils.bold(String.valueOf(stopwords.size())) + " ".repeat(40 - String.valueOf(stopwords.size()).length()));
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
}