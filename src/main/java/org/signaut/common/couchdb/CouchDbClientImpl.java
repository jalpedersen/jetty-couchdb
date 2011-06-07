/*
Copyright (c) 2010, Jesper André Lyngesen Pedersen
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.signaut.common.couchdb;

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
import org.codehaus.jackson.type.TypeReference;
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
            this.databaseUrl = databaseUrl+"/";
        }
        final String authString = username + ":" + password;
        final String base64EncodedAuth = Base64Variants.getDefaultVariant().encode(authString.getBytes());
        headers.put("Authorization", "Basic " + base64EncodedAuth);;
    }

    @Override
    public <T> T get(String uri, HttpResponseHandler<T> handler) {
        try {
            final URL url = new URL(databaseUrl+uri);
            return httpClient.get(url, handler, headers);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Bad URL: %s%s" ,databaseUrl, uri), e);
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
    
    @Override
    public DocumentStatus putDocument(String id, Object document) {
        try {
            return httpClient.put(new URL(databaseUrl+id), new DocumentStatusHandler(), encode(document), headers);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Bad URL: %s%s" ,databaseUrl, id), e);
        }
    }

    @Override
    public DocumentStatus postDocument(String id, Object document) {
        try {
            return httpClient.post(new URL(databaseUrl+id), new DocumentStatusHandler(), encode(document), headers);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Bad URL: %s%s" ,databaseUrl, id), e);
        }
    }

    @Override
    public DocumentStatus putDocument(String id, String document) {
        try {
            return httpClient.put(new URL(databaseUrl+id), new DocumentStatusHandler(), document, headers);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Bad URL: %s%s" ,databaseUrl, id), e);
        }
    }

    @Override
    public DocumentStatus postDocument(String id, String document) {
        try {
            return httpClient.post(new URL(databaseUrl+id), new DocumentStatusHandler(), document, headers);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Bad URL: %s%s" ,databaseUrl, id), e);
        }
    }

    @Override
    public DocumentStatus createDatabase() {
        return putDocument("", null);
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
                log.warn(String.format("File not found: %s",file));
            } catch (IOException e) {
                log.warn(String.format("While downloading: %s ", file), e);
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    log.warn(String.format("While downloading: %s ", file), e);
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
                    throw new DocumentException(String.format("ResponseCode: %d", responseCode));
                }
                return objectMapper.readValue(connection.getInputStream(), type);
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("While parsing a %s", type), e);
            }
        }
    }
    
    private final class DocumentStatusHandler implements HttpResponseHandler<DocumentStatus> {
        @Override
        public DocumentStatus handleInput(int responseCode, HttpURLConnection connection) {
            try {
                return objectMapper.readValue(connection.getInputStream(), DocumentStatus.class);
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("While parsing document status"), e);
            }
        }
    }


    private String encode(Object document) {
        try {
            return objectMapper.writeValueAsString(document);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("While encoding a %s", document), e);
        }
    }

    private final class GenericMapHandler implements HttpResponseHandler<Map<String, Object>> {
        private final TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String,Object>>() {};
        @Override
        public Map<String, Object> handleInput(int responseCode, HttpURLConnection connection) {
            try {
                return objectMapper.readValue(connection.getInputStream(), mapType);
            } catch (FileNotFoundException e) {
                return null;
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("While parsing response."), e);
            }
        }
    }

    private final class GenericDocumentHandler implements HttpResponseHandler<Document>{

        @Override
        public Document handleInput(int responseCode, HttpURLConnection connection) {
            try {
                return objectMapper.readValue(connection.getInputStream(), Document.class);
            } catch (FileNotFoundException e) {
                return null;
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("While parsing response."), e);
            }
        }
    }
    
    @Override
    public HttpResponseHandler<Map<String, Object>> getGenericMapHandler() {
        return new GenericMapHandler();
    }

    @Override
    public HttpResponseHandler<Document> getDocumentHandler() {
        return new GenericDocumentHandler();
    }
}
