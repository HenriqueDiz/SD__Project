package common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Utils {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String BOLD = "\u001B[1m";

    private static final String CONFIG_FILE_PATH = "/Config.cfg";

    private Utils() {}

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

    private static String color(String text, String code) {
        return code + text + RESET;
    }

    public static String red(String text) {
        return color(text, RED);
    }

    public static String yellow(String text) {
        return color(text, YELLOW);
    }

    public static String green(String text) {
        return color(text, GREEN);
    }

    public static String blue(String text) {
        return color(text, BLUE);
    }

    public static String bold(String text) {
        return color(text, BOLD);
    }
}