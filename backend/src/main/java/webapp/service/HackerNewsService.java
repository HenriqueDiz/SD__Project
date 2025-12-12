package webapp.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serviço para buscar e indexar as top 50 histórias do HackerNews.
 * 
 * Este serviço executa automaticamente quando a aplicação inicia
 * e busca as top stories da API do HackerNews, indexando os links
 * do news.ycombinator.com.
 * 
 * API do HackerNews:
 * - Top Stories: https://hacker-news.firebaseio.com/v0/topstories.json
 * - Item Details: https://hacker-news.firebaseio.com/v0/item/{id}.json
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
@Service
public class HackerNewsService {
    
    private static final String HN_TOP_STORIES_URL = "https://hacker-news.firebaseio.com/v0/topstories.json";
    private static final String HN_BASE_URL = "https://news.ycombinator.com";
    private static final String HN_ITEM_PAGE_URL = "https://news.ycombinator.com/item?id=%d";
    private static final int TOP_STORIES_LIMIT = 50;
    
    private final GatewayServiceClient gatewayClient;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Construtor com injeção de dependência.
     * 
     * @param gatewayClient Cliente do serviço Gateway para comunicação RMI
     */
    @Autowired
    public HackerNewsService(GatewayServiceClient gatewayClient) {
        this.gatewayClient = gatewayClient;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Executa automaticamente quando a aplicação está pronta.
     * Busca as top 50 histórias do HackerNews e indexa.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void indexTopStories() {
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("╔════════════════════════════════════════════════╗");
                System.out.println("║  Iniciando indexação do HackerNews Top 50...   ║");
                System.out.println("╚════════════════════════════════════════════════╝");
                
                // Dar tempo para o sistema inicializar completamente
                Thread.sleep(5000);
                
                indexTopStoriesSync();
                
            } catch (Exception e) {
                System.err.println("Erro na indexação do HackerNews: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Indexa as top 50 histórias do HackerNews de forma síncrona.
     * Este método pode ser chamado tanto na inicialização quanto via API REST.
     * 
     * @return Número de URLs indexados com sucesso
     * @throws Exception Se houver erro na indexação
     */
    public int indexTopStoriesSync() throws Exception {
        List<Long> topStoryIds = fetchTopStoryIds();
        System.out.println("Obtidos " + topStoryIds.size() + " story IDs do HackerNews");
        
        // Indexar apenas as TOP 50 stories do HackerNews
        int indexed = 0;
        for (Long storyId : topStoryIds) {
            try {
                // Indexar o link da story no HackerNews (news.ycombinator.com)
                String hnStoryUrl = String.format(HN_ITEM_PAGE_URL, storyId);
                gatewayClient.addURL(hnStoryUrl, false);
                indexed++;
                System.out.println("HackerNews story indexado (" + indexed + "/" + topStoryIds.size() + "): " + hnStoryUrl);
                
                // Rate limiting - não sobrecarregar o sistema
                Thread.sleep(100);
            } catch (Exception e) {
                System.err.println("Erro ao indexar story " + storyId + ": " + e.getMessage());
            }
        }
        
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║  Indexação do HackerNews concluída!            ║");
        System.out.println("║  Total indexado: " + indexed + " URLs                       ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        
        return indexed;
    }
    
    /**
     * Busca os IDs das top stories do HackerNews.
     * @return Lista com os IDs das top 50 stories
     */
    private List<Long> fetchTopStoryIds() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HN_TOP_STORIES_URL))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Falha ao buscar top stories: HTTP " + response.statusCode());
        }
        
        JsonNode jsonArray = objectMapper.readTree(response.body());
        List<Long> storyIds = new ArrayList<>();
        
        for (int i = 0; i < Math.min(TOP_STORIES_LIMIT, jsonArray.size()); i++) {
            storyIds.add(jsonArray.get(i).asLong());
        }
        
        return storyIds;
    }
}
