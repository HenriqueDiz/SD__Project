package common;

import java.io.Serializable;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Classe para obter e armazenar informações de uma página web, como título e descrição.
 * 
 * @author Rodrigo Manão - 2023207589
 * @author Henrique Diz - 2023213681
 * @author João Francisco - 2023228417
 * 
 * @version 1.0
 */
public class PageInfo implements Serializable {

    private final String url;
    private String title;
    private String description;

    /**
     * Construtor que inicializa a URL e tenta buscar o título e a descrição da página.
     * 
     * @param url       A URL da página web.
     */
    public PageInfo(String url) {
        this.url = url;
        this.title = "[Sem título]";
        this.description = "[Sem descrição]";
        try {
            fetchAndParse();
        } catch (Exception ignored) {}
    }

    /**
     * Obtém o título da página.
     * 
     * @return O título da página.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Obtém a descrição da página.
     * 
     * @return A descrição da página.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retorna uma representação em string da informação da página, formatada com título, URL e descrição.
     * 
     * @return A representação em string da informação da página.
     */
    @Override
    public String toString() {
        String ls = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.bold(title)).append(ls);
        sb.append(Utils.blue(Utils.bold(Utils.underline(url)))).append(ls).append(ls);
        sb.append(description);
        return sb.toString();
    }

    /**
     * Busca e analisa a página web para extrair o título e a descrição.
     */
    private void fetchAndParse() throws Exception {
        Connection conn = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") 
                .timeout(6000)
                .followRedirects(true)
                .ignoreContentType(false);

        Connection.Response resp = conn.execute();
        String contentType = resp.contentType();
        if (contentType == null || !contentType.toLowerCase().contains("text/html")) {
            return;
        }

        Document doc = resp.parse();

        String t = doc.title();
        if (t != null && !t.isBlank()) {
            this.title = trimEllipsis(t.trim(), 120);
        }

        String desc = null;
        Element metaDesc = doc.selectFirst("meta[name=description]");
        if (metaDesc != null) desc = metaDesc.attr("content");

        if (desc == null || desc.isBlank()) {
            Element ogDesc = doc.selectFirst("meta[property=og:description]");
            if (ogDesc != null) desc = ogDesc.attr("content");
        }

        if (desc == null || desc.isBlank()) {
            Element body = doc.body();
        if (body != null) {
            Element firstHeading = body.selectFirst("h1, h2, h3");
            if (firstHeading != null) {
                firstHeading.remove();
            }
                desc = buildSnippet(body.text(), 200);
            } else {
                desc = buildSnippet(doc.text(), 200);
            }
        } else {
            desc = trimEllipsis(desc.trim(), 220);
        }
        this.description = desc;
    }

    /**
     * Constrói um trecho de texto limpo e formatado a partir do texto fornecido.
     * 
     * @param text      O texto original.
     * @param maxLen    O comprimento máximo do trecho.
     * @return          O trecho formatado.
     */
    private String buildSnippet(String text, int maxLen) {
        if (text == null) return "[Sem descrição]";
        String cleaned = text.replaceAll("\\s+", " ").trim();
        if (cleaned.isBlank()) return "[Sem descrição]";
        if (title != null && !title.isBlank() && cleaned.startsWith(title)) {
            cleaned = cleaned.substring(title.length()).trim();
        }
        return trimEllipsis(cleaned, maxLen);
    }

    /**
     * Trunca uma string e adiciona reticências se exceder o comprimento máximo.
     * 
     * @param s         A string original.
     * @param maxLen    O comprimento máximo permitido.
     * 
     * @return          A string truncada com reticências, se necessário.
     */
    private String trimEllipsis(String s, int maxLen) {
        if (s.length() <= maxLen) return s;
        return s.substring(0, Math.max(0, maxLen - 1)).trim() + "…";
    }
}