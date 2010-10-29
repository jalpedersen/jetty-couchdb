package org.signaut.common.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHttpClientImpl implements SimpleHttpClient {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public <T> T post(URL url, HttpResponseHandler<T> handler, String content, Map<String, String> headers) {
        return send("POST", url, handler, content, headers);
    }

    @Override
    public <T> T get(URL url, HttpResponseHandler<T> handler, Map<String, String> headers) {
        return send("GET", url, handler, null, headers);
    }

    
    public <T> T send(String method, URL url, HttpResponseHandler<T> handler, String content, Map<String, String> headers) {
        HttpURLConnection connection = null;
        OutputStream out = null;
        OutputStreamWriter writer = null;
        InputStream in = null;
        T result = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            for (Entry<String, String> entry: headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            if (content != null) {
                connection.setDoOutput(true);
                out = connection.getOutputStream();
                writer = new OutputStreamWriter(out);
                writer.write(content);
                writer.close();
            }
            in = connection.getInputStream();
            result = handler.handleInput(responseCode(connection), connection);
        } catch (IOException e) {
            result = handler.handleInput(responseCode(connection), connection);
        } finally {
            close(writer, out, in);
        }
        return result;
    }

    
    private int responseCode(HttpURLConnection connection) {
        if (connection != null){
            try {
                return connection.getResponseCode();
            } catch (IOException e) {
                //Fall though
            }
        }
        return -1;
    }
    
    private void close(Closeable... streams) {
        for (Closeable stream : streams) {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                log.warn("", e);
            }
        }
    }
}
