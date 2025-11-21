package webapp.dto;

import java.util.List;

/**
 * DTO para requisições de busca vindas do frontend React.
 * 
 * Exemplo JSON recebido:
 * {
 *   "query": "java programming",
 *   "page": 0,
 *   "pageSize": 10
 * }
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class SearchRequestDTO {
    
    private String query;
    private int page = 0;
    private int pageSize = 10;
    
    public SearchRequestDTO() {}
    
    public SearchRequestDTO(String query, int page, int pageSize) {
        this.query = query;
        this.page = page;
        this.pageSize = pageSize;
    }
    
    // Getters e Setters
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    /**
     * Converte a query em lista de palavras.
     * "java programming" -> ["java", "programming"]
     */
    public List<String> getWords() {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return List.of(query.trim().split("\\s+"));
    }
}
