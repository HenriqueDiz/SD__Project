package webapp.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DTO (Data Transfer Object) para transferir estatísticas do sistema.
 * 
 * Este objeto é serializado para JSON e enviado para o frontend React.
 * Contém informações sobre pesquisas mais populares e tempo de resposta dos barrels.
 * 
 * Exemplo JSON:
 * {
 *   "topSearches": {
 *     "java": 15,
 *     "python": 10,
 *     "javascript": 8
 *   },
 *   "averageResponseTime": {
 *     "Barrel1:8001": 125000,
 *     "Barrel2:8002": 98000
 *   }
 * }
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class StatisticsDTO {
    
    /**
     * Mapa das pesquisas mais frequentes (palavra -> contagem).
     */
    private Map<String, Integer> topSearches = new LinkedHashMap<>();
    
    /**
     * Mapa do tempo médio de resposta dos barrels (nome:porta -> tempo em nanossegundos).
     */
    private Map<String, Long> averageResponseTime = new LinkedHashMap<>();

    /**
     * Construtor padrão (necessário para serialização JSON).
     */
    public StatisticsDTO() {}

    /**
     * Construtor com parâmetros.
     * 
     * @param topSearches           Mapa das pesquisas mais frequentes
     * @param averageResponseTime   Mapa dos tempos médios de resposta
     */
    public StatisticsDTO(Map<String, Integer> topSearches, Map<String, Long> averageResponseTime) {
        this.topSearches = topSearches != null ? topSearches : new LinkedHashMap<>();
        this.averageResponseTime = averageResponseTime != null ? averageResponseTime : new LinkedHashMap<>();
    }

    /**
     * Obtém o mapa das pesquisas mais frequentes.
     * 
     * @return Mapa de pesquisas e suas contagens
     */
    public Map<String, Integer> getTopSearches() {
        return topSearches;
    }

    /**
     * Define o mapa das pesquisas mais frequentes.
     * 
     * @param topSearches Mapa de pesquisas e suas contagens
     */
    public void setTopSearches(Map<String, Integer> topSearches) {
        this.topSearches = topSearches;
    }

    /**
     * Obtém o mapa dos tempos médios de resposta dos barrels.
     * 
     * @return Mapa de barrels e seus tempos médios (em nanossegundos)
     */
    public Map<String, Long> getAverageResponseTime() {
        return averageResponseTime;
    }

    /**
     * Define o mapa dos tempos médios de resposta dos barrels.
     * 
     * @param averageResponseTime Mapa de barrels e seus tempos médios
     */
    public void setAverageResponseTime(Map<String, Long> averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }
}
