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

    private final String host;
    private final int port;
    private final String name;
    private final Properties properties;

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
    
    private String requireProperty(String key) {
        String val = properties.getProperty(key);
        return Utils.validateName(val);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }
}