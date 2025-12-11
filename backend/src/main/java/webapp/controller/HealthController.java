package webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webapp.service.GatewayServiceClient;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller para health checks e informações do sistema.
 * 
 * Endpoints:
 * - GET /api/health  -> Verifica se API e Gateway estão funcionando
 * - GET /api/info    -> Informações sobre o sistema
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://localhost:3000", "https://localhost:3001", "https://localhost:3002"})
public class HealthController {
    
    private final GatewayServiceClient gatewayClient;
    
    @Autowired
    public HealthController(GatewayServiceClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }
    
    /**
     * Health check endpoint.
     * 
     * GET http://localhost:8080/api/health
     * 
     * Resposta JSON:
     * {
     *   "status": "UP",
     *   "gateway": "CONNECTED",
     *   "activeBarrels": 2
     * }
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Testa conexão com Gateway
            int activeBarrels = gatewayClient.getActiveBarrels().size();
            
            health.put("status", "UP");
            health.put("gateway", "CONNECTED");
            health.put("activeBarrels", activeBarrels);
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("gateway", "DISCONNECTED");
            health.put("error", e.getMessage());
            
            return ResponseEntity.status(503).body(health);
        }
    }
    
    /**
     * Informações do sistema.
     * 
     * GET http://localhost:8080/api/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("application", "Googol Search Engine");
        info.put("version", "1.0.0");
        info.put("authors", new String[]{
            "Rodrigo Manão - 2023207589",
            "Henrique Diz - 2023213681",
            "João Francisco - 2023228417"
        });
        info.put("api", Map.of(
            "search", "/api/search",
            "barrels", "/api/barrels",
            "health", "/api/health"
        ));
        
        return ResponseEntity.ok(info);
    }
}
