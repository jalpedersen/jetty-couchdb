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
package org.signaut.jetty.deploy.providers.couchdb;


public class CouchDbDeployerProperties {
    private String databaseUrl;
    private String username;
    private String password;
    private String filter;
    private String designDocument;
    private int heartbeat = 5;
    private boolean compacting;
    private String temporaryDirectory = System.getProperty("java.io.tmpdir");

    public CouchDbDeployerProperties() {
        compacting = true;
    }

    public CouchDbDeployerProperties(String databaseUrl, String username, String password, String designDocument, String filter, int heartbeat) {
        super();
        this.databaseUrl = databaseUrl;
        this.username = username;
        this.password = password;
        this.designDocument = designDocument;
        this.filter = filter;
        this.heartbeat = heartbeat;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDesignDocument() {
        return designDocument;
    }

    public String getFilter() {
        return filter;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public String getPassword() {
        return password;
    }

    public String getTemporaryDirectory() {
        return temporaryDirectory;
    }

    public String getUsername() {
        return username;
    }

    public CouchDbDeployerProperties setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
        return this;
    }

    public void setDesignDocument(String designDocument) {
        this.designDocument = designDocument;
    }

    public CouchDbDeployerProperties setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public CouchDbDeployerProperties setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }

    public CouchDbDeployerProperties setPassword(String password) {
        this.password = password;
        return this;
    }

    public CouchDbDeployerProperties setTemporaryDirectory(String temporaryDirectory) {
        this.temporaryDirectory = temporaryDirectory;
        return this;
    }

    public CouchDbDeployerProperties setUsername(String username) {
        this.username = username;
        return this;
    }

    public boolean isCompacting() {
        return compacting;
    }

    public void setCompacting(boolean compacting) {
        this.compacting = compacting;
    }
}