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
package org.signaut.jetty.deploy.providers.couchdb;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.handler.ErrorHandler;
/**
 * Display the error as a json object
 * 
 */
class JsonErrorHandler extends ErrorHandler {

    @Override
    protected void writeErrorPage(HttpServletRequest request, Writer writer, int code, String message,
            boolean showStacks) throws IOException {
        final Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
        final String user = request.getUserPrincipal()==null?null:request.getUserPrincipal().getName();
        if (exception == null) {
            message = String.format("{\"status\": %d, \"uri\":\"%s\", \"user\": %s, \"message\": %s}", code,
                                    request.getRequestURI(), jsonStr(user), jsonStr(message));
        } else {
            message = String.format("{\"status\": %d, \"uri\":\"%s\", \"user\": %s, \"message\": %s, \"exception\": { "
                                            + "\"exception\": \"%s\", \"message\": %s} }", code,
                                    request.getRequestURI(), jsonStr(user), jsonStr(message),
                                    exception.getClass().getCanonicalName(), jsonStr(exception.getMessage()));
        }
        writer.write(message);
    }

    private final String jsonStr(String string) {
        return string == null ? null : new StringBuilder("\"").append(string).append("\"").toString();
    }
}
