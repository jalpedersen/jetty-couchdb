package org.signaut.jetty.security.authentication;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.security.Authenticator.AuthConfiguration;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.junit.Assert;
import org.junit.Test;
import org.signaut.couchdb.CouchDbAuthenticator;
import org.signaut.couchdb.UserContext;
import org.signaut.couchdb.impl.CouchDbAuthenticatorImpl;
import org.signaut.jetty.server.security.SerializableIdentityService;
import org.signaut.jetty.server.security.authentication.CouchDbSSOAuthenticator;
import static org.mockito.Mockito.*;

public class TestSSOAuthenticator {
    
    @Test
    public void testSso() throws ServerAuthException, IOException {
        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse res = mock(HttpServletResponse.class);
        final CouchDbAuthenticator couchDbAuthenticator = mock(CouchDbAuthenticator.class);
        final AuthConfiguration configuration = mock(AuthConfiguration.class);
        final LoginService loginService = mock(LoginService.class);
        final String sessionId = "424242abc";
        final String cookieString = "Something=blah; AuthSession="+sessionId+"; SomethingElse=blahblah";
        when(configuration.getIdentityService()).thenReturn(new SerializableIdentityService());
        when(configuration.getLoginService()).thenReturn(loginService);
        when(configuration.isSessionRenewedOnAuthentication()).thenReturn(false);
        when(req.getHeader(HttpHeaders.COOKIE)).thenReturn(cookieString);
        when(couchDbAuthenticator.decodeAuthToken(cookieString)).thenReturn(sessionId);
        when(couchDbAuthenticator.validate(sessionId)).thenReturn(new UserContext("jalp", null));
        
        final CouchDbSSOAuthenticator ssoAuthenticator = new CouchDbSSOAuthenticator(couchDbAuthenticator);
        ssoAuthenticator.setConfiguration(configuration);
        Authentication auth = ssoAuthenticator.validateRequest(req, res, true);
        Assert.assertTrue("Not a user authentication", auth instanceof UserAuthentication);
    }

    @Test
    public void testCookie() {
        final CouchDbAuthenticator realAuthenticator = new CouchDbAuthenticatorImpl("http://notused");
        final String sessionId1 = "424242abc";
        final String cookieString1 = "Something=blah; AuthSession="+sessionId1+"; SomethingElse=blahblah";
        Assert.assertEquals(sessionId1, realAuthenticator.decodeAuthToken(cookieString1));
        
        final String sessionId2 = "a25vd25Vc2VyOjRDQ0NDMjM3Oi9mO6CQMYje4dQ4KVTPnEGqML7Z";
        final String cookieString2 = "JSESSIONID=jalp-laptop188ry8banfewr1phy6ovvpkz3i.jalp-laptop; AuthSession=a25vd25Vc2VyOjRDQ0NDMjM3Oi9mO6CQMYje4dQ4KVTPnEGqML7Z";
        Assert.assertEquals(sessionId2, realAuthenticator.decodeAuthToken(cookieString2));
        

    }
    
}
