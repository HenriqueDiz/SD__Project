package webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import statistics.Statistics;
import webapp.dto.StatisticsDTO;
import webapp.service.GatewayServiceClient;

import java.util.Map;

/**
 * REST Controller para estatísticas do sistema.
 */
@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class StatisticsController {

    private final GatewayServiceClient gatewayClient;

    @Autowired
    public StatisticsController(GatewayServiceClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }

    @GetMapping
    public ResponseEntity<StatisticsDTO> getStatistics() {
        try {
            Statistics stats = gatewayClient.getBarrelStatistics();
            Map<String, Integer> top = stats.getTop10Searches();
            Map<String, Long> avg = stats.getAverageResponseTime();
            return ResponseEntity.ok(new StatisticsDTO(top, avg));
        } catch (Exception e) {
            return ResponseEntity.ok(new StatisticsDTO());
        }
    }
}
