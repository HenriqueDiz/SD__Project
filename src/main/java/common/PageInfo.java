package common;

import java.io.Serializable;
import java.util.Set;

public class PageInfo implements Serializable {
    private String url;
    private String title;
    private String snippet;
    private Set<String> links;

    public PageInfo(String url, String title, String snippet, Set<String> links) {
        this.url = url;
        this.title = title;
        this.snippet = snippet;
        this.links = links;
    }

    public String getUrl() { 
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

    public Set<String> getLinks() {
        return links;
    }
}
