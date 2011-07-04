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

public class CouchDbLoginService extends AbstractLifeCycle implements LoginService {

    private IdentityService identityService;
    private final String name;
    private final CouchDbAuthenticator couchDbAuthenticator;
    private final ConcurrentMap<String, String> activeUsers;

    public CouchDbLoginService(String name, CouchDbAuthenticator couchDbAuthenticator,
                               ConcurrentMap<String, String> activeUsers) {
        this.name = name;
        this.couchDbAuthenticator = couchDbAuthenticator;
        this.activeUsers = activeUsers;
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
                    //Update session token in case it has changed
                    final String newToken = session.getAuthToken();
                    if (newToken != null && ! sessionId.equals(newToken)) {
                        activeUsers.put(username, newToken);
                    }
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

    @Override
    public void logout(UserIdentity user) {
        activeUsers.remove(user.getUserPrincipal().getName());
        
    }
}
