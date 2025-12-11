package webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicação Spring Boot Web.
 * Fornece uma API REST para o frontend React consumir.
 * 
 * @SpringBootApplication combina:
 *   - @Configuration: Define beans de configuração
 *   - @EnableAutoConfiguration: Configura automaticamente o Spring
 *   - @ComponentScan: Escaneia pacotes procurando componentes
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
@SpringBootApplication
public class WebApplication {
    
    /**
     * Método main que inicia a aplicação Spring Boot.
     * 
     * Inicia:
     * - Servidor Tomcat embedded na porta 8080 (configurável)
     * - API REST para o frontend React
     * - Conexão com o Gateway RMI
     */
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   Spring Boot API REST iniciado com sucesso    ║");
        System.out.println("║   Acesse: https://localhost:8443/api/health    ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    }
}
