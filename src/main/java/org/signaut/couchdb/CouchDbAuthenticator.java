package org.signaut.couchdb;


public interface CouchDbAuthenticator {

    String authenticate(String username, String password);

    UserContext validate(String sessionId);
    
    String decodeAuthToken(String cookieString);
    
}
