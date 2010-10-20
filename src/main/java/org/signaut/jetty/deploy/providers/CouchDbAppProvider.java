package org.signaut.jetty.deploy.providers;

import java.io.File;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.Authenticator.Factory;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouchDbAppProvider extends AbstractLifeCycle implements AppProvider {

    private DeploymentManager deploymentManager;
    private final CouchDbProperties couchDbProperties;
    private final Authenticator.Factory authenticatorFactory;
    private final SessionManagerProvider sessionManagerProvider;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper(new JsonFactory()
            .enable(JsonParser.Feature.ALLOW_COMMENTS).enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES));

    private String serverClasses[] = { "com.google.inject." };
    private String systemClasses[] = { "org.slf4j." };

    public CouchDbAppProvider(CouchDbProperties couchDbProperties, Factory authenticatorFactory,
                              SessionManagerProvider sessionManagerProvider, DeploymentManager deploymentManager) {
        this.couchDbProperties = couchDbProperties;
        this.authenticatorFactory = authenticatorFactory;
        this.sessionManagerProvider = sessionManagerProvider;
        this.deploymentManager = deploymentManager;
    }

    @Override
    protected void doStart() throws Exception {
        // Do the initial deployment

        // Now listen for changes continuously for changes since the last
        // sequence

    }

    public void setServerClasses(String[] serverClasses) {
        this.serverClasses = serverClasses;
    }

    public void setSystemClasses(String[] systemClasses) {
        this.systemClasses = systemClasses;
    }

    @Override
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        if (isRunning()) {
            throw new IllegalStateException("running");
        }
        this.deploymentManager = deploymentManager;
    }

    @Override
    public ContextHandler createContextHandler(App app) throws Exception {
        final File file = new File(app.getOriginId());
        // Get descriptor from document

        // Download actual war from attachment
        final WebAppDescriptor descriptor = objectMapper.readValue(file, WebAppDescriptor.class);
        return createContext(descriptor);
    }

    private ContextHandler createContext(WebAppDescriptor desc) {
        log.info("Creating new context for " + desc);
        final WebAppContext context = new WebAppContext(desc.name, desc.contextPath);
        context.setServerClasses(concat(context.getServerClasses(), serverClasses));
        context.setSystemClasses(concat(context.getSystemClasses(), systemClasses));

        // context.setWar(home + desc.war);
        context.setErrorHandler(new JsonErrorHandler(desc.showFullStacktrace));
        context.getSecurityHandler().setAuthenticatorFactory(authenticatorFactory);
        context.setSessionHandler(new SessionHandler(sessionManagerProvider.get()));
        context.setParentLoaderPriority(desc.parentLoaderPriority);
        return context;
    }

    private final String[] concat(String[] l, String[] r) {
        if (l == null || l.length == 0) {
            return r;
        }
        if (r == null || r.length == 0) {
            return l;
        }
        final String combined[] = new String[l.length + r.length];
        System.arraycopy(l, 0, combined, 0, l.length);
        System.arraycopy(r, 0, combined, l.length, r.length);
        return combined;
    }

    public static class WebAppDescriptor {
        private String war;
        private String name;
        private String contextPath;
        private boolean showFullStacktrace = false;
        private boolean parentLoaderPriority = false;

        public String getWar() {
            return war;
        }

        public void setWar(String war) {
            this.war = war;
        }

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

        @JsonAnySetter
        public void setOptionalKey(String key, Object value) {
            //Ignore for now
        }
        
        @Override
        public String toString() {
            return "WebAppDescriptor [war=" + war + ", name=" + name + ", contextPath=" + contextPath + "]";
        }

    }

    public interface SessionManagerProvider {
        SessionManager get();
    }

    public static class CouchDbProperties {
        private String databaseUrl;
        private String username;
        private String password;
        private String view;
        private String filter;

        public CouchDbProperties(String databaseUrl, String username, String password, String view, String filter) {
            super();
            this.databaseUrl = databaseUrl;
            this.username = username;
            this.password = password;
            this.view = view;
            this.filter = filter;
        }

        public String getDatabaseUrl() {
            return databaseUrl;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getView() {
            return view;
        }

        public String getFilter() {
            return filter;
        }

        public CouchDbProperties setDatabaseUrl(String databaseUrl) {
            this.databaseUrl = databaseUrl;
            return this;
        }

        public CouchDbProperties setUsername(String username) {
            this.username = username;
            return this;
        }

        public CouchDbProperties setPassword(String password) {
            this.password = password;
            return this;
        }

        public CouchDbProperties setView(String view) {
            this.view = view;
            return this;
        }

        public CouchDbProperties setFilter(String filter) {
            this.filter = filter;
            return this;
        }

    }

}
