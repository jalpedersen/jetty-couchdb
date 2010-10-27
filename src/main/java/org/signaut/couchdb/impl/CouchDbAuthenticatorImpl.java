package org.signaut.couchdb.impl;

import java.io.IOException;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.signaut.couchdb.CouchDbAuthenticator;
import org.signaut.couchdb.UserContext;

public class CouchDbAuthenticatorImpl implements CouchDbAuthenticator {

    private final String authenticationUrl;
    private final HttpClient httpClient;
    
    private final String sessionTokenId = "AuthSession";
    private final Pattern authSessionCookiePattern = Pattern.compile(".*" + sessionTokenId + "=");

    private final ObjectMapper objectMapper = new ObjectMapper(new JsonFactory()
            .enable(JsonParser.Feature.ALLOW_COMMENTS).enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES));

    public CouchDbAuthenticatorImpl(String authenticationUrl) {
        this.authenticationUrl = authenticationUrl;
        httpClient = new HttpClient();
        try {
            httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            httpClient.setThreadPool(new QueuedThreadPool(25));
            httpClient.setTimeout(5000);
            httpClient.start();
        } catch (Exception e) {
            throw new IllegalStateException("While starting httpClient", e);
        }
    }

    @Override
    public String authenticate(String username, String password) {
        final CouchDbAuthExchange exchange = getSessionAuthRequest();
        exchange.setMethod(HttpMethods.POST);
        try {
            // The username key is "name" - NOT "username"
            final String content = "name=" + username + "&password=" + password;
            final Buffer requestContent = new ByteArrayBuffer(content.getBytes("utf-8"));
            exchange.setRequestContent(requestContent);

            httpClient.send(exchange);

            exchange.waitForDone();
            return exchange.getSessionId();
        } catch (Exception e) {
            throw new IllegalStateException("While authenticating " + username, e);
        }
    }

    @Override
    public UserContext validate(String sessionId) {
        final CouchDbSessionExchange exchange = getSessionRequest(sessionId);
        exchange.setMethod(HttpMethods.GET);
        try {
            httpClient.send(exchange);
            exchange.waitForDone();
            final UserSession session = exchange.getUserSession();
            if (session != null && session.isOk()) {
                return session.getUserContext();
            }
        } catch (Exception e) {
            throw new IllegalStateException("While validating " + sessionId, e);
        }
        return null;
    }

    private CouchDbSessionExchange getSessionRequest(String sessionId) {
        final CouchDbSessionExchange exchange = new CouchDbSessionExchange();
        exchange.setRequestHeader("Cookie", sessionTokenId + "=" + sessionId);
        exchange.setRequestHeader("X-CouchDB-WWW-Authenticate", "Cookie");
        return setConnectionDetails(exchange);
    }

    private final <T extends HttpExchange> T setConnectionDetails(T exchange) {
        exchange.setURL(authenticationUrl);
        exchange.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF=8");
        return exchange;
    }

    private CouchDbAuthExchange getSessionAuthRequest() {
        return setConnectionDetails(new CouchDbAuthExchange());
    }

    private class CouchDbSessionExchange extends HttpExchange {

        private UserSession userSession;

        public UserSession getUserSession() {
            return userSession;
        }

        @Override
        protected void onResponseContent(Buffer content) throws IOException {
            userSession = objectMapper.readValue(content.toString(), UserSession.class);
        }

    }

    private class CouchDbAuthExchange extends HttpExchange {
        private String sessionId;

        public String getSessionId() {
            return sessionId;
        }

        @Override
        protected void onResponseHeader(Buffer name, Buffer value) throws IOException {
            if ("Set-Cookie".equals(name.toString()) && value != null) {
                final String cookie = value.toString();
                final String tokens[] = authSessionCookiePattern.split(cookie, 2);
                if (tokens.length > 1) {
                    final String cookiePart = tokens[1];
                    final int splitIndex = cookiePart.indexOf(";");
                    if (splitIndex >= 0) {
                        sessionId = cookiePart.substring(0, splitIndex);
                    }
                }
            }
        }
    }
}
