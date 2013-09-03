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
package org.signaut.jetty.server.security.authentication;

import java.io.IOException;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.authentication.DeferredAuthentication;
import org.eclipse.jetty.security.authentication.LoginAuthenticator;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.signaut.couchdb.CouchDbAuthenticator;
import org.signaut.couchdb.UserContext;
import org.signaut.jetty.server.security.SerializablePrincipal;

public class CouchDbSSOAuthenticator extends LoginAuthenticator {
    private final DeferredAuthentication _deferred=new DeferredAuthentication(this);
    private final CouchDbAuthenticator couchDbAuthenticator;

    public CouchDbSSOAuthenticator(CouchDbAuthenticator couchDbAuthenticator) {
        this.couchDbAuthenticator = couchDbAuthenticator;
    }

    @Override
    public String getAuthMethod() {
        return "COUCHDB";
    }

    @Override
    public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory)
            throws ServerAuthException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;
        String sessionId = null;
        final String cookie = httpRequest.getHeader(HttpHeader.COOKIE.asString());
        if (cookie != null) {
            //First try to find a AuthSession
            sessionId = couchDbAuthenticator.decodeAuthToken(cookie);
        }
        if (sessionId == null) {
            //If all else fails, use basic auth
            sessionId = basicAuth(httpRequest);
        }

        try {
            if ( ! mandatory) {
                return _deferred;
            }

            if (sessionId != null) {
                final UserContext userContext = couchDbAuthenticator.validate(sessionId);
                final UserIdentity user = getIdentity(userContext);
                if (user != null) {
                    return new UserAuthentication(getAuthMethod(), user);
                }
            }
            if (DeferredAuthentication.isDeferred(httpResponse)) {
                return Authentication.UNAUTHENTICATED;
            }
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return Authentication.SEND_CONTINUE;
        } catch (IOException e) {
            throw new ServerAuthException(e);
        }
    }

    private UserIdentity getIdentity(UserContext userContext) {
        final Principal principal = new SerializablePrincipal(userContext.getName());
        final Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        final String roles[] = userContext.getRoles();
        if (roles != null) {
            for (String role : roles) {
                subject.getPrincipals().add(new SerializablePrincipal(role));
            }
        }
        subject.setReadOnly();
        return _identityService.newUserIdentity(subject, principal, roles);
    }

    private String basicAuth(HttpServletRequest httpRequest) {
        final String auth = httpRequest.getHeader(HttpHeader.AUTHORIZATION.asString());
        if (auth != null) {
            final String decodedAuth;
            decodedAuth = B64Code.decode(auth.substring(auth.indexOf(' ')+1),StringUtil.__ISO_8859_1);
            final String authTokens[] = decodedAuth.split(":", 2);
            if (authTokens.length > 1) {

            } else {
                throw new IllegalArgumentException(String.format("bad authentication: %s", auth));
            }
            return couchDbAuthenticator.authenticate(authTokens[0], authTokens[1]);
        }
        return null;
    }

    @Override
    public boolean secureResponse(ServletRequest request, ServletResponse response, boolean mandatory,
            User validatedUser) throws ServerAuthException {
        return true;
    }


}
