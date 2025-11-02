package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

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

    public static void saveBarrelProgress(String barrelName, Map<String, HashSet<String>> progress) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(barrelName + "_progress.dat"))) {
            oos.writeObject(progress);
            System.out.println(Utils.green("Progresso salvo para o barrel: " + barrelName));
        } catch (IOException e) {
            System.err.println("Erro ao salvar progresso do barrel: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, HashSet<String>> loadBarrelProgress(String barrelName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(barrelName + "_progress.dat"))) {
            return (Map<String, HashSet<String>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar progresso do barrel: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static boolean progressFileExists(String barrelName) {
        File progressFile = new File(barrelName + "_progress.dat");
        return progressFile.exists();
    }

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

    public static void printLogException(Throwable t) {
        printLogException(null, t);
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

    public static String underline(String text) {
        return color(text, UNDERLINE);
    }
}