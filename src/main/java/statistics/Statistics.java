package statistics;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Statistics implements Serializable {

    /**
     * Mapa de estatísticas de busca (palavra -> contagem).
     */
    private final Map<String, Integer> searchStats = new ConcurrentHashMap<>();

    /**
     * Mapa de tempos totais de processamento dos barrels (nome -> tempo em nanos).
     */
    private final Map<String, Long> barrelTotalNanos = new ConcurrentHashMap<>();
    
    /**
     * Mapa de contagem de requisições dos barrels (nome -> contagem).
     */
    private final Map<String, Long> barrelCount = new ConcurrentHashMap<>();

    /**
     * Atualiza as estatísticas de busca para uma palavra.
     * 
     * @param word  A palavra buscada.
     */
    public void updateSearchStats(String word) throws RemoteException {
        if (word == null || word.isBlank()) return;
        searchStats.merge(word, 1, Integer::sum);
    }

    /**
     * Obtém as 10 palavras mais buscadas.
     * 
     * @return                  Mapa das 10 palavras mais buscadas e suas contagens.
     * @throws RemoteException  Se ocorrer um erro remoto.
     */
    public Map<String, Integer> getTop10Searches() throws RemoteException{
        return searchStats.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .collect(LinkedHashMap::new,
                (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                LinkedHashMap::putAll);
    }

    /**
     * Registra o tempo de resposta de um barrel.
     * 
     * @param barrelKey         A chave do barrel.
     * @param durationNanos     A duração em nanossegundos.
     */
    public void recordResponseTime(String barrelKey, long durationNanos) throws RemoteException {
        String key = (barrelKey == null || barrelKey.isBlank()) ? "unknown_barrel" : barrelKey;
        barrelTotalNanos.merge(key, durationNanos, Long::sum);
        barrelCount.merge(key, 1L, Long::sum);
    }

    /**
     * Obtém o tempo médio de resposta dos barrels.
     * 
     * @return                  Mapa de nomes de barrels para tempos médios de resposta em nanos.
     * @throws RemoteException  Se ocorrer um erro remoto.
     */
    public Map<String, Long> getAverageResponseTime() throws RemoteException {
        Map<String, Long> averages = new LinkedHashMap<>();
        barrelCount.keySet().stream()
            .sorted()
            .forEach(key -> {
                long count = barrelCount.getOrDefault(key, 0L);
                long total = barrelTotalNanos.getOrDefault(key, 0L);
                if (count > 0) {
                    averages.put(key, total / count);
                }
            });
        return averages;
    }
}