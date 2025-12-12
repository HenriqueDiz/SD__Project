package webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webapp.service.GatewayServiceClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller para operações relacionadas aos Barrels.
 * 
 * Endpoints:
 * - GET /api/barrels/active       -> Lista barrels ativos
 * - GET /api/barrels/registered   -> Lista barrels registrados
 * - POST /api/barrels/add-url     -> Adiciona URL para indexação
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
@RestController
@RequestMapping("/api/barrels")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://localhost:3000", "https://localhost:3001", "https://localhost:3002"})
public class BarrelController {
    
    private final GatewayServiceClient gatewayClient;
    
    /**
     * Construtor com injeção de dependência.
     * 
     * @param gatewayClient Cliente do serviço Gateway para comunicação RMI
     */
    @Autowired
    public BarrelController(GatewayServiceClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }
    
    /**
     * Obtém lista de barrels ativos.
     *
     * GET http://localhost:8080/api/barrels/active
     *
     * Resposta JSON:
     * <pre>
     * {
     *   "barrels": [
     *     "Barrel1:8001:localhost:1500",
     *     "Barrel2:8002:localhost:2300"
     *   ],
     *   "count": 2
     * }
     * </pre>
     * 
     * @return ResponseEntity com lista de barrels ativos e contador
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveBarrels() {
        try {
            List<String> barrels = gatewayClient.getActiveBarrels();
            
            Map<String, Object> response = new HashMap<>();
            response.put("barrels", barrels);
            response.put("count", barrels.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erro ao obter barrels ativos");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Obtém lista de barrels registrados (histórico).
     *
     * GET http://localhost:8080/api/barrels/registered
     * 
     * @return ResponseEntity com lista de barrels registrados e contador
     */
    @GetMapping("/registered")
    public ResponseEntity<Map<String, Object>> getRegisteredBarrels() {
        try {
            List<String> barrels = gatewayClient.getRegisteredBarrels();
            
            Map<String, Object> response = new HashMap<>();
            response.put("barrels", barrels);
            response.put("count", barrels.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erro ao obter barrels registrados");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Adiciona uma URL para indexação.
     *
     * POST http://localhost:8080/api/barrels/add-url
     *
     * Body JSON:
     * <pre>
     * {
     *   "url": "https://example.com",
     *   "indexAnyway": false
     * }
     * </pre>
     *
     * Resposta:
     * <pre>
     * {
     *   "success": true,
     *   "alreadyIndexed": false,
     *   "message": "URL adicionada à fila"
     * }
     * </pre>
     * 
     * @param request Mapa com "url" e opcionalmente "indexAnyway"
     * @return ResponseEntity com resultado da operação
     */
    @PostMapping("/add-url")
    public ResponseEntity<Map<String, Object>> addURL(@RequestBody Map<String, Object> request) {
        try {
            String url = (String) request.get("url");
            boolean indexAnyway = request.containsKey("indexAnyway") 
                ? (boolean) request.get("indexAnyway") 
                : false;
            
            if (url == null || url.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "URL não pode ser vazio");
                return ResponseEntity.badRequest().body(error);
            }
            
            boolean alreadyIndexed = gatewayClient.addURL(url, indexAnyway);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("alreadyIndexed", alreadyIndexed);
            response.put("message", alreadyIndexed 
                ? "URL já estava indexado" 
                : "URL adicionado à fila");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Erro ao adicionar URL");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
