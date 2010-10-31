package org.signaut.jetty.deploy.providers.couchdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicLong;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.Authenticator.Factory;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;
import org.signaut.common.http.SimpleHttpClient.HttpResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouchDbAppProvider extends AbstractLifeCycle implements AppProvider {

    public interface SessionManagerProvider {
        /**
         * Create a new instance of a SessionManager
         * 
         * @return
         */
        SessionManager get();
    }

    private DeploymentManager deploymentManager;
    private final CouchDeployerProperties couchDeployerProperties;
    private final Authenticator.Factory authenticatorFactory;
    private final SessionManagerProvider sessionManagerProvider;
    private final CouchDbClient couchDbClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(getClass());
    /*
     * Latest couchdb sequence. Used in the event the connection between
     * this and couchdb is broken. If we did not have the latest sequence, all
     * apps would be redeployed, and we don't want that.
     */
    private final AtomicLong sequence = new AtomicLong();
    
    private Thread changeListenerThread;
    private String serverClasses[] = { "com.google.inject." };
    private String systemClasses[] = { "org.slf4j." };

    public CouchDbAppProvider(CouchDeployerProperties couchDeployerProperties, Factory authenticatorFactory,
                              SessionManagerProvider sessionManagerProvider) {
        this.couchDeployerProperties = couchDeployerProperties;
        this.authenticatorFactory = authenticatorFactory;
        this.sessionManagerProvider = sessionManagerProvider;
        couchDbClient = new CouchDbClientImpl(couchDeployerProperties.getDatabaseUrl(), couchDeployerProperties.getUsername(),
                couchDeployerProperties.getPassword());
        
    }

    @Override
    protected void doStart() {
        if (changeListenerThread != null) {
            throw new IllegalArgumentException("Already running");
        }
        changeListenerThread = new ChangeListener();
        changeListenerThread.start();
    }
    
    private final class ChangeListener extends Thread {
        final HttpResponseHandler<Void> changeSetHandler = new HttpResponseHandler<Void>(){

            @Override
            public Void handleInput(int responseCode, HttpURLConnection connection) {
                try {
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String change;
                    while ((change = reader.readLine())!=null) {
                        final ChangeSet changeSet = decode(change, ChangeSet.class);
                        if (changeSet == null || changeSet.getSequence() == null) {
                            if (changeSet.getLastSequence() == null) {
                                throw new IllegalStateException(String.format("bad change: %s", change));
                            }
                        }

                        //undeploy if needed
                        final App oldApp = deploymentManager.getAppByOriginId(changeSet.getId());
                        if (oldApp != null) {
                            log.debug("Undeploying {} at {}", oldApp.getOriginId(), oldApp.getContextId());
                            deploymentManager.removeApp(oldApp);
                        }
                        if ( ! changeSet.isDeleted()) {
                            deploymentManager.addApp(new App(deploymentManager, CouchDbAppProvider.this, changeSet.getId()));
                        }
                        sequence.set(changeSet.getSequence());
                    }
                } catch (IOException e) {
                    //Ignore
                }
                return null;
            }};
        
        @Override
        public void run() {
            while (isRunning()) {
                try {
                    log.info("CouchDB sequence: " + sequence.get());
                    couchDbClient.get("/_changes?feed=continuous" +
                                      "&filter="+couchDeployerProperties.getFilter()+
                                      "&since="+sequence.get(), 
                                      changeSetHandler);
                } catch (Throwable t) {
                    log.error("While listening for changes", t);
                }
            }
        }
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
        final WebAppDocument wepapp = couchDbClient.getDocument(app.getOriginId(), WebAppDocument.class);
        //Search for a suitable war file
        for (String s: wepapp.getAttachments().keySet()) {
            if (s.endsWith(".war")) {
                final File directory = new File("/tmp"+"/"+app.getOriginId());
                wepapp.setWar(couchDbClient.downloadAttachment(app.getOriginId(), s, directory));
                break;
            }
        }
        log.debug(wepapp.toString());
        return createContext(wepapp);
    }

    private ContextHandler createContext(WebAppDocument desc) {
        log.info("Creating new context for " + desc);
        final WebAppContext context = new WebAppContext(desc.getName(), desc.getContextPath());
        context.setServerClasses(concat(context.getServerClasses(), serverClasses));
        context.setSystemClasses(concat(context.getSystemClasses(), systemClasses));

        context.setWar(desc.getWar());
        final ErrorHandler errorHandler = new JsonErrorHandler();
        errorHandler.setShowStacks(desc.isShowingFullStacktrace());
        context.setErrorHandler(errorHandler);
        context.getSecurityHandler().setAuthenticatorFactory(authenticatorFactory);
        context.setSessionHandler(new SessionHandler(sessionManagerProvider.get()));
        context.setParentLoaderPriority(false);
        return context;
    }

    private <T> T decode(String str, Class<T> type) {
        try {
            return objectMapper.readValue(str, type);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("While parsing %s as %s", str, type), e);
        }
    }
    
    
    public void setServerClasses(String[] serverClasses) {
        this.serverClasses = serverClasses;
    }

    public void setSystemClasses(String[] systemClasses) {
        this.systemClasses = systemClasses;
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

}
