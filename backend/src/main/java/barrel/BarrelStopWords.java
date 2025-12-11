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
    private static final double STOPWORD_THRESHOLD = 0.40; // 40% dos documentos
    /**
     * Fator para definir outliers usando IQR
     */
    private static final double OUTLIER_K = 1.2; // Fator multiplicativo para o IQR

    /**
     * Frequência mínima para considerar uma palavra como outlier
     */
    private static final int MIN_WORD_FREQUENCY = 3; // Frequência mínima para considerar outlier
    
    /**
     * Número mínimo de documentos antes de calcular stopwords
     */
    private static final int MIN_DOCUMENTS_FOR_STOPWORDS = 2; // Mínimo de docs para calcular
    
    /**
     * Mapa de contagens de palavras por URL e linguagem
     * url -> language -> list of outlier words
     */
    private final ConcurrentHashMap<String, Map<String, List<String>>> urlWordCounts;

    /**
     * Stopwords separadas por linguagem
     * language -> list of stopwords
     */
    private final ConcurrentHashMap<String, List<String>> stopwordsByLanguage;

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
        this.stopwordsByLanguage = new ConcurrentHashMap<>();
        this.barrelName = barrelName;
        this.listener = null;
    }
    
    /**
     * Adiciona contagens de palavras de uma URL e identifica outliers por linguagem
     * 
     * @param wordCounts    Mapa com palavra -> frequência
     * @param url           URL da página
     * @param language      Linguagem detetada do documento
     */
    public void addWordCounts(Map<String, Integer> wordCounts, String url, String language) {
        if (language == null || language.isEmpty() || language.equals("unknown")) {
            language = "unknown";
        }
        
        // Inicializa o mapa para esta URL se não existir
        urlWordCounts.putIfAbsent(url, new ConcurrentHashMap<>());
        Map<String, List<String>> languageMap = urlWordCounts.get(url);
        
        List<String> outliers = new ArrayList<>();

        if (wordCounts == null || wordCounts.isEmpty()) {
            languageMap.put(language, outliers);
            return;
        }
        
        // Filtra palavras com frequência mínima antes de calcular quartis
        List<Integer> frequencias = wordCounts.values().stream()
            .filter(count -> count >= MIN_WORD_FREQUENCY)
            .sorted()
            .collect(Collectors.toList());
        
        if (frequencias.isEmpty()) {
            languageMap.put(language, outliers);
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
        
        languageMap.put(language, outliers);

        if (!outliers.isEmpty()) {
            findStopwordsByLanguage(language);
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
     * Identifica stopwords baseado na frequência em documentos para uma linguagem específica
     * Uma palavra se torna stopword se aparecer como outlier em X% dos documentos dessa linguagem
     * 
     * @param language Linguagem a processar
     */
    private void findStopwordsByLanguage(String language) {
        if (urlWordCounts == null || urlWordCounts.isEmpty()) {
            return;
        }
        
        // Conta documentos por linguagem
        int docsInLanguage = 0;
        for (Map<String, List<String>> langMap : urlWordCounts.values()) {
            if (langMap.containsKey(language)) {
                docsInLanguage++;
            }
        }
        
        // Só calcula stopwords se tiver documentos suficientes
        if (docsInLanguage < MIN_DOCUMENTS_FOR_STOPWORDS) {
            return;
        }
        
        Set<String> newStopwordsSet = new HashSet<>();
        int limiar = (int) Math.ceil(docsInLanguage * STOPWORD_THRESHOLD);
        // Garante limiar mínimo de 2 documentos
        if (limiar < 2) {
            limiar = 2;
        }
        
        // Conta ocorrências de cada palavra na linguagem específica
        Map<String, Integer> contagemPalavras = new HashMap<>();
        for (Map<String, List<String>> langMap : urlWordCounts.values()) {
            List<String> lista = langMap.get(language);
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
        
        // Obtém ou cria lista de stopwords para esta linguagem
        List<String> currentStopwords = stopwordsByLanguage.getOrDefault(language, new ArrayList<>());
        
        // Verifica se houve mudanças
        Set<String> added = new HashSet<>(newStopwordsSet);
        added.removeAll(currentStopwords);
        
        Set<String> removed = new HashSet<>(currentStopwords);
        removed.removeAll(newStopwordsSet);
        
        if (!added.isEmpty() || !removed.isEmpty()) {
            printStopwordsUpdate(language, added, removed, contagemPalavras, docsInLanguage, limiar);
            
            // Atualiza a lista de stopwords para esta linguagem
            stopwordsByLanguage.put(language, new ArrayList<>(newStopwordsSet));
            
            // Notifica listener se configurado
            if (listener != null) {
                listener.onStopwordsUpdated();
            }
        }
    }
    
    /**
     * Imprime um relatório formatado das mudanças nas stopwords
     * 
     * @param language             Linguagem sendo processada
     * @param added                Palavras adicionadas como stopwords
     * @param removed              Palavras removidas das stopwords
     * @param contagemPalavras     Mapa de contagem de palavras
     * @param totalDocs            Total de documentos analisados nesta linguagem
     * @param limiar               Limiar usado para definir stopwords
     */
    private void printStopwordsUpdate(String language, Set<String> added, Set<String> removed, 
                                     Map<String, Integer> contagemPalavras, int totalDocs, int limiar) {
        System.out.println("\n" + "=".repeat(68));
        System.out.println(Utils.bold(Utils.yellow("STOPWORDS UPDATE - " + language.toUpperCase())));
        System.out.println("=".repeat(68));
        System.out.println(Utils.blue("Barrel:") + " " + Utils.bold(barrelName));
        System.out.println(Utils.blue("Linguagem:") + " " + Utils.bold(language));
        System.out.println(Utils.blue("Documentos nesta linguagem:") + " " + Utils.bold(String.valueOf(totalDocs)));
        
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
        
        List<String> currentStopwords = stopwordsByLanguage.getOrDefault(language, new ArrayList<>());
        System.out.println("-".repeat(68));
        System.out.println(Utils.blue("Total de stopwords ativas (" + language + "):") + " " + Utils.bold(String.valueOf(currentStopwords.size())));
        System.out.println("=".repeat(68) + "\n");
    }
    
    /**
     * Verifica se uma palavra é stopword em uma linguagem específica
     * 
     * @param palavra   Palavra a verificar
     * @param language  Linguagem da palavra
     * @return          true se for stopword naquela linguagem
     */
    public boolean isStopword(String palavra, String language) {
        if (language == null || language.isEmpty() || language.equals("unknown")) {
            language = "unknown";
        }
        List<String> stopwords = stopwordsByLanguage.get(language);
        return stopwords != null && stopwords.contains(palavra);
    }
    
    /**
     * Verifica se uma palavra é stopword (compatibilidade com código antigo)
     * Verifica em todas as linguagens
     * 
     * @param palavra   Palavra a verificar
     * @return          true se for stopword em qualquer linguagem
     */
    public boolean isStopword(String palavra) {
        for (List<String> stopwords : stopwordsByLanguage.values()) {
            if (stopwords.contains(palavra)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtém a lista de stopwords de uma linguagem específica
     * 
     * @param language Linguagem desejada
     * @return Cópia da lista de stopwords dessa linguagem
     */
    public List<String> getStopwords(String language) {
        List<String> stopwords = stopwordsByLanguage.get(language);
        return stopwords != null ? new ArrayList<>(stopwords) : new ArrayList<>();
    }
    
    /**
     * Obtém todas as stopwords de todas as linguagens
     * 
     * @return Cópia da lista de todas as stopwords
     */
    public List<String> getStopwords() {
        Set<String> allStopwords = new HashSet<>();
        for (List<String> stopwords : stopwordsByLanguage.values()) {
            allStopwords.addAll(stopwords);
        }
        return new ArrayList<>(allStopwords);
    }
    
    /**
     * Obtém o mapa completo de stopwords por linguagem
     * 
     * @return Cópia do mapa de stopwords por linguagem
     */
    public Map<String, List<String>> getAllStopwordsByLanguage() {
        Map<String, List<String>> copy = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : stopwordsByLanguage.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
    
    /**
     * Obtém o número total de stopwords em todas as linguagens
     * 
     * @return Número total de stopwords únicas
     */
    public int getStopwordsCount() {
        return getStopwords().size();
    }
    
    /**
     * Obtém o número de stopwords de uma linguagem específica
     * 
     * @param language Linguagem desejada
     * @return Número de stopwords dessa linguagem
     */
    public int getStopwordsCount(String language) {
        List<String> stopwords = stopwordsByLanguage.get(language);
        return stopwords != null ? stopwords.size() : 0;
    }
    
    /**
     * Limpa todos os dados
     */
    public void clear() {
        urlWordCounts.clear();
        stopwordsByLanguage.clear();
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
     * @param externalStopwordsByLanguage Stopwords de outro barrel organizadas por linguagem
     */
    public synchronized void mergeStopwords(Map<String, List<String>> externalStopwordsByLanguage) {
        if (externalStopwordsByLanguage == null || externalStopwordsByLanguage.isEmpty()) {
            return;
        }
        
        Map<String, Set<String>> addedByLanguage = new HashMap<>();
        int totalAdded = 0;
        
        for (Map.Entry<String, List<String>> entry : externalStopwordsByLanguage.entrySet()) {
            String language = entry.getKey();
            List<String> externalWords = entry.getValue();
            
            if (externalWords == null || externalWords.isEmpty()) {
                continue;
            }
            
            // Obtém ou cria lista de stopwords para esta linguagem
            List<String> currentWords = stopwordsByLanguage.computeIfAbsent(language, k -> new ArrayList<>());
            Set<String> added = new HashSet<>();
            
            for (String word : externalWords) {
                if (!currentWords.contains(word)) {
                    currentWords.add(word);
                    added.add(word);
                }
            }
            
            if (!added.isEmpty()) {
                addedByLanguage.put(language, added);
                totalAdded += added.size();
            }
        }
        
        if (totalAdded > 0) {
            System.out.println("\n" + "=".repeat(68));
            System.out.println(Utils.bold(Utils.blue("STOPWORDS SYNC FROM PEER")));
            System.out.println("=".repeat(68));
            System.out.println(Utils.blue("Barrel:") + " " + Utils.bold(barrelName));
            System.out.println(Utils.blue("Total recebido:") + " " + Utils.bold(String.valueOf(totalAdded) + " novas stopwords"));
            System.out.println("-".repeat(68));
            
            for (Map.Entry<String, Set<String>> entry : addedByLanguage.entrySet()) {
                String language = entry.getKey();
                Set<String> added = entry.getValue();
                
                System.out.println(Utils.yellow("\nLinguagem: " + language) + " (" + added.size() + " palavras)");
                
                List<String> addedList = new ArrayList<>(added);
                addedList.sort(String::compareTo);
                int maxShow = Math.min(5, addedList.size());
                for (int i = 0; i < maxShow; i++) {
                    System.out.println("   - " + Utils.bold(addedList.get(i)));
                }
                if (addedList.size() > 5) {
                    System.out.println(Utils.yellow("   ... e mais " + (addedList.size() - 5) + " palavra(s)"));
                }
                
                List<String> currentWords = stopwordsByLanguage.get(language);
                System.out.println(Utils.blue("   Total ativo (" + language + "):") + " " + Utils.bold(String.valueOf(currentWords.size())));
            }
            
            System.out.println("-".repeat(68));
            System.out.println(Utils.blue("Total global de stopwords:") + " " + Utils.bold(String.valueOf(getStopwordsCount())));
            System.out.println("=".repeat(68));
        }
    }
    
    /**
     * Faz merge de stopwords de outro barrel (compatibilidade com código antigo)
     * 
     * @param externalStopwords Stopwords de outro barrel (sem separação por linguagem)
     */
    public synchronized void mergeStopwords(List<String> externalStopwords) {
        if (externalStopwords == null || externalStopwords.isEmpty()) {
            return;
        }
        
        // Trata como linguagem desconhecida
        Map<String, List<String>> byLanguage = new HashMap<>();
        byLanguage.put("unknown", externalStopwords);
        mergeStopwords(byLanguage);
    }
}