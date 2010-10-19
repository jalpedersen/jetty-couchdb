package org.signaut.jetty.server.security.authentication;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.security.Authenticator.Configuration;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Test;
import org.signaut.couchdb.CouchDbAuthenticator;
import org.signaut.couchdb.UserContext;
import org.signaut.jetty.server.security.SerializableIdentityService;

public class TestSSOAuthenticator {
    final Mockery m = new Mockery();
    
    @Test
    public void testSso() throws ServerAuthException, IOException {
        final HttpServletRequest req = m.mock(HttpServletRequest.class);
        final HttpServletResponse res = m.mock(HttpServletResponse.class);
        final CouchDbAuthenticator couchDbAuthenticator = m.mock(CouchDbAuthenticator.class);
        final Configuration configuration = m.mock(Configuration.class);
        final LoginService loginService = m.mock(LoginService.class);
        final String sessionId = "424242abc";
        m.checking(new Expectations() {{
            oneOf(configuration).getIdentityService(); will(returnValue(new SerializableIdentityService()));
            oneOf(configuration).getLoginService(); will(returnValue(loginService));
            oneOf(req).getHeader(HttpHeaders.COOKIE); will(returnValue("Something=blah;AuthSession="+sessionId+";SomethingElse=blahblah"));
            oneOf(couchDbAuthenticator).validate(sessionId); will(returnValue(new UserContext("jalp", null)));
            oneOf(res).sendError(HttpServletResponse.SC_FORBIDDEN);
            
        }});
        final CouchDbSSOAuthenticator ssoAuthenticator = new CouchDbSSOAuthenticator(couchDbAuthenticator);
        ssoAuthenticator.setConfiguration(configuration);
        Authentication auth = ssoAuthenticator.validateRequest(req, res, true);
        Assert.assertTrue("Not a user authentication", auth instanceof UserAuthentication);
    }
    
}
