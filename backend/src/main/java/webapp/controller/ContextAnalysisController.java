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

import io.github.cdimascio.dotenv.Dotenv;

/**
 * REST Controller para análise contextual de pesquisas usando Google Gemini AI.
 * 
 * Este controlador fornece análise inteligente dos resultados de pesquisa,
 * usando a API do Google Gemini para gerar insights contextuais.
 * 
 * Endpoints:
 * - POST /api/context-analysis -> Analisa query e citações
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
@RestController
@RequestMapping("/api/context-analysis")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://localhost:3000", "https://localhost:3001", "https://localhost:3002"})
public class ContextAnalysisController {

    /**
     * Chave da API do Google Gemini.
     */
    private final String apiKey;
    
    /**
     * Modelo do Google Gemini a ser utilizado.
     */
    private final String model;

    /**
     * Construtor que carrega a configuração da API Gemini.
     * Tenta carregar a configuração na seguinte ordem:
     * 1. Ficheiro .env
     * 2. Variáveis de ambiente do sistema
     * 3. Ficheiro Config.properties (fallback)
     */
    public ContextAnalysisController() {
        // Try to load .env file first
        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.configure()
                    .directory("./")  // Look in project root
                    .ignoreIfMissing()
                    .load();
        } catch (Exception e) {
            System.err.println("Could not load .env file: " + e.getMessage());
        }
        
        // Try .env file first, then system environment variables, then Config.properties
        String envApiKey = null;
        String envModel = null;
        
        if (dotenv != null) {
            envApiKey = dotenv.get("GEMINI_API_KEY");
            envModel = dotenv.get("GEMINI_MODEL");
        }
        
        if (envApiKey == null || envApiKey.isEmpty()) {
            envApiKey = System.getenv("GEMINI_API_KEY");
            envModel = System.getenv("GEMINI_MODEL");
        }
        
        if (envApiKey != null && !envApiKey.isEmpty()) {
            this.apiKey = envApiKey;
            this.model = envModel != null && !envModel.isEmpty() ? envModel : "gemini-1.5-flash";
            System.out.println("Loaded Gemini API configuration from environment");
        } else {
            // Fallback to Config.properties
            Properties props = new Properties();
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("Config.properties")) {
                if (input != null) {
                    props.load(input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.apiKey = props.getProperty("gemini.apiKey", "");
            this.model = props.getProperty("gemini.model", "gemini-1.5-flash");
            System.out.println("Loaded Gemini API configuration from Config.properties");
        }
        
        // Debug: Show if API key is loaded (without revealing the key)
        if (this.apiKey != null && !this.apiKey.isEmpty()) {
            System.out.println("Gemini API Key loaded successfully (length: " + this.apiKey.length() + ")");
            System.out.println("Using model: " + this.model);
        } else {
            System.err.println("WARNING: Gemini API Key is empty or not loaded!");
        }
    }

    /**
     * Endpoint POST para análise contextual de uma pesquisa.
     * 
     * Recebe a query do utilizador e trechos de citações dos resultados,
     * envia para a API do Google Gemini, e retorna uma análise contextual.
     * 
     * Exemplo de uso:
     * POST http://localhost:8080/api/context-analysis
     * Body JSON:
     * {
     *   "query": "java programming",
     *   "citations": "Java is a programming language..."
     * }
     * 
     * Resposta JSON:
     * {
     *   "analysis": "Java é uma linguagem de programação..."
     * }
     * 
     * @param payload   Mapa contendo "query" e "citations"
     * @return          Resposta com análise contextual ou erro
     */
    @PostMapping
    public ResponseEntity<?> analyze(@RequestBody Map<String, Object> payload) {
        String query = (String) payload.getOrDefault("query", "");
        String citations = (String) payload.getOrDefault("citations", "");
        if (query.isBlank() && citations.isBlank()) {
            return ResponseEntity.badRequest().body("Missing query or citations");
        }
        try {
            String prompt = buildPrompt(query, citations);
            String response = callGeminiAPI(prompt);
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

    /**
     * Constrói o prompt para a API do Google Gemini.
     * 
     * Cria um prompt estruturado com:
     * - Definição do papel do assistente
     * - Query do utilizador
     * - Citações dos resultados (se disponíveis)
     * - Instruções específicas baseadas na qualidade das citações
     * - Restrições de formato de saída
     * 
     * @param query         Query de pesquisa do utilizador
     * @param citations     Trechos de texto dos resultados
     * @return              Prompt formatado para envio à API
     */
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

    /**
     * Realiza a chamada HTTP à API do Google Gemini.
     * 
     * Processo:
     * 1. Monta a URL do endpoint com a chave da API
     * 2. Prepara o corpo da requisição em formato JSON
     * 3. Faz escape de caracteres especiais no prompt
     * 4. Envia requisição POST com o prompt
     * 5. Parse da resposta JSON
     * 6. Extração do texto gerado
     * 
     * @param prompt            Prompt construído para análise
     * @return                  Texto da análise gerada pelo Gemini
     * @throws IOException      Se houver erro na comunicação ou no parse da resposta
     */
    private String callGeminiAPI(String prompt) throws IOException {
        // Google Gemini API endpoint
        String urlString = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", 
                                        model, apiKey);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Escape the prompt to prevent JSON injection and formatting issues
        String escapedPrompt = prompt.replace("\\", "\\\\")
                                     .replace("\"", "\\\"")
                                     .replace("\n", "\\n")
                                     .replace("\r", "\\r")
                                     .replace("\t", "\\t");

        // Gemini API request body format
        String body = String.format("{\n  \"contents\": [{\n    \"parts\": [{\n      \"text\": \"%s\"\n    }]\n  }]\n}", escapedPrompt);
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
            throw new IOException("Gemini API error: " + errorMessage);
        }
        
        // Parse Gemini response structure
        JsonNode candidatesNode = root.path("candidates");
        if (candidatesNode.isMissingNode() || !candidatesNode.isArray() || candidatesNode.size() == 0) {
            throw new IOException("Invalid API response: missing or empty 'candidates' array. Response: " + response.toString());
        }
        
        JsonNode firstCandidate = candidatesNode.get(0);
        if (firstCandidate == null || firstCandidate.isNull()) {
            throw new IOException("Invalid API response: first candidate is null. Response: " + response.toString());
        }
        
        JsonNode contentNode = firstCandidate.path("content");
        if (contentNode.isMissingNode()) {
            throw new IOException("Invalid API response: missing 'content' in candidate. Response: " + response.toString());
        }
        
        JsonNode partsNode = contentNode.path("parts");
        if (partsNode.isMissingNode() || !partsNode.isArray() || partsNode.size() == 0) {
            throw new IOException("Invalid API response: missing or empty 'parts' array. Response: " + response.toString());
        }
        
        JsonNode textNode = partsNode.get(0).path("text");
        if (textNode.isMissingNode() || textNode.isNull()) {
            throw new IOException("Invalid API response: missing or null 'text'. Response: " + response.toString());
        }
        
        return textNode.asText();
    }
}
