package org.signaut.couchdb.impl;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.signaut.couchdb.UserContext;

public class UserSession {

    @JsonProperty("userCtx")
    private UserContext userContext;
    
    private boolean ok;
    
    public UserContext getUserContext() {
        return userContext;
    }

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }

    public boolean isOk() {
        return ok;
    }



    public void setOk(boolean ok) {
        this.ok = ok;
    }



    @JsonAnySetter
    public void String(String key, Object value) {
        //Ignore unknown values
    }
}
