package common;

import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

/**
 * Classe utilitária para deteção de linguagem de textos.
 * Utiliza Apache Tika com Optimaize Language Detector.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class TextLanguageDetector {
    
    private static LanguageDetector detector;
    
    static {
        try {
            detector = new OptimaizeLangDetector().loadModels();
        } catch (Exception e) {
            System.err.println("Erro ao inicializar Language Detector: " + e.getMessage());
            detector = null;
        }
    }
    
    /**
     * Deteta a linguagem de um texto.
     * 
     * @param content   Texto a analisar
     * @return          Código ISO 639-1 da linguagem (ex: "pt", "en", "es") ou "unknown"
     */
    public static String detectLanguage(String content) {
        try {
            if (content == null || content.trim().isEmpty()) {
                return "unknown";
            }
            
            if (detector == null) {
                return "unknown";
            }

            LanguageResult result = detector.detect(content);
            String language = result.getLanguage();

            // Retorna "unknown" se a confiança for muito baixa
            if (result.isReasonablyCertain()) {
                return language;
            } else {
                return "unknown";
            }

        } catch (Exception e) {
            System.err.println("Erro ao detetar linguagem: " + e.getMessage());
            return "unknown";
        }
    }
    
    /**
     * Deteta a linguagem de um texto com informação detalhada.
     * 
     * @param content   Texto a analisar
     * @return          Resultado completo da deteção (com confiança)
     */
    public static LanguageResult detectLanguageDetailed(String content) {
        try {
            if (content == null || content.trim().isEmpty() || detector == null) {
                return null;
            }
            
            return detector.detect(content);
        } catch (Exception e) {
            System.err.println("Erro ao detetar linguagem: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Verifica se o detector está disponível.
     * 
     * @return true se o detector foi inicializado com sucesso
     */
    public static boolean isAvailable() {
        return detector != null;
    }
}
