package org.signaut.common.http;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public interface SimpleHttpClient {
    public interface HttpResponseHandler<T> {
        T handleInput(int responseCode, HttpURLConnection connection);
    }

    <T> T post(URL url, HttpResponseHandler<T> handler, String content, Map<String, String> headers);

    <T> T get(URL url, HttpResponseHandler<T> handler, Map<String, String> headers);
}
