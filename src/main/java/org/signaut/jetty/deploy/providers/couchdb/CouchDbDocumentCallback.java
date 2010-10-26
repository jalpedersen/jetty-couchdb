package org.signaut.jetty.deploy.providers.couchdb;

import java.io.File;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.io.Buffer;

class CouchDbDocumentCallback implements Callback {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CouchDbClient couchDbClient;
    private final File tempDirectory;

    private WebAppDocument webApp;

    public static class WebAppDocument extends Document {
        private String name;
        private String contextPath;
        private String war;
        private boolean showingFullStacktrace;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContextPath() {
            return contextPath;
        }

        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }

        public String getWar() {
            return war;
        }

        public void setWar(String war) {
            this.war = war;
        }

        public boolean isShowingFullStacktrace() {
            return showingFullStacktrace;
        }

        public void setShowingFullStacktrace(boolean showingFullStacktrace) {
            this.showingFullStacktrace = showingFullStacktrace;
        }
    }

    public CouchDbDocumentCallback(CouchDbClient couchDbClient, File tempDirectory) {
        this.couchDbClient = couchDbClient;
        this.tempDirectory = tempDirectory;
    }

    public WebAppDocument getWebApp() {
        return webApp;
    }

    @Override
    public void onMessage(Buffer arg) {
        try {
            webApp = objectMapper.readValue(arg.toString("utf-8"), WebAppDocument.class);
            // Now download attachment
            if (webApp.getAttachments() != null) {
                for (String s : webApp.getAttachments().keySet()) {
                    if (s.endsWith(".war")) {
                        CouchDbAttachmentCallback cb = new CouchDbAttachmentCallback(s, tempDirectory);
                        HttpExchange exchange = couchDbClient.dispatchGetDocument(webApp.getId() + "/" + s, cb);
                        exchange.waitForDone();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

    }

}
