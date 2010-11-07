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
    public String decodeAuthToken(String cookie) {
        if (cookie == null) {
            return null;
        }
        final String tokens[] = authSessionCookiePattern.split(cookie, 2);
        if (tokens.length > 1) {
            final String cookiePart = tokens[1];
            final int splitIndex = cookiePart.indexOf(";");
            if (splitIndex >= 0) {
                return (cookiePart.substring(0, splitIndex));
            } else {
                return cookiePart;
            }
        }
        return null;
    }
}
