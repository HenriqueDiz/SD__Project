package webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webapp.dto.SearchRequestDTO;
import webapp.dto.SearchResponseDTO;
import webapp.dto.SearchResultDTO;
import webapp.service.GatewayServiceClient;

import java.util.List;

/**
 * REST Controller para operações de busca.
 * 
 * @RestController combina @Controller + @ResponseBody
 * Todos os métodos retornam JSON automaticamente
 * 
 * @RequestMapping("/api/search") define o prefixo /api/search para todas as rotas
 * 
 * Endpoints:
 * - GET  /api/search?q=java&page=0&pageSize=10  -> Buscar
 * - POST /api/search                             -> Buscar (com JSON no body)
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://localhost:3000", "https://localhost:3001", "https://localhost:3002"})
public class SearchController {
    
    private final GatewayServiceClient gatewayClient;
    
    @Autowired
    public SearchController(GatewayServiceClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }
    
    /**
     * Endpoint GET para busca (mais simples, usado para testes).
     * 
     * Exemplo de uso:
     * GET http://localhost:8080/api/search?q=java+programming&page=0&pageSize=10
     * 
     * Resposta JSON:
     * {
     *   "query": "java programming",
     *   "results": [...],
     *   "currentPage": 0,
     *   "totalResults": 5,
     *   "hasResults": true
     * }
     * 
     * @param query     Termos de busca (ex: "java programming")
     * @param page      Página (padrão: 0)
     * @param pageSize  Resultados por página (padrão: 10)
     * @return          Resposta com resultados em JSON
     */
    @GetMapping
    public ResponseEntity<SearchResponseDTO> searchGet(
            @RequestParam("q") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        
        try {
            // Criar DTO a partir dos parâmetros
            SearchRequestDTO request = new SearchRequestDTO(query, page, pageSize);
            
            // Buscar via RMI
            List<SearchResultDTO> results = gatewayClient.search(
                request.getWords(), 
                request.getPage(), 
                request.getPageSize()
            );
            
            // Obter total REAL de resultados do Gateway
            int totalResults = gatewayClient.getTotalResults(request.getWords());
            
            // Montar resposta
            SearchResponseDTO response = new SearchResponseDTO(
                query, 
                results, 
                page, 
                pageSize, 
                totalResults
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Erro na busca: " + e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SearchResponseDTO(query, List.of(), page, pageSize, 0));
        }
    }
    
    /**
     * Endpoint POST para busca (recomendado para o React).
     * 
     * Exemplo de uso no React:
     * ```javascript
     * fetch('http://localhost:8080/api/search', {
     *   method: 'POST',
     *   headers: { 'Content-Type': 'application/json' },
     *   body: JSON.stringify({
     *     query: 'java programming',
     *     page: 0,
     *     pageSize: 10
     *   })
     * })
     * .then(res => res.json())
     * .then(data => console.log(data.results))
     * ```
     * 
     * @param request   DTO com query, page e pageSize
     * @return          Resposta com resultados em JSON
     */
    @PostMapping
    public ResponseEntity<SearchResponseDTO> searchPost(@RequestBody SearchRequestDTO request) {
        
        try {
            System.out.println("Requisição de busca recebida: " + request.getQuery());
            
            // Buscar via RMI
            List<SearchResultDTO> results = gatewayClient.search(
                request.getWords(), 
                request.getPage(), 
                request.getPageSize()
            );
            
            // Obter total REAL de resultados do Gateway
            int totalResults = gatewayClient.getTotalResults(request.getWords());
            
            // Montar resposta
            SearchResponseDTO response = new SearchResponseDTO(
                request.getQuery(), 
                results, 
                request.getPage(),
                request.getPageSize(),
                totalResults
            );
            
            System.out.println("Enviando " + results.size() + " resultado(s) - Total: " + totalResults);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Erro na busca: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SearchResponseDTO(request.getQuery(), List.of(), request.getPage(), request.getPageSize(), 0));
        }
    }
}
