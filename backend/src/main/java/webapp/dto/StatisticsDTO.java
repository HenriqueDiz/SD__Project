package webapp.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DTO para expor estatísticas via REST.
 */
public class StatisticsDTO {
    private Map<String, Integer> topSearches = new LinkedHashMap<>();
    private Map<String, Long> averageResponseTime = new LinkedHashMap<>(); // nanos

    public StatisticsDTO() {}

    public StatisticsDTO(Map<String, Integer> topSearches, Map<String, Long> averageResponseTime) {
        this.topSearches = topSearches != null ? topSearches : new LinkedHashMap<>();
        this.averageResponseTime = averageResponseTime != null ? averageResponseTime : new LinkedHashMap<>();
    }

    public Map<String, Integer> getTopSearches() {
        return topSearches;
    }

    public void setTopSearches(Map<String, Integer> topSearches) {
        this.topSearches = topSearches;
    }

    public Map<String, Long> getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(Map<String, Long> averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }
}
