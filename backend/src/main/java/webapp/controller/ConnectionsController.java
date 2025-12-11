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
 * @RestController combina @Controller + @ResponseBody
 * Todos os métodos retornam JSON automaticamente
 * 
 * @RequestMapping("/api/connections") define o prefixo /api/connections para todas as rotas
 * 
 * Endpoints:
 * - GET  /api/connections?url=https://example.com  -> Obter ligações para uma URL
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
    
    @Autowired
    public ConnectionsController(GatewayServiceClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }
    
    /**
     * Endpoint GET para obter ligações/backlinks de uma URL.
     * 
     * Exemplo de uso:
     * GET http://localhost:8080/api/connections?url=https://example.com
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
     *   "totalConnections": 5,
     *   "hasConnections": true
     * }
     * 
     * @param url   URL para a qual queremos encontrar as ligações
     * @return      Resposta com as URLs que linkam para a URL fornecida
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getConnections(
            @RequestParam("url") String url) {
        
        try {
            System.out.println("Requisição de ligações recebida para URL: " + url);
            
            // Buscar ligações via RMI
            List<SearchResultDTO> connections = gatewayClient.getConnections(url);
            
            // Montar resposta
            Map<String, Object> response = new HashMap<>();
            response.put("url", url);
            response.put("connections", connections);
            response.put("totalConnections", connections.size());
            response.put("hasConnections", !connections.isEmpty());
            
            System.out.println("Enviando " + connections.size() + " ligação(ões)");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Erro ao buscar ligações: " + e.getMessage());
            e.printStackTrace();
            
            // Retornar resposta de erro
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("url", url);
            errorResponse.put("connections", List.of());
            errorResponse.put("totalConnections", 0);
            errorResponse.put("hasConnections", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }
}
