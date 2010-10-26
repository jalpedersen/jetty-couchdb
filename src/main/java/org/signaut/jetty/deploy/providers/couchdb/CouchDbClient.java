package org.signaut.jetty.deploy.providers.couchdb;

import java.io.IOException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.client.security.BasicAuthentication;
import org.eclipse.jetty.client.security.Realm;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

class CouchDbClient {
    private HttpClient httpClient;
    private final String databaseUrl;
    private final BasicAuthentication auth;
    
    public CouchDbClient(String databaseUrl, String username, String password) {
        this.databaseUrl = databaseUrl;
        httpClient = new HttpClient();
        try {
            httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            httpClient.setThreadPool(new QueuedThreadPool(2));
            auth = new BasicAuthentication(new SimpleRealm(username, password));
            
            httpClient.start();
        } catch (Exception e) {
            throw new IllegalStateException("While starting httpClient", e);
        }
    }

    public HttpExchange dispatchChanges(String design, String filter, long since, Callback callback) {
        final CouchDbExchange exchange = new CouchDbChangesExchange(design, filter, since, callback);
        try {
            httpClient.send(exchange);
            return exchange;
        } catch (IOException e) {
            throw new IllegalStateException(String.format("While sending to %s/_design/%s/_view/%s", databaseUrl,
                                                          design, filter), e);
        }
    }

    public HttpExchange dispatchGetDocument(String docId, Callback callback) {
        final CouchDbExchange exchange = new CouchDbGetDocumentExchange(docId, callback);
        try {
            httpClient.send(exchange);
            return exchange;
        } catch (IOException e) {
            throw new IllegalStateException(String.format("While getting doc %s", docId), e);
        }
    }

    private class CouchDbGetDocumentExchange extends CouchDbExchange {
        private CouchDbGetDocumentExchange(String docId, Callback callback) {
            super(callback);
            setURL(databaseUrl +"/"+ docId);
        }
    }
    
    private class CouchDbChangesExchange extends CouchDbExchange {
        private CouchDbChangesExchange(String design, String filter, long since, Callback callback) {
            super(callback);
            setURL(databaseUrl + "/_changes/" + filter + "?feed=continuous&since="+since);
        }
    }
    
    private class CouchDbExchange extends HttpExchange {
        private final Callback callback;
        private CouchDbExchange(Callback callback) {
            if (auth != null) {
                try {
                    auth.setCredentials(this);
                } catch (IOException e) {
                    throw new IllegalStateException("While setting credetials to "+databaseUrl, e);
                }
            }
            setMethod(HttpMethods.GET);
            this.callback = callback;
        }

        @Override
        protected void onResponseContent(Buffer content) throws IOException {
            callback.onMessage(content);
        }
    }
    
    private static class SimpleRealm implements Realm {
        private final String username;
        private final String password;
        
        public SimpleRealm(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public String getId() {
            return "default";
        }

        @Override
        public String getPrincipal() {
            return username;
        }

        @Override
        public String getCredentials() {
            return password;
        }
        
    }

}
