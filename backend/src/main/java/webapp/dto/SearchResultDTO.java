package webapp.dto;

import java.io.Serializable;

/**
 * DTO (Data Transfer Object) para transferir resultados de busca.
 * 
 * Este objeto é serializado para JSON e enviado para o frontend React.
 * 
 * Exemplo JSON:
 * {
 *   "url": "https://example.com",
 *   "title": "Example Domain",
 *   "description": "This domain is for use in illustrative examples...",
 *   "references": 42
 * }
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class SearchResultDTO implements Serializable {
    
    /**
     * URL do resultado da busca.
     */
    private String url;
    
    /**
     * Título da página.
     */
    private String title;
    
    /**
     * Descrição/snippet da página.
     */
    private String description;
    
    /**
     * Número de inbound links (referências).
     */
    private int references;
    
    /**
     * Flag para identificar se é do HackerNews.
     */
    private boolean isHackerNews;
    
    /**
     * Construtor padrão vazio (necessário para serialização JSON).
     */
    public SearchResultDTO() {}
    
    /**
     * Construtor com parâmetros básicos.
     * 
     * @param url         URL do resultado
     * @param title       Título da página
     * @param description Descrição/snippet da página
     * @param references  Número de inbound links
     */
    public SearchResultDTO(String url, String title, String description, int references) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.references = references;
        this.isHackerNews = false;
    }
    
    /**
     * Construtor com todos os parâmetros.
     * 
     * @param url          URL do resultado
     * @param title        Título da página
     * @param description  Descrição/snippet da página
     * @param references   Número de inbound links
     * @param isHackerNews Flag indicando se é do HackerNews
     */
    public SearchResultDTO(String url, String title, String description, int references, boolean isHackerNews) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.references = references;
        this.isHackerNews = isHackerNews;
    }
    
    /**
     * Obtém a URL do resultado.
     * 
     * @return URL da página
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Define a URL do resultado.
     * 
     * @param url URL da página
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Obtém o título da página.
     * 
     * @return Título da página
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Define o título da página.
     * 
     * @param title Título da página
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Obtém a descrição da página.
     * 
     * @return Descrição/snippet da página
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Define a descrição da página.
     * 
     * @param description Descrição/snippet da página
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Obtém o número de referências (inbound links).
     * 
     * @return Número de páginas que linkam para esta
     */
    public int getReferences() {
        return references;
    }
    
    /**
     * Define o número de referências.
     * 
     * @param references Número de inbound links
     */
    public void setReferences(int references) {
        this.references = references;
    }
    
    /**
     * Verifica se o resultado é do HackerNews.
     * 
     * @return true se for do HackerNews, false caso contrário
     */
    public boolean isHackerNews() {
        return isHackerNews;
    }
    
    /**
     * Define se o resultado é do HackerNews.
     * 
     * @param isHackerNews true se for do HackerNews
     */
    public void setHackerNews(boolean isHackerNews) {
        this.isHackerNews = isHackerNews;
    }
    
    @Override
    public String toString() {
        return "SearchResultDTO{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", references=" + references +
                ", isHackerNews=" + isHackerNews +
                '}';
    }
}
