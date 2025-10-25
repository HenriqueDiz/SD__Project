package common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {

    private final String host;
    private final int port;
    private final String name;
    private final Properties properties;
    private static final String CONFIG_FILE_PATH = "/Config.cfg";

    public ConfigReader(String type) {
        
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo inválido: 'type' não pode ser nulo ou vazio");
        }

        String hostType = type + ".host";
        String portType = type + ".port";
        String nameType = type + ".name";

        properties = loadConfiguration();

        // Host    
        this.host = requireProperty(hostType);

        // Porta
        String portProp = requireProperty(portType);
        this.port = validatePort(portProp);

        // Nome
        this.name = requireProperty(nameType);
    }
    
    public static int validatePort(String portStr) {
        try {
            int portTemp = Integer.parseInt(portStr);
            if (portTemp <= 0 || portTemp > 65535) {
                throw new IllegalArgumentException("Porta fora do intervalo válido (1-65535): " + portStr);
            }
            return portTemp;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Porta inválida (não numérica): " + portStr, ex);
        }
    }

    public static String validateName(String nameStr) {
        if (nameStr == null || nameStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome inválido: não pode ser nulo ou vazio");
        }
        return nameStr.trim();
    }

    public static Properties loadConfiguration() {
        Properties propertiesTemp = new Properties();
        try {
            InputStream configStream = ConfigReader.class.getResourceAsStream(CONFIG_FILE_PATH);
            if (configStream != null) {
                propertiesTemp.load(configStream);
                configStream.close();
                System.out.println("Configuração carregada: " + CONFIG_FILE_PATH);
            } else {
                throw new IOException("Ficheiro " + CONFIG_FILE_PATH + " não encontrado");
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar configuração: " + e.getMessage());
            System.err.println("Usando valores padrão");
        }
        return propertiesTemp;
    }

    private String requireProperty(String key) {
        String val = properties.getProperty(key);
        return validateName(val);
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