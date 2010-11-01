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
        // TODO Auto-generated method stub
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
        return string == null ? null : "\"" + string + "\"";
    }
}
