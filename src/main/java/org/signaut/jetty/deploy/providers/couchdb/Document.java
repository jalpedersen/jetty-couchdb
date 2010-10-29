package org.signaut.jetty.deploy.providers.couchdb;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;

class Document {
    @JsonProperty("_attachments")
    private Map<String, Attachment> attachments;
    @JsonProperty("_id")
    private String id;
    @JsonProperty("_rev")
    private String revision;
    private Map<String, Object> properties = new HashMap<String, Object>();

    public String getId() {
        return id;
    }

    public Document setId(String id) {
        this.id = id;
        return this;
    }

    public String getRevision() {
        return revision;
    }

    public Document setRevision(String revision) {
        this.revision = revision;
        return this;
    }

    public Map<String, Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, Attachment> attachments) {
        this.attachments = attachments;
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    @JsonAnySetter
    public Document setOptional(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    static class Attachment {
        @JsonProperty("content_type")
        private String contentType;
        private long length;
        private boolean stub;

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

        public boolean isStub() {
            return stub;
        }

        public void setStub(boolean stub) {
            this.stub = stub;
        }

        @JsonAnySetter
        public void setOptional(String key, Object value) {
            // Ignore
        }
    }

}
