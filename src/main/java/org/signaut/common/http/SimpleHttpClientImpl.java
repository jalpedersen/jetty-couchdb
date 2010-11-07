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
package org.signaut.common.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHttpClientImpl implements SimpleHttpClient {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public <T> T post(URL url, HttpResponseHandler<T> handler, String content, Map<String, String> headers) {
        return send("POST", url, handler, content, headers);
    }

    @Override
    public <T> T get(URL url, HttpResponseHandler<T> handler, Map<String, String> headers) {
        return send("GET", url, handler, null, headers);
    }

    
    public <T> T send(String method, URL url, HttpResponseHandler<T> handler, String content, Map<String, String> headers) {
        HttpURLConnection connection = null;
        OutputStream out = null;
        OutputStreamWriter writer = null;
        InputStream in = null;
        T result = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            for (Entry<String, String> entry: headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            if (content != null) {
                connection.setDoOutput(true);
                out = connection.getOutputStream();
                writer = new OutputStreamWriter(out);
                writer.write(content);
                writer.close();
            }
            in = connection.getInputStream();
            result = handler.handleInput(responseCode(connection), connection);
        } catch (IOException e) {
            result = handler.handleInput(responseCode(connection), connection);
        } finally {
            close(writer, out, in);
        }
        return result;
    }

    
    private int responseCode(HttpURLConnection connection) {
        if (connection != null){
            try {
                return connection.getResponseCode();
            } catch (IOException e) {
                //Fall though
            }
        }
        return -1;
    }
    
    private void close(Closeable... streams) {
        for (Closeable stream : streams) {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                log.warn("", e);
            }
        }
    }
}
