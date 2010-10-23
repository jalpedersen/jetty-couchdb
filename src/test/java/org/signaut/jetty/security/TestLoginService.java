package org.signaut.jetty.security;

import org.eclipse.jetty.server.UserIdentity;
import org.junit.Assert;
import org.junit.Test;
import org.signaut.common.hazelcast.HazelcastFactory;
import org.signaut.couchdb.CouchDbAuthenticator;
import org.signaut.couchdb.impl.CouchDbAuthenticatorImpl;
import org.signaut.jetty.server.security.CouchDbLoginService;
import org.signaut.jetty.server.security.SerializableIdentityService;

import com.hazelcast.core.HazelcastInstance;

public class TestLoginService {

    @Test
    public void testLogin() {
        final CouchDbAuthenticator authenticator = new CouchDbAuthenticatorImpl("localhost");
        final HazelcastInstance hz = new HazelcastFactory()
                .loadHazelcastInstance("/test-login-cluster.xml", getClass());
        final CouchDbLoginService service = new CouchDbLoginService("test", authenticator, hz);
        service.setIdentityService(new SerializableIdentityService());

        UserIdentity notHere = service.login("nonValid", "blah");

        Assert.assertNull("User is not supposed to be bere", notHere);

        UserIdentity emptyUser = service.login("", "blah");
        Assert.assertNull("anonymous == null", emptyUser);

        UserIdentity id = service.login("knownUser", "myPassword");
        Assert.assertNotNull("User was null", id);

    }

}
