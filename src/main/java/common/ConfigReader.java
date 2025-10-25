package common;

import java.util.Properties;

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