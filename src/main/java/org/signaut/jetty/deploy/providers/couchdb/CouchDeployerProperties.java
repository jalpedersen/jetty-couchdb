package org.signaut.jetty.deploy.providers.couchdb;

public class CouchDeployerProperties {
    private String databaseUrl;
    private String username;
    private String password;
    private String designDocument;
    private String filter;

    public CouchDeployerProperties() {
    }

    public CouchDeployerProperties(String databaseUrl, String username, String password, String designDocument,
                                   String filter) {
        super();
        this.databaseUrl = databaseUrl;
        this.username = username;
        this.password = password;
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

    public String getDesignDocument() {
        return designDocument;
    }

    public String getFilter() {
        return filter;
    }

    public CouchDeployerProperties setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
        return this;
    }

    public CouchDeployerProperties setUsername(String username) {
        this.username = username;
        return this;
    }

    public CouchDeployerProperties setPassword(String password) {
        this.password = password;
        return this;
    }

    public void setDesignDocument(String designDocument) {
        this.designDocument = designDocument;
    }

    public CouchDeployerProperties setFilter(String filter) {
        this.filter = filter;
        return this;
    }

}