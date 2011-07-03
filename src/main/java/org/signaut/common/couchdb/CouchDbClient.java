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
package org.signaut.common.couchdb;

import java.io.File;
import java.util.Map;

import org.signaut.common.http.SimpleHttpClient.HttpResponseHandler;

public interface CouchDbClient {
    public static class DocumentException extends RuntimeException {
        private static final long serialVersionUID = -7152085677408141413L;

        public DocumentException(String message, Throwable cause) {
            super(message, cause);
        }

        public DocumentException(String message) {
            super(message);
        }
    }

    <T> T get(String uri, HttpResponseHandler<T> handler);

    <T> T getDocument(String documentId, Class<T> type);
    
    DocumentStatus putDocument(String id, Object document);
    
    DocumentStatus postDocument(Object document);

    String downloadAttachment(String documentId, String name, File directory);

    DocumentStatus putDocument(String id, String document);

    DocumentStatus postDocument(String document);

    DocumentStatus deleteDocument(String id);

    DocumentStatus createDatabase();

    HttpResponseHandler<Map<String, Object>> getGenericMapHandler();

    HttpResponseHandler<Document> getDocumentHandler();

    CouchDbUser getUser(String name);

    CouchDbUser updateUser(CouchDbUser user);

    boolean deleteUser(CouchDbUser user);
}