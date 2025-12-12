package webapp.config;

import gateway.GatewayInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import common.ConfigReader;

/**
 * Configuração do cliente RMI para conexão com o Gateway.
 *
 * A anotação {@code @Configuration} indica que esta classe contém beans de configuração.
 * Beans são objetos gerenciados pelo Spring que podem ser injetados em outros componentes.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
@Configuration
public class RMIConfig {
    
    /**
     * Construtor padrão.
     */
    public RMIConfig() {
        // Construtor vazio - Spring gerencia a instanciação
    }
    
    /**
     * Host do Gateway RMI
     */
    private String gatewayHost;
    
    /**
     * Porta do Gateway RMI
     */
    private int gatewayPort;
    
    /**
     * Nome do serviço RMI registrado
     */
    private String gatewayName;
    
    /**
     * Cria um bean do tipo GatewayInterface conectando ao servidor RMI.
     *
     * Este bean é criado UMA VEZ na inicialização e reutilizado em todas as requisições.
     * Spring injeta automaticamente onde for necessário via {@code @Autowired}.
     * 
     * @return Referência remota ao Gateway RMI
     * @throws Exception Se não conseguir conectar ao Gateway
     */
    @Bean
    public GatewayInterface gatewayService() throws Exception {
        ConfigReader config = new ConfigReader("gateway");
        gatewayHost = config.getHost();
        gatewayPort = config.getPort();
        gatewayName = config.getName();
        try {
            System.out.println("Conectando ao Gateway RMI...");
            System.out.println("   Host: " + gatewayHost);
            System.out.println("   Porta: " + gatewayPort);
            System.out.println("   Nome: " + gatewayName);
            
            // Conecta ao RMI Registry
            Registry registry = LocateRegistry.getRegistry(gatewayHost, gatewayPort);
            
            // Busca o objeto remoto Gateway
            GatewayInterface gateway = (GatewayInterface) registry.lookup(gatewayName);
            
            // Testa a conexão
            gateway.getActiveBarrels();
            
            System.out.println("Conectado ao Gateway RMI com sucesso!");
            return gateway;
            
        } catch (Exception e) {
            System.err.println("ERRO: Não foi possível conectar ao Gateway RMI");
            System.err.println("   Certifique-se que o Gateway está rodando em " + gatewayHost + ":" + gatewayPort);
            System.err.println("   Erro: " + e.getMessage());
            throw new RuntimeException("Falha ao conectar ao Gateway RMI", e);
        }
    }
}
