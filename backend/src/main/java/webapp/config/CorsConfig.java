package webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuração de CORS (Cross-Origin Resource Sharing).
 * 
 * Permite que o frontend React (localhost:3000) acesse a API (localhost:8080).
 * Sem isso, o browser bloqueia as requisições por segurança.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
@Configuration
public class CorsConfig {
    
    /**
     * Configura CORS para permitir requisições do frontend React.
     * 
     * @return Filtro CORS configurado
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Permite credenciais (cookies, authorization headers)
        config.setAllowCredentials(true);
        
        // Permite requisições do frontend React (HTTP e HTTPS)
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:3001");
        config.addAllowedOrigin("http://localhost:3002");
        config.addAllowedOrigin("https://localhost:3000");
        config.addAllowedOrigin("https://localhost:3001");
        config.addAllowedOrigin("https://localhost:3002");
        
        // Permite todos os headers
        config.addAllowedHeader("*");
        
        // Permite todos os métodos HTTP (GET, POST, PUT, DELETE, etc)
        config.addAllowedMethod("*");
        
        source.registerCorsConfiguration("/api/**", config);
        
        System.out.println("✓ CORS configurado para permitir frontend React");
        
        return new CorsFilter(source);
    }
}
