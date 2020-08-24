package utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class DownloadUtil {
    private static DownloadUtil downloader = null;
    private DownloadUtil(){}

    public static DownloadUtil getDownloader(){
        if(downloader == null)
            downloader = new DownloadUtil();
        return downloader;
    }

    public HttpEntity getUrl(String url) throws IOException {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        httpResponse = httpClient.execute(get);
        return httpResponse.getEntity();
    }
}
