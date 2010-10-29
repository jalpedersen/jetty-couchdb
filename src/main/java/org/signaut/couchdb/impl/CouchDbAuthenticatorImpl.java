package org.signaut.couchdb.impl;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.signaut.common.http.SimpleHttpClient;
import org.signaut.common.http.SimpleHttpClient.HttpResponseHandler;
import org.signaut.common.http.SimpleHttpClientImpl;
import org.signaut.couchdb.CouchDbAuthenticator;
import org.signaut.couchdb.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouchDbAuthenticatorImpl implements CouchDbAuthenticator {

    private final URL authUrl;
    private final String sessionTokenId = "AuthSession";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Pattern authSessionCookiePattern = Pattern.compile(".*" + sessionTokenId + "=");
    private final SimpleHttpClient httpClient = new SimpleHttpClientImpl();
    private final Map<String, String> authHeaders = new HashMap<String, String>();
    private final ObjectMapper objectMapper = new ObjectMapper(new JsonFactory()
            .enable(JsonParser.Feature.ALLOW_COMMENTS).enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES));

    public CouchDbAuthenticatorImpl(String authenticationUrl) {
        try {
            this.authUrl = new URL(authenticationUrl);
            authHeaders.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF=8");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Bad authentication url: " + authenticationUrl, e);
        }
    }

    private final class AuthHandler implements HttpResponseHandler<String> {

        @Override
        public String handleInput(int responseCode, HttpURLConnection connection) {
            if (responseCode < 400) {
                return decodeAuthToken(connection.getHeaderField("Set-Cookie"));
            }
            return null;
        }
    }

    private final class UserSessionHandler implements HttpResponseHandler<UserSession> {

        @Override
        public UserSession handleInput(int responseCode, HttpURLConnection connection) {
            if (responseCode < 400) {
                try {
                    return objectMapper.readValue(connection.getInputStream(), UserSession.class);
                } catch (Exception e) {
                    log.error("Bad user session", e);
                }
            }
            return null;
        }

    }

    @Override
    public String authenticate(String username, String password) {
        final String content = "name=" + username + "&password=" + password;
        return httpClient.post(authUrl, new AuthHandler(), content, authHeaders);
    }

    @Override
    public UserContext validate(String sessionId) {
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", sessionTokenId + "=" + sessionId);
        headers.put("X-CouchDB-WWW-Authenticate", "Cookie");
        final UserSession session = httpClient.get(authUrl, new UserSessionHandler(), headers);
        if (session != null && session.isOk()) {
            return session.getUserContext();
        }
        return null;
    }

    @Override
    public String decodeAuthToken(String cookieString) {
        final String cookie = cookieString.toString();
        final String tokens[] = authSessionCookiePattern.split(cookie, 2);
        if (tokens.length > 1) {
            final String cookiePart = tokens[1];
            final int splitIndex = cookiePart.indexOf(";");
            if (splitIndex >= 0) {
                return (cookiePart.substring(0, splitIndex));
            }
        }
        return null;
    }
}
