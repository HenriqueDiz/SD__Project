package webapp.controller;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import statistics.Statistics;
import statistics.BarrelStateCallback;
import statistics.StatsCallback;
import webapp.dto.StatisticsDTO;
import webapp.service.GatewayServiceClient;
 
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
 
/**
 * REST Controller para estatísticas do sistema.
 * 
 * Fornece endpoints para consultar estatísticas do sistema e receber
 * atualizações em tempo real via Server-Sent Events (SSE).
 * 
 * Endpoints:
 * - GET /api/statistics        -> Obter estatísticas atuais
 * - GET /api/statistics/stream -> Stream SSE com atualizações em tempo real
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://localhost:3000", "https://localhost:3001", "https://localhost:3002"})
public class StatisticsController {
 
    private final GatewayServiceClient gatewayClient;
 
    /**
     * Construtor com injeção de dependência.
     * 
     * @param gatewayClient Cliente do serviço Gateway para comunicação RMI
     */
    @Autowired
    public StatisticsController(GatewayServiceClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }
 
    /**
     * Endpoint GET para obter estatísticas atuais do sistema.
     * 
     * Retorna um snapshot das estatísticas no momento da requisição.
     * 
     * GET http://localhost:8080/api/statistics
     * 
     * Resposta JSON:
     * {
     *   "topSearches": {"java": 15, "python": 10},
     *   "averageResponseTime": {"Barrel1:8001": 125000}
     * }
     * 
     * @return Estatísticas atuais do sistema ou DTO vazio em caso de erro
     */
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
 
    /**
     * Endpoint SSE (Server-Sent Events) para stream de estatísticas em tempo real.
     * 
     * Regista callbacks no Gateway RMI e envia eventos ao frontend sempre que:
     * - Estatísticas de pesquisa são atualizadas
     * - Lista de barrels ativos muda
     * - Lista de barrels registrados muda
     * 
     * Processo:
     * 1. Cria SseEmitter com timeout infinito
     * 2. Cria e exporta callbacks RMI para estatísticas e estado dos barrels
     * 3. Regista callbacks no Gateway
     * 4. Envia estado inicial
     * 5. Mantém conexão aberta para enviar atualizações
     * 6. Cleanup automático quando conexão fecha
     * 
     * Eventos enviados:
     * - "stats": Atualizações de estatísticas
     * - "barrels-active": Mudanças nos barrels ativos
     * - "barrels-registered": Mudanças nos barrels registrados
     * 
     * GET http://localhost:8080/api/statistics/stream
     * 
     * @return SseEmitter que envia eventos de atualização em tempo real
     */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStatistics() {
        final SseEmitter emitter = new SseEmitter(0L); // sem timeout
 
        try {
            // Callback para estatísticas
            StatsCallback statsCb = new StatsCallback() {
                @Override
                public void onStatsUpdate(Statistics stats) throws RemoteException {
                    try {
                        Map<String, Integer> top = stats.getTop10Searches();
                        Map<String, Long> avg = stats.getAverageResponseTime();
                        StatisticsDTO dto = new StatisticsDTO(top, avg);
                        emitter.send(SseEmitter.event().name("stats").data(dto));
                    } catch (IOException ex) {
                        // Se não conseguir enviar, completa com erro e faz cleanup
                        try { emitter.completeWithError(ex); } catch (Exception ignored) {}
                    }
                }
            };
            UnicastRemoteObject.exportObject(statsCb, 0);
 
            // Callback para estado dos barrels
            BarrelStateCallback stateCb = new BarrelStateCallback() {
                @Override
                public void onActiveBarrelsUpdate(java.util.List<String> activeBarrels) throws RemoteException {
                    try {
                        emitter.send(SseEmitter.event().name("barrels-active").data(activeBarrels));
                    } catch (IOException ex) {
                        try { emitter.completeWithError(ex); } catch (Exception ignored) {}
                    }
                }

                @Override
                public void onRegisteredBarrelsUpdate(java.util.List<String> registeredBarrels) throws RemoteException {
                    try {
                        emitter.send(SseEmitter.event().name("barrels-registered").data(registeredBarrels));
                    } catch (IOException ex) {
                        try { emitter.completeWithError(ex); } catch (Exception ignored) {}
                    }
                }
            };
            UnicastRemoteObject.exportObject(stateCb, 0);
 
            // Registar no Gateway
            gatewayClient.registerStatsCallback(statsCb);
            gatewayClient.registerBarrelStateCallback(stateCb);
 
            // Enviar estado inicial
            try {
                Statistics stats = gatewayClient.getBarrelStatistics();
                emitter.send(SseEmitter.event().name("stats").data(new StatisticsDTO(
                        stats.getTop10Searches(), stats.getAverageResponseTime())));
                emitter.send(SseEmitter.event().name("barrels-active").data(gatewayClient.getActiveBarrels()));
                emitter.send(SseEmitter.event().name("barrels-registered").data(gatewayClient.getRegisteredBarrels()));
            } catch (Exception ignored) {}
 
            // Limpeza quando o emitter fecha ou dá erro
            emitter.onCompletion(() -> cleanupCallbacks(statsCb, stateCb));
            emitter.onTimeout(() -> cleanupCallbacks(statsCb, stateCb));
            emitter.onError((ex) -> cleanupCallbacks(statsCb, stateCb));
        } catch (Exception e) {
            try { emitter.completeWithError(e); } catch (Exception ignored) {}
        }
 
        return emitter;
    }
 
    /**
     * Limpa os callbacks RMI registrados.
     * 
     * Chamado automaticamente quando:
     * - A conexão SSE é fechada pelo cliente
     * - Ocorre timeout
     * - Ocorre erro na comunicação
     * 
     * Operações de cleanup:
     * 1. Desregistrar callbacks do Gateway
     * 2. Desexportar objetos RMI
     * 
     * @param statsCb   Callback de estatísticas a ser limpo
     * @param stateCb   Callback de estado dos barrels a ser limpo
     */
    private void cleanupCallbacks(StatsCallback statsCb, BarrelStateCallback stateCb) {
        try { gatewayClient.unregisterStatsCallback(statsCb); } catch (Exception ignored) {}
        try { gatewayClient.unregisterBarrelStateCallback(stateCb); } catch (Exception ignored) {}
        try { UnicastRemoteObject.unexportObject(statsCb, true); } catch (Exception ignored) {}
        try { UnicastRemoteObject.unexportObject(stateCb, true); } catch (Exception ignored) {}
    }
}

