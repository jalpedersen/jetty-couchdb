package org.signaut.jetty.server.security;

import java.security.Principal;
import java.util.concurrent.ConcurrentMap;

import javax.security.auth.Subject;

import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.signaut.couchdb.CouchDbAuthenticator;
import org.signaut.couchdb.UserContext;

import com.hazelcast.core.HazelcastInstance;

public class CouchDbLoginService extends AbstractLifeCycle implements LoginService {

    private IdentityService identityService;
    private final String name;
    private final CouchDbAuthenticator couchDbAuthenticator;
    private final ConcurrentMap<String, String> activeUsers;
    private final String USERS_MAP = "signaut.activeUsers";

    public CouchDbLoginService(String name, CouchDbAuthenticator couchDbAuthenticator,
                               HazelcastInstance hazelcastInstance) {
        this.name = name;
        this.couchDbAuthenticator = couchDbAuthenticator;
        this.activeUsers = hazelcastInstance.getMap(USERS_MAP);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UserIdentity login(String username, Object credentials) {
        if (username == null || "".equals(username.trim())) {
            return null;
        }
        final String sessionId = couchDbAuthenticator.authenticate(username, (String) credentials);

        if (sessionId != null) {
            final UserContext user = couchDbAuthenticator.validate(sessionId);
            if (user != null && user.getName() != null) {
                this.activeUsers.put(username, sessionId);
                return userIdentity(username, credentials, user.getRoles());
            }
        }
        return null;
    }

    private UserIdentity userIdentity(String username, Object credentials, String roles[]) {
        final Principal userPrincipal = new SerializablePrincipal(username);
        final Subject subject = new Subject();
        subject.getPrincipals().add(userPrincipal);
        subject.getPrivateCredentials().add(credentials);

        if (roles != null) {
            for (String role : roles) {
                subject.getPrincipals().add(new SerializablePrincipal(role));
            }
        }
        subject.setReadOnly();
        return identityService.newUserIdentity(subject, userPrincipal, roles);
    }

    @Override
    public boolean validate(UserIdentity user) {
        if (user != null && user.getUserPrincipal() != null) {
            final String username = user.getUserPrincipal().getName();
            final String sessionId = activeUsers.get(username);
            if (sessionId != null) {
                final UserContext session = couchDbAuthenticator.validate(sessionId);
                if (session != null) {
                    return true;
                }
            }
            // User's session is no longer valid - remove sessionId from cache
            activeUsers.remove(username);
        }
        return false;
    }

    @Override
    public IdentityService getIdentityService() {
        return identityService;
    }

    @Override
    public void setIdentityService(IdentityService service) {
        this.identityService = service;
    }
}
