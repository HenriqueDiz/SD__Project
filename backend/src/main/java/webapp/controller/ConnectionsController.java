package webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webapp.dto.SearchResultDTO;
import webapp.service.GatewayServiceClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller para operações de ligações/backlinks.
 *
 * Este controlador lida com requisições relacionadas a encontrar
 * URLs que fazem referência (linkam) para uma URL específica.
 *
 * A anotação {@code @RestController} combina {@code @Controller} + {@code @ResponseBody}.
 * Todos os métodos retornam JSON automaticamente.
 *
 * A anotação {@code @RequestMapping("/api/connections")} define o prefixo /api/connections para todas as rotas.
 *
 * Endpoints:
 * <ul>
 * <li>GET  /api/connections?url=https://example.com  -&gt; Obter ligações para uma URL</li>
 * </ul>
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
@RestController
@RequestMapping("/api/connections")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://localhost:3000", "https://localhost:3001", "https://localhost:3002"})
public class ConnectionsController {
    
    private final GatewayServiceClient gatewayClient;
    
    /**
     * Construtor com injeção de dependência.
     * 
     * @param gatewayClient Cliente do serviço Gateway para comunicação RMI
     */
    @Autowired
    public ConnectionsController(GatewayServiceClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }
    
    /**
     * Endpoint GET para obter ligações/backlinks de uma URL com paginação.
     *
     * Exemplo de uso:
     * GET http://localhost:8080/api/connections?url=https://example.com&amp;page=0
     * 
     * Resposta JSON:
     * {
     *   "url": "https://example.com",
     *   "connections": [
     *     {
     *       "url": "https://site1.com",
     *       "title": "Site 1 Title",
     *       "snippet": "Description of site 1",
     *       "references": 0
     *     },
     *     ...
     *   ],
     *   "totalConnections": 50,
     *   "currentPage": 0,
     *   "pageSize": 10,
     *   "totalPages": 5,
     *   "hasConnections": true
     * }
     * 
     * @param url   URL para a qual queremos encontrar as ligações
     * @param page  Número da página (0-indexed, padrão = 0)
     * @return      Resposta com as URLs que linkam para a URL fornecida
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getConnections(
            @RequestParam("url") String url,
            @RequestParam(value = "page", defaultValue = "0") int page) {
        
        try {
            System.out.println("Requisição de ligações recebida para URL: " + url + " (página " + page + ")");
            
            // Buscar todas as ligações via RMI
            List<SearchResultDTO> allConnections = gatewayClient.getConnections(url);
            
            // Aplicar paginação
            final int PAGE_SIZE = 10;
            int totalConnections = allConnections.size();
            int totalPages = totalConnections == 0 ? 0 : (int) Math.ceil((double) totalConnections / PAGE_SIZE);
            
            // Se não há conexões, retorna lista vazia
            List<SearchResultDTO> pagedConnections;
            if (totalConnections == 0) {
                pagedConnections = List.of();
            } else {
                // Validar página
                if (page < 0) {
                    page = 0;
                }
                if (page >= totalPages) {
                    page = totalPages - 1;
                }
                
                // Calcular índices de início e fim
                int startIndex = page * PAGE_SIZE;
                int endIndex = Math.min(startIndex + PAGE_SIZE, totalConnections);
                
                // Obter conexões da página atual
                pagedConnections = allConnections.subList(startIndex, endIndex);
            }
            
            // Montar resposta
            Map<String, Object> response = new HashMap<>();
            response.put("url", url);
            response.put("connections", pagedConnections);
            response.put("totalConnections", totalConnections);
            response.put("currentPage", page);
            response.put("pageSize", PAGE_SIZE);
            response.put("totalPages", totalPages);
            response.put("hasConnections", !allConnections.isEmpty());
            
            System.out.println("Enviando " + pagedConnections.size() + " ligação(ões) da página " + page + "/" + (totalPages - 1));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Erro ao buscar ligações: " + e.getMessage());
            e.printStackTrace();
            
            // Retornar resposta de erro
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("url", url);
            errorResponse.put("connections", List.of());
            errorResponse.put("totalConnections", 0);
            errorResponse.put("currentPage", 0);
            errorResponse.put("pageSize", 10);
            errorResponse.put("totalPages", 0);
            errorResponse.put("hasConnections", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }
}
