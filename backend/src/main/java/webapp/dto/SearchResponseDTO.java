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
    private int pageSize;
    private int totalResults;
    private int totalPages;
    private boolean hasResults;
    
    /**
     * Construtor padrão vazio (necessário para serialização JSON).
     */
    public SearchResponseDTO() {}
    
    /**
     * Construtor com todos os parâmetros.
     * 
     * @param query        Texto da busca original
     * @param results      Lista de resultados encontrados
     * @param currentPage  Página atual (0-based)
     * @param pageSize     Tamanho da página
     * @param totalResults Total de resultados encontrados
     */
    public SearchResponseDTO(String query, List<SearchResultDTO> results, int currentPage, int pageSize, int totalResults) {
        this.query = query;
        this.results = results;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalResults = totalResults;
        this.totalPages = (int) Math.ceil((double) totalResults / pageSize);
        this.hasResults = !results.isEmpty();
    }
    
    /**
     * Obtém o texto da busca.
     * 
     * @return Texto da query original
     */
    public String getQuery() {
        return query;
    }
    
    /**
     * Define o texto da busca.
     * 
     * @param query Texto da query
     */
    public void setQuery(String query) {
        this.query = query;
    }
    
    /**
     * Obtém a lista de resultados.
     * 
     * @return Lista de resultados da busca
     */
    public List<SearchResultDTO> getResults() {
        return results;
    }
    
    /**
     * Define a lista de resultados.
     * 
     * @param results Lista de resultados da busca
     */
    public void setResults(List<SearchResultDTO> results) {
        this.results = results;
        this.hasResults = !results.isEmpty();
    }
    
    /**
     * Obtém a página atual.
     * 
     * @return Número da página atual (0-based)
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Define a página atual.
     * 
     * @param currentPage Número da página (0-based)
     */
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    /**
     * Obtém o tamanho da página.
     * 
     * @return Quantidade de resultados por página
     */
    public int getPageSize() {
        return pageSize;
    }
    
    /**
     * Define o tamanho da página.
     * 
     * @param pageSize Quantidade de resultados por página
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    /**
     * Obtém o total de resultados.
     * 
     * @return Total de resultados encontrados
     */
    public int getTotalResults() {
        return totalResults;
    }
    
    /**
     * Define o total de resultados.
     * 
     * @param totalResults Total de resultados encontrados
     */
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
        if (this.pageSize > 0) {
            this.totalPages = (int) Math.ceil((double) totalResults / this.pageSize);
        }
    }
    
    /**
     * Obtém o total de páginas.
     * 
     * @return Total de páginas disponíveis
     */
    public int getTotalPages() {
        return totalPages;
    }
    
    /**
     * Define o total de páginas.
     * 
     * @param totalPages Total de páginas
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    /**
     * Verifica se há resultados.
     * 
     * @return true se há resultados, false caso contrário
     */
    public boolean isHasResults() {
        return hasResults;
    }
    
    /**
     * Define se há resultados.
     * 
     * @param hasResults true se há resultados
     */
    public void setHasResults(boolean hasResults) {
        this.hasResults = hasResults;
    }
}
