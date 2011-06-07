package org.signaut.common.couchdb;

import org.codehaus.jackson.annotate.JsonProperty;

public class DocumentStatus {

    private boolean ok;
    private String id;
    @JsonProperty("rev")
    private String revision;
    
    private String error;
    private String reason;
    
    public String getError() {
        return error;
    }
    public String getId() {
        return id;
    }
    public String getReason() {
        return reason;
    }
    public String getRevision() {
        return revision;
    }
    public boolean isOk() {
        return ok;
    }
    public void setError(String error) {
        this.error = error;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setOk(boolean ok) {
        this.ok = ok;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public void setRevision(String revision) {
        this.revision = revision;
    }
    @Override
    public String toString() {
        return "{\"ok\":\"" + ok +"\""+ (id != null ? ", id\":\"" + id +"\"": "")
                + (revision != null ? ", revision\":\"" + revision +"\"" : "")
                + (error != null ? ", error\":\"" + error +"\"": "") + (reason != null ? ", reason\":\"" + reason +"\"": "")
                + "}";
    }
}
