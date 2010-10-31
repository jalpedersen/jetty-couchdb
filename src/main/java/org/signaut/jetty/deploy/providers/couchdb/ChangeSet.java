package org.signaut.jetty.deploy.providers.couchdb;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;

class ChangeSet {
    @JsonProperty("seq")
    private Long sequence;
    @JsonProperty("last_seq")
    private Long lastSequence;
    @JsonProperty("_id")
    private String id;
    private boolean deleted;

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
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

    public Long getLastSequence() {
        return lastSequence;
    }

    public void setLastSequence(Long lastSequence) {
        this.lastSequence = lastSequence;
    }

    @Override
    public String toString() {
        return "ChangeSet [sequence=" + sequence + ", id=" + id + ", deleted=" + deleted + "]";
    }

    @JsonAnySetter
    public void setOptional(String key, Object value) {
        // Ignore
    }
}