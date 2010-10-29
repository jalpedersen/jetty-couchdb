package org.signaut.jetty.deploy.providers.couchdb;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;

class ChangeSet {
    @JsonProperty("seq")
    private long sequence;
    @JsonProperty("_id")
    private String id;
    private boolean deleted;
    
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @JsonAnySetter
    public void setOptional(String key, Object value) {
        // Ignore
    }
}