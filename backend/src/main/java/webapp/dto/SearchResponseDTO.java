package webapp.dto;

import java.util.List;

/**
 * DTO para resposta de busca enviada ao frontend React.
 * 
 * Exemplo JSON enviado:
 * {
 *   "query": "java programming",
 *   "results": [...],
 *   "currentPage": 0,
 *   "totalResults": 2,
 *   "hasResults": true
 * }
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class SearchResponseDTO {
    
    private String query;
    private List<SearchResultDTO> results;
    private int currentPage;
    private int totalResults;
    private boolean hasResults;
    
    public SearchResponseDTO() {}
    
    public SearchResponseDTO(String query, List<SearchResultDTO> results, int currentPage) {
        this.query = query;
        this.results = results;
        this.currentPage = currentPage;
        this.totalResults = results.size();
        this.hasResults = !results.isEmpty();
    }
    
    // Getters e Setters
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public List<SearchResultDTO> getResults() {
        return results;
    }
    
    public void setResults(List<SearchResultDTO> results) {
        this.results = results;
        this.totalResults = results.size();
        this.hasResults = !results.isEmpty();
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public int getTotalResults() {
        return totalResults;
    }
    
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
    
    public boolean isHasResults() {
        return hasResults;
    }
    
    public void setHasResults(boolean hasResults) {
        this.hasResults = hasResults;
    }
}
