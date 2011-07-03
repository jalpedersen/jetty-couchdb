package org.signaut.jetty.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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
    private static final String knownUser;
    private static final String knownUserPassword;

    final static Properties properties = new Properties();
    static {
        final String confProperties = "/couchdb.properties";
        try {
            InputStream input = TestLoginService.class.getResourceAsStream(confProperties);
            if (input == null){
                throw new IOException(confProperties + " not found");
            }
            properties.load(input);
            knownUser = properties.getProperty("user");
            knownUserPassword = properties.getProperty("password");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + confProperties);
        }
    }

    @Test
    public void testLogin() {
        final CouchDbAuthenticator authenticator = new CouchDbAuthenticatorImpl(properties);
        final ConcurrentMap<String, String> hz = new ConcurrentHashMap<String, String>();
        final CouchDbLoginService service = new CouchDbLoginService("test", authenticator, hz);
        service.setIdentityService(new SerializableIdentityService());

        UserIdentity notHere = service.login("nonValid", "blah");

        Assert.assertNull("User is not supposed to be bere (Make sure that a user named \"notValid\" is not in your user database", notHere);

        UserIdentity emptyUser = service.login("", "blah");
        Assert.assertNull("anonymous != null", emptyUser);

        UserIdentity id = service.login(knownUser, knownUserPassword);
        Assert.assertNotNull("User was null", id);

        UserIdentity withBadPassword = service.login(knownUser, knownUserPassword+"_now_invalid");
        Assert.assertNull("User was not null", withBadPassword);
    }

}
