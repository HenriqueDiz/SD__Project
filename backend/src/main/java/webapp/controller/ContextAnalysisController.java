package webapp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/context-analysis")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ContextAnalysisController {

    private final String apiKey;
    private final String model;

    public ContextAnalysisController() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("Config.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.apiKey = props.getProperty("openrouter.apiKey", "");
        this.model = props.getProperty("openrouter.model", "");
    }

    @PostMapping
    public ResponseEntity<?> analyze(@RequestBody Map<String, Object> payload) {
        String query = (String) payload.getOrDefault("query", "");
        String citations = (String) payload.getOrDefault("citations", "");
        if (query.isBlank() && citations.isBlank()) {
            return ResponseEntity.badRequest().body("Missing query or citations");
        }
        try {
            String prompt = buildPrompt(query, citations);
            String response = callOpenRouterAPI(prompt);
            Map<String, Object> result = new HashMap<>();
            result.put("analysis", response);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    private String buildPrompt(String query, String citations) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analisa a seguinte pesquisa e as citacoes apresentadas.\n");
        sb.append("Pesquisa: ").append(query).append("\n");
        if (!citations.isBlank()) {
            sb.append("Citacoes: ").append(citations).append("\n");
        }
        sb.append("Responde de forma simples, apenas com letras e numeros, sem acentos, sem emojis. Fornece uma analise curta e clara.");
        return sb.toString();
    }

    private String callOpenRouterAPI(String prompt) throws IOException {
        URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);

        String body = String.format("{\n  \"model\": \"%s\",\n  \"messages\": [\n    {\n      \"role\": \"user\",\n      \"content\": \"%s\"\n    }\n  ],\n  \"reasoning\": {\"enabled\": true}\n}", model, prompt);
        byte[] input = body.getBytes(StandardCharsets.UTF_8);
        conn.getOutputStream().write(input);

        int status = conn.getResponseCode();
        InputStream is = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder response = new StringBuilder();
        int c;
        while ((c = is.read()) != -1) {
            response.append((char) c);
        }
        is.close();
        conn.disconnect();
        // Parse JSON response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.toString());
        // O caminho depende do formato da resposta da API
        JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
        if (!contentNode.isMissingNode()) {
            return contentNode.asText();
        }
        return response.toString();
    }
}
