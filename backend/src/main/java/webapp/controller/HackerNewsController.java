package webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webapp.service.HackerNewsService;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller para operações relacionadas ao HackerNews.
 * 
 * Endpoints:
 * - POST /api/hackernews/index-top50 -> Indexa as top 50 stories do HackerNews
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
@RestController
@RequestMapping("/api/hackernews")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://localhost:3000", "https://localhost:3001", "https://localhost:3002"})
public class HackerNewsController {
    
    private final HackerNewsService hackerNewsService;
    
    /**
     * Construtor com injeção de dependência.
     * 
     * @param hackerNewsService Serviço do HackerNews
     */
    @Autowired
    public HackerNewsController(HackerNewsService hackerNewsService) {
        this.hackerNewsService = hackerNewsService;
    }
    
    /**
     * Indexa as top 50 stories do HackerNews.
     *
     * POST http://localhost:8443/api/hackernews/index-top50
     *
     * Resposta JSON:
     * <pre>
     * {
     *   "success": true,
     *   "message": "Indexação do HackerNews iniciada",
     *   "indexed": 50
     * }
     * </pre>
     * 
     * @return ResponseEntity com resultado da operação
     */
    @PostMapping("/index-top50")
    public ResponseEntity<Map<String, Object>> indexTop50() {
        try {
            int indexed = hackerNewsService.indexTopStoriesSync();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "HackerNews Top 50 indexado com sucesso");
            response.put("indexed", indexed);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Erro ao indexar HackerNews");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
