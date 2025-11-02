package common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Classe utilitária com métodos auxiliares para validação, carregamento de configuração e logging.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public final class Utils {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String BOLD = "\u001B[1m";
    private static final String UNDERLINE = "\u001B[4m";

    private static final String CONFIG_FILE_PATH = "/Config.properties";
    private static final String LOG_EXCEPTIONS_FILE_PATH = "/Log_Exceptions.txt";
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Construtor privado para evitar instanciação da classe utilitária.
     */
    private Utils() {}

    /**
     * Valida uma string de porta, garantindo que é um número inteiro válido entre 1 e 65535.
     * 
     * @param portStr      String representando a porta
     * @return             Porta válida como inteiro
     * 
     */
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

    /**
     * Valida uma string de nome, garantindo que não é nula ou vazia.
     * 
     * @param nameStr      String representando o nome
     * @return             Nome válido como string
     * 
     */
    public static String validateName(String nameStr) {
        if (nameStr == null || nameStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome inválido: não pode ser nulo ou vazio");
        }
        return nameStr.trim();
    }

    /**
     * Carrega a configuração a partir do ficheiro Config.properties.
     * 
     * @return Propriedades carregadas do ficheiro de configuração
     */
    public static Properties loadConfiguration() {
        Properties propertiesTemp = new Properties();
        try {
            InputStream configStream = ConfigReader.class.getResourceAsStream(CONFIG_FILE_PATH);
            if (configStream != null) {
                propertiesTemp.load(configStream);
                configStream.close();
                //System.out.println("Configuração carregada: " + CONFIG_FILE_PATH);
            } else {
                throw new IOException("Ficheiro " + CONFIG_FILE_PATH + " não encontrado");
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar configuração: " + e.getMessage());
            System.err.println("Usando valores padrão");
        }
        return propertiesTemp;
    }

    /**
     * Regista uma exceção no log de erros, incluindo uma mensagem opcional.
     * 
     * @param msg  Mensagem adicional para o log (pode ser null)
     * @param t    Exceção a ser registada
     * 
     */
    public static void printLogException(String msg, Throwable t) {
        if (t == null && (msg == null || msg.isBlank())) return;

        String ts = LocalDateTime.now().format(TS_FMT);
        String header = "[" + ts + "] " + msg + ": " + t.getMessage();

        System.err.println(red(header));
        t.printStackTrace(System.err);

        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_EXCEPTIONS_FILE_PATH, true))) {
            pw.println(header);
            t.printStackTrace(pw);
            pw.println();
        } catch (IOException ioe) {
            System.err.println(red("Falha ao escrever no log: " + ioe.getMessage()));
        }
    }

    /**
     * Regista uma exceção no log de erros sem mensagem adicional.
     */
    public static void printLogException(Throwable t) {
        printLogException(null, t);
    }

    /**
     * Aplica uma cor ANSI ao texto fornecido.
     * 
     * @param text      Texto a ser colorido
     * @param code      Código ANSI da cor
     * @return          Texto colorido
    */
    private static String color(String text, String code) {
        return code + text + RESET;
    }

    /**
     * Aplica a cor vermelha ao texto fornecido.
     * 
     * @param text      Texto a ser colorido
     * @return          Texto colorido
    */
    public static String red(String text) {
        return color(text, RED);
    }

    
    /**
     * Aplica a cor amarela ao texto fornecido.
     * 
     * @param text      Texto a ser colorido
     * @return          Texto colorido
    */
    public static String yellow(String text) {
        return color(text, YELLOW);
    }


    /**
     * Aplica a cor verde ao texto fornecido.
     * 
     * @param text      Texto a ser colorido
     * @return          Texto colorido
    */
    public static String green(String text) {
        return color(text, GREEN);
    }

    /**
     * Aplica a cor azul ao texto fornecido.
     * 
     * @param text      Texto a ser colorido
     * @return          Texto colorido
    */
    public static String blue(String text) {
        return color(text, BLUE);
    }

    /**
     *  Aplica negrito ao texto fornecido.
     * 
     * @param text      Texto a ser destacado
     * @return          Texto em negrito
     */
    public static String bold(String text) {
        return color(text, BOLD);
    }

    /**
     * Aplica sublinhado ao texto fornecido.
     * 
     * @param text      Texto a ser sublinhado
     * @return          Texto sublinhado
     */
    public static String underline(String text) {
        return color(text, UNDERLINE);
    }

    /**
     * Remove códigos ANSI de uma string para calcular o tamanho real
     * 
     * @param text          String com códigos ANSI
     * @return              String sem códigos ANSI
    */
    public static String stripAnsi(String text) {
        return text.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}