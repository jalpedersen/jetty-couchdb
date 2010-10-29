package org.signaut.jetty.deploy.providers.couchdb;

import java.io.File;

import org.signaut.common.http.SimpleHttpClient.HttpResponseHandler;

public interface CouchDbClient {
    public static class DocumentException extends RuntimeException {
        private static final long serialVersionUID = -7152085677408141413L;

        public DocumentException(String message, Throwable cause) {
            super(message, cause);
        }

        public DocumentException(String message) {
            super(message);
        }
    }

    <T> T get(String uri, HttpResponseHandler<T> handler);

    <T> T getDocument(String documentId, Class<T> type);

    String downloadAttachment(String documentId, String name, File directory);

}