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
    
    private String url;
    private String title;
    private String description;
    private int references;  // Número de inbound links
    
    public SearchResultDTO() {}
    
    public SearchResultDTO(String url, String title, String description, int references) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.references = references;
    }
    
    // Getters e Setters (necessários para serialização JSON)
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getReferences() {
        return references;
    }
    
    public void setReferences(int references) {
        this.references = references;
    }
    
    @Override
    public String toString() {
        return "SearchResultDTO{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", references=" + references +
                '}';
    }
}
