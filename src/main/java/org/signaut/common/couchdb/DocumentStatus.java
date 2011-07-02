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
