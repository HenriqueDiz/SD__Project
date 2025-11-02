package common;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.Serial;
import java.io.Serializable;

public class PageInfo implements Serializable {

    private final String url;
    private String title;
    private String description;

    public PageInfo(String url) {
        this.url = url;
        this.title = "[Sem título]";
        this.description = "[Sem descrição]";
        try {
            fetchAndParse();
        } catch (Exception ignored) {}
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        String ls = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.bold(title)).append(ls);
        sb.append(Utils.blue(Utils.bold(Utils.underline(url)))).append(ls).append(ls);
        sb.append(description);
        return sb.toString();
    }

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

    private String buildSnippet(String text, int maxLen) {
        if (text == null) return "[Sem descrição]";
        String cleaned = text.replaceAll("\\s+", " ").trim();
        if (cleaned.isBlank()) return "[Sem descrição]";
        if (title != null && !title.isBlank() && cleaned.startsWith(title)) {
            cleaned = cleaned.substring(title.length()).trim();
        }
        return trimEllipsis(cleaned, maxLen);
    }

    private String trimEllipsis(String s, int maxLen) {
        if (s.length() <= maxLen) return s;
        return s.substring(0, Math.max(0, maxLen - 1)).trim() + "…";
    }
}