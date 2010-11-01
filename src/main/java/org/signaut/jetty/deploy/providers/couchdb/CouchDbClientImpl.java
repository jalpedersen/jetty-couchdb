package org.signaut.jetty.deploy.providers.couchdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.Base64Variants;
import org.codehaus.jackson.map.ObjectMapper;
import org.signaut.common.http.SimpleHttpClient;
import org.signaut.common.http.SimpleHttpClient.HttpResponseHandler;
import org.signaut.common.http.SimpleHttpClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouchDbClientImpl implements CouchDbClient {

    private final SimpleHttpClient httpClient = new SimpleHttpClientImpl();
    private final String databaseUrl;
    private final Map<String, String> headers = new HashMap<String, String>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public CouchDbClientImpl(String databaseUrl, String username, String password) {
        super();
        if (databaseUrl.endsWith("/")) {
            this.databaseUrl = databaseUrl;
        } else {
            this.databaseUrl = databaseUrl + "/";
        }
        final String authString = username + ":" + password;
        final String base64EncodedAuth = Base64Variants.getDefaultVariant().encode(authString.getBytes());
        headers.put("Authorization", "Basic " + base64EncodedAuth);
    }

    @Override
    public <T> T get(String uri, HttpResponseHandler<T> handler) {
        try {
            final URL url = new URL(databaseUrl + uri);
            return httpClient.get(url, handler, headers);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Bad URL:" + databaseUrl + uri, e);
        }
    }

    @Override
    public <T> T getDocument(String documentId, Class<T> type) {
        return get(documentId, new DocumentHandler<T>(type));
    }

    @Override
    public String downloadAttachment(String documentId, String name, File directory) {
        if ( ! directory.exists()) {
            directory.mkdirs();
        }
        return get(documentId+"/"+name, new FileHandler(new File(directory, name)));
    }

    private final class FileHandler implements HttpResponseHandler<String> {
        private final File file;
        
        public FileHandler(File file) {
            super();
            this.file = file;
        }

        @Override
        public String handleInput(int responseCode, HttpURLConnection connection) {
            if (responseCode >= 400) {
                return null;
            }
            OutputStream out = null;
            try {
                out = new FileOutputStream(file);
                InputStream in = connection.getInputStream();
                byte buffer[] = new byte[4092];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
                return file.getAbsolutePath();
            } catch (FileNotFoundException e) {
                log.warn("File not found: " + file);
            } catch (IOException e) {
                log.warn("While downloading " + file, e);
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    log.warn("While downloading " + file,e);
                }
            }
            return null;
        }
    }
    
    private final class DocumentHandler<T> implements HttpResponseHandler<T> {
        private final Class<T> type;

        public DocumentHandler(Class<T> type) {
            this.type = type;
        }

        @Override
        public T handleInput(int responseCode, HttpURLConnection connection) {
            try {
                if (responseCode >= 400) {
                    throw new DocumentException("ResponseCode: " + responseCode);
                }
                return objectMapper.readValue(connection.getInputStream(), type);
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("While parsing a %s", type), e);
            }
        }

    }

}