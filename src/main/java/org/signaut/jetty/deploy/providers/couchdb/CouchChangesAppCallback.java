package org.signaut.jetty.deploy.providers.couchdb;

import java.io.BufferedReader;
import java.io.StringReader;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.io.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CouchChangesAppCallback implements Callback {

    protected final DeploymentManager deploymentManager;
    protected final CouchDbAppProvider appProvider;
    protected final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(getClass());

    public CouchChangesAppCallback(DeploymentManager deploymentManager, CouchDbAppProvider appProvider) {
        log.info("Starting couchdb changes deployment manager");
        this.deploymentManager = deploymentManager;
        this.appProvider = appProvider;
    }

    @Override
    public void onMessage(Buffer arg) {
        try {
            final BufferedReader reader = new BufferedReader(new StringReader(arg.toString("utf-8")));
            String str;
            while ((str = reader.readLine()) != null) {
                final ChangeSet changeSet = objectMapper.readValue(str, ChangeSet.class);
                log.info("adding " + changeSet.getId());
                deploymentManager.addApp(new App(deploymentManager, appProvider, changeSet.getId()));
                appProvider.setSequence(changeSet.getSequence());
            }
        } catch (Exception e) {
            log.error(String.format("While preparing deployment:\n %s", arg.toString("utf-8")), e);
        }

    }

    static class ChangeSet {
        @JsonProperty("seq")
        private long sequence;
        @JsonProperty("_id")
        private String id;

        public long getSequence() {
            return sequence;
        }

        public void setSequence(long sequence) {
            this.sequence = sequence;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setOptional(String key, Object value) {
            // Ignore
        }
    }

}
