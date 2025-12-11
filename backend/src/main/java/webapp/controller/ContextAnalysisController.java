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
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://localhost:3000", "https://localhost:3001"})
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
            return ResponseEntity.ok()
                .header("Content-Type", "application/json; charset=utf-8")
                .body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    private String buildPrompt(String query, String citations) {
        StringBuilder sb = new StringBuilder();
        
        // System prompt defining the AI's role and constraints
        sb.append("Voce e um assistente de pesquisa que ajuda usuarios a compreender melhor os resultados de busca. ");
        sb.append("Sua tarefa e fornecer uma analise contextual breve e util sobre a pesquisa do usuario.\n\n");
        
        // User's search query
        sb.append("Pesquisa do usuario: \"").append(query).append("\"\n\n");
        
        // Conditional instructions based on citation quality
        if (!citations.isBlank() && citations.length() > 50) {
            // Good citations available - use them as primary source
            sb.append("Trechos relevantes encontrados nos resultados:\n");
            sb.append(citations).append("\n\n");
            sb.append("Instrucoes:\n");
            sb.append("- Analise os trechos fornecidos e resuma o que foi encontrado sobre \"").append(query).append("\"\n");
            sb.append("- Destaque informacoes-chave e conceitos principais mencionados nos trechos\n");
            sb.append("- Se os trechos contem informacoes contraditorias ou diferentes perspectivas, mencione isso\n");
            sb.append("- Mantenha a resposta concisa (maximo 3-4 frases)\n");
        } else if (!citations.isBlank()) {
            // Poor quality citations - acknowledge but don't rely heavily
            sb.append("Alguns resultados foram encontrados, mas com informacao limitada.\n\n");
            sb.append("Instrucoes:\n");
            sb.append("- Forneca um contexto geral sobre \"").append(query).append("\"\n");
            sb.append("- Explique brevemente o que o usuario provavelmente esta buscando\n");
            sb.append("- Sugira que tipo de informacao seria util para esta pesquisa\n");
            sb.append("- Mantenha a resposta concisa (maximo 3-4 frases)\n");
        } else {
            // No citations - provide contextual understanding
            sb.append("Nenhum trecho especifico foi fornecido.\n\n");
            sb.append("Instrucoes:\n");
            sb.append("- Forneca uma breve explicacao contextual sobre \"").append(query).append("\"\n");
            sb.append("- Ajude o usuario a entender melhor o topico ou termo pesquisado\n");
            sb.append("- Mencione que tipo de informacao normalmente se encontra ao pesquisar sobre isso\n");
            sb.append("- Mantenha a resposta concisa (maximo 3-4 frases)\n");
        }
        
        // Output format constraints
        sb.append("\nFormato de resposta:\n");
        sb.append("- Escreva em portugues de Portugal\n");
        sb.append("- NAO use emojis ou caracteres especiais\n");
        sb.append("- Use apenas letras, numeros, espacos e pontuacao basica (. , ! ?)\n");
        sb.append("- Seja claro, objetivo e informativo\n");
        
        return sb.toString();
    }

    private String callOpenRouterAPI(String prompt) throws IOException {
        URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);

        // Escape the prompt to prevent JSON injection and formatting issues
        String escapedPrompt = prompt.replace("\\", "\\\\")
                                     .replace("\"", "\\\"")
                                     .replace("\n", "\\n")
                                     .replace("\r", "\\r")
                                     .replace("\t", "\\t");

        String body = String.format("{\n  \"model\": \"%s\",\n  \"messages\": [\n    {\n      \"role\": \"user\",\n      \"content\": \"%s\"\n    }\n  ]\n}", model, escapedPrompt);
        byte[] input = body.getBytes(StandardCharsets.UTF_8);
        conn.getOutputStream().write(input);

        int status = conn.getResponseCode();
        InputStream is = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder response = new StringBuilder();
        byte[] buf = is.readAllBytes();
        String respStr = new String(buf, StandardCharsets.UTF_8);
        response.append(respStr);
        is.close();
        conn.disconnect();
        
        // Parse JSON response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.toString());
        
        // Check if the response contains an error
        if (root.has("error")) {
            JsonNode errorNode = root.get("error");
            String errorMessage = errorNode.has("message") 
                ? errorNode.get("message").asText() 
                : "Unknown API error";
            throw new IOException("OpenRouter API error: " + errorMessage);
        }
        
        // Safely navigate the JSON structure
        JsonNode choicesNode = root.path("choices");
        if (choicesNode.isMissingNode() || !choicesNode.isArray() || choicesNode.size() == 0) {
            throw new IOException("Invalid API response: missing or empty 'choices' array. Response: " + response.toString());
        }
        
        JsonNode firstChoice = choicesNode.get(0);
        if (firstChoice == null || firstChoice.isNull()) {
            throw new IOException("Invalid API response: first choice is null. Response: " + response.toString());
        }
        
        JsonNode messageNode = firstChoice.path("message");
        if (messageNode.isMissingNode()) {
            throw new IOException("Invalid API response: missing 'message' in choice. Response: " + response.toString());
        }
        
        JsonNode contentNode = messageNode.path("content");
        if (contentNode.isMissingNode() || contentNode.isNull()) {
            throw new IOException("Invalid API response: missing or null 'content'. Response: " + response.toString());
        }
        
        return contentNode.asText();
    }
}
