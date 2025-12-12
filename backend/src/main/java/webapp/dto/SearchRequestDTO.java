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
    
    /**
     * Construtor padrão vazio (necessário para deserialização JSON).
     */
    public SearchRequestDTO() {}
    
    /**
     * Construtor com todos os parâmetros.
     * 
     * @param query    Texto da busca (ex: "java programming")
     * @param page     Número da página (0-based)
     * @param pageSize Quantidade de resultados por página
     */
    public SearchRequestDTO(String query, int page, int pageSize) {
        this.query = query;
        this.page = page;
        this.pageSize = pageSize;
    }
    
    /**
     * Obtém o texto da busca.
     * 
     * @return Texto da query
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
     * Obtém o número da página.
     * 
     * @return Número da página (0-based)
     */
    public int getPage() {
        return page;
    }
    
    /**
     * Define o número da página.
     * 
     * @param page Número da página (0-based)
     */
    public void setPage(int page) {
        this.page = page;
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
     * Converte a query em lista de palavras.
     * Exemplo: "java programming" -&gt; ["java", "programming"]
     * 
     * @return Lista de palavras da busca
     */
    public List<String> getWords() {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return List.of(query.trim().split("\\s+"));
    }
}
