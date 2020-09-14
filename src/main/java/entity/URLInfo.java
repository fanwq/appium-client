package entity;

import java.util.Objects;

public class URLInfo {
    private String ua;
    private String url;

    public URLInfo(String ua, String url) {
        this.ua = ua;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URLInfo urlInfo = (URLInfo) o;
        return Objects.equals(ua, urlInfo.ua) &&
                Objects.equals(url, urlInfo.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ua, url);
    }
}
