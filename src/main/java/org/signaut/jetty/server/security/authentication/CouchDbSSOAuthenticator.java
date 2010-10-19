package org.signaut.jetty.server.security.authentication;

import java.io.IOException;
import java.security.Principal;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.authentication.LoginAuthenticator;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;
import org.eclipse.jetty.server.UserIdentity;
import org.signaut.couchdb.CouchDbAuthenticator;
import org.signaut.couchdb.UserContext;
import org.signaut.jetty.server.security.SerializablePrincipal;

public class CouchDbSSOAuthenticator extends LoginAuthenticator {

    private final String sessionTokenId = "AuthSession";
    private final Pattern authSessionCookiePattern = Pattern.compile(".*" + sessionTokenId + "=");
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
        final String sessionId = getSessionId(httpRequest);

        try {
            if ( ! mandatory) {
                return _deferred;
            }

            if (sessionId != null) {
                final UserContext userContext = couchDbAuthenticator.validate(sessionId);
                final UserIdentity user = getIdentity(userContext);
                if (user != null) {
                    return new UserAuthentication(this, user);
                }
            }
            if (_deferred.isDeferred(httpResponse)) {
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
    
    private String getSessionId(HttpServletRequest request) {
        final String cookieString = request.getHeader(HttpHeaders.COOKIE);
        final String tokens[] = authSessionCookiePattern.split(cookieString, 2);
        System.out.println("c -> " + cookieString);
        if (tokens.length > 1) {
            final String cookiePart = tokens[1];
            final int splitIndex = cookiePart.indexOf(";");
            if (splitIndex >= 0) {
                return cookiePart.substring(0, splitIndex);
            }else {
                return cookiePart;
            }
        }
        return null;
    }

    @Override
    public boolean secureResponse(ServletRequest request, ServletResponse response, boolean mandatory,
            User validatedUser) throws ServerAuthException {
        return true;
    }

}
