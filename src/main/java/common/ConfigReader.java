package common;

import java.util.Properties;

/**
 * Classe responsável por ler e validar as configurações do sistema a partir de um ficheiro de propriedades.
 * 
 * @author João Francisco - 2023228417
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * 
 * @version 1.0
 */

public final class ConfigReader {

    /** 
     * Host do componente. 
     */
    private final String host;

    /** 
     * Porta do componente. 
     */
    private final int port;

    /** 
     * Nome do componente. 
     */
    private final String name;

    /**
     * Propriedades do componente.
     */
    private final Properties properties;

    /**
     * Construtor que lê e valida as configurações para o tipo especificado (e.g., "gateway", "queue", "downloader", "barrel").
     *  
     * @param type O tipo de componente cujas configurações serão lidas.
     */

    public ConfigReader(String type) {
        
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo inválido: 'type' não pode ser nulo ou vazio");
        }

        String hostType = type + ".host";
        String portType = type + ".port";
        String nameType = type + ".name";

        properties = Utils.loadConfiguration();

        // Host    
        this.host = requireProperty(hostType);

        // Porta
        String portProp = requireProperty(portType);
        this.port = Utils.validatePort(portProp);

        // Nome
        this.name = requireProperty(nameType);
    }

    /**
     * Obtém uma propriedade obrigatória do arquivo de configuração.
     * 
     * @param key A chave da propriedade a ser obtida.
     * @return O valor da propriedade.
     */
    private String requireProperty(String key) {
        String val = properties.getProperty(key);
        return Utils.validateName(val);
    }


    /**
     * Obtém o host configurado.
     * @return O host como string.
     */
    public String getHost() {
        return host;
    }

    /**
     * Obtém a porta configurada.
     * @return A porta como inteiro.
     */
    public int getPort() {
        return port;
    }

    /**
     * Obtém o nome configurado.
     * @return O nome como string.
     */
    public String getName() {
        return name;
    }
}