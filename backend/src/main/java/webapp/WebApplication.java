package webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicação Spring Boot Web.
 * Fornece uma API REST para o frontend React consumir.
 *
 * A anotação {@code @SpringBootApplication} combina:
 * <ul>
 * <li>{@code @Configuration}: Define beans de configuração</li>
 * <li>{@code @EnableAutoConfiguration}: Configura automaticamente o Spring</li>
 * <li>{@code @ComponentScan}: Escaneia pacotes procurando componentes</li>
 * </ul>
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
     * Construtor padrão.
     */
    public WebApplication() {
        // Construtor vazio - Spring gerencia a instanciação
    }
    
    /**
     * Método main que inicia a aplicação Spring Boot.
     *
     * Inicia:
     * <ul>
     * <li>Servidor Tomcat embedded na porta 8080 (configurável)</li>
     * <li>API REST para o frontend React</li>
     * <li>Conexão com o Gateway RMI</li>
     * </ul>
     * 
     * @param args Argumentos da linha de comando
     */
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   Spring Boot API REST iniciado com sucesso    ║");
        System.out.println("║   Acesse: https://localhost:8443/api/health    ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    }
}
