package org.signaut.jetty.deploy.providers;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.io.UTF8StreamWriter;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;

class JsonErrorHandler extends ErrorHandler {

    private boolean showingStackTrace;
    private final String cacheControlString = "must-revalidate,no-cache,no-store";

    public JsonErrorHandler(boolean showingStackTrace) {
        this.showingStackTrace = showingStackTrace;
    }

    public boolean isShowingStackTrace() {
        return showingStackTrace;
    }

    public void setShowingStackTrace(boolean showingStackTrace) {
        this.showingStackTrace = showingStackTrace;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        final HttpConnection connection = HttpConnection.getCurrentConnection();
        connection.getRequest().setHandled(true);
        final String method = request.getMethod();
        if (!method.equals(HttpMethods.GET) && !method.equals(HttpMethods.POST) && !method.equals(HttpMethods.HEAD)) {
            return;
        }
        response.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
        response.setStatus(connection.getResponse().getStatus());
        response.setHeader(HttpHeaders.CACHE_CONTROL, cacheControlString);
        final UTF8StreamWriter writer = new UTF8StreamWriter();
        try {
            createErrorMessage(writer, request, connection.getResponse());
            writer.setOutput(response.getOutputStream());
            writer.close();
        } finally {
            writer.close();
        }
    }

    private void createErrorMessage(Writer writer, HttpServletRequest request, Response response) throws IOException {
        final Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
        final String message;
        if (exception == null) {
            message = String.format("{\"status\": %d, \"uri\":\"%s\"," + "\"message\": %s}", response.getStatus(),
                                    request.getRequestURI(), jsonStr(response.getReason()));
        } else {
            message = String.format("{\"status\": %d, \"uri\":\"%s\"," + "\"message\": %s, " + "\"exception\": { "
                                            + "\"exception\": \"%s\", \"message\": %s} }", response.getStatus(),
                                    request.getRequestURI(),
                                    jsonStr(response.getReason()), exception.getClass().getCanonicalName(),
                                    jsonStr(exception.getMessage()));
        }
        writer.write(message);
    }

    private final String jsonStr(String string) {
        return string == null ? null : "\"" + string + "\"";
    }
}
