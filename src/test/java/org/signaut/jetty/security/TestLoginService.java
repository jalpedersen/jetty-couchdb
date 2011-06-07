package org.signaut.jetty.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jetty.server.UserIdentity;
import org.junit.Assert;
import org.junit.Test;

import org.signaut.couchdb.CouchDbAuthenticator;
import org.signaut.couchdb.impl.CouchDbAuthenticatorImpl;
import org.signaut.jetty.server.security.CouchDbLoginService;
import org.signaut.jetty.server.security.SerializableIdentityService;

public class TestLoginService {

    @Test
    public void testLogin() {
        final CouchDbAuthenticator authenticator = new CouchDbAuthenticatorImpl("http://localhost:5984/_session");
        final ConcurrentMap<String, String> hz = new ConcurrentHashMap<String, String>();
        final CouchDbLoginService service = new CouchDbLoginService("test", authenticator, hz);
        service.setIdentityService(new SerializableIdentityService());

        UserIdentity notHere = service.login("nonValid", "blah");

        Assert.assertNull("User is not supposed to be bere", notHere);

        UserIdentity emptyUser = service.login("", "blah");
        Assert.assertNull("anonymous == null", emptyUser);

        UserIdentity id = service.login("knownUser", "myPassword");
        Assert.assertNotNull("User was null (Make sure that a user named 'knownUser' with password 'myPassword' exists in your local couchdb instance)", id);

    }

}
