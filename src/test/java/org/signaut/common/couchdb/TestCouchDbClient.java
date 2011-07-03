package org.signaut.common.couchdb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jetty.server.UserIdentity;
import org.junit.Test;
import org.signaut.couchdb.CouchDbAuthenticator;
import org.signaut.couchdb.impl.CouchDbAuthenticatorImpl;
import org.signaut.jetty.security.TestLoginService;
import org.signaut.jetty.server.security.CouchDbLoginService;
import org.signaut.jetty.server.security.SerializableIdentityService;

public class TestCouchDbClient {
    private final static CouchDbClient client;
    final static Properties properties = new Properties();
    static {
        final String confProperties = "/couchdb.properties";
        try {
            InputStream input = TestLoginService.class.getResourceAsStream(confProperties);
            if (input == null){
                throw new IOException(confProperties + " not found");
            }
            properties.load(input);
            client = new CouchDbClientImpl(properties.getProperty("host", "http://localhost:5984")+"/_users", 
                                           properties.getProperty("user"), 
                                           properties.getProperty("password"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + confProperties);
        }
    }

    @Test
    public void testUserLifecycle() {
        final String username = "testing_user_creation";
        final String password = "hello";
        final CouchDbUser new_user = new CouchDbUser().setName(username).setPlainTextPassword(password);
        //Clean up after any potential previous failures
        client.deleteUser(client.getUser(username));
        
        final CouchDbUser updated_user = client.updateUser(new_user);
        assertNotNull("user document should have a revision", updated_user.getRevision());
        
        //Try logging in with the new user
        final CouchDbAuthenticator authenticator = new CouchDbAuthenticatorImpl(properties);
        final ConcurrentMap<String, String> hz = new ConcurrentHashMap<String, String>();
        final CouchDbLoginService service = new CouchDbLoginService("test", authenticator, hz);
        service.setIdentityService(new SerializableIdentityService());

        final UserIdentity userIdentity = service.login(username, password);
        assertNotNull("Failed to login with new user", userIdentity);
        
        client.deleteUser(updated_user);
        final CouchDbUser deletedUser = client.getUser(username);
        assertNull("User should not be here", deletedUser);

    }
}
