package org.signaut.common.couchdb;

@SuppressWarnings("serial")
public class CouchDbException extends RuntimeException {

    public CouchDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public CouchDbException(String message) {
        super(message);
    }

}
