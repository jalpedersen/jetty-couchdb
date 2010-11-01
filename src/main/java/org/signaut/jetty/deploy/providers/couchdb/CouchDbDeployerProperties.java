package org.signaut.jetty.deploy.providers.couchdb;

public class CouchDbDeployerProperties {
    private String databaseUrl;
    private String username;
    private String password;
    private String filter;
    private int heartbeat = 10;

    public CouchDbDeployerProperties() {
    }

    public CouchDbDeployerProperties(String databaseUrl, String username, String password, String filter, int heartbeat) {
        super();
        this.databaseUrl = databaseUrl;
        this.username = username;
        this.password = password;
        this.filter = filter;
        this.heartbeat = heartbeat;
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

    public String getFilter() {
        return filter;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public CouchDbDeployerProperties setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
        return this;
    }

    public CouchDbDeployerProperties setUsername(String username) {
        this.username = username;
        return this;
    }

    public CouchDbDeployerProperties setPassword(String password) {
        this.password = password;
        return this;
    }

    public CouchDbDeployerProperties setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }

    public CouchDbDeployerProperties setFilter(String filter) {
        this.filter = filter;
        return this;
    }

}