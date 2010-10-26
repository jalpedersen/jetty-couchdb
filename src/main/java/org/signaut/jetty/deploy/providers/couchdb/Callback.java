package org.signaut.jetty.deploy.providers.couchdb;

import org.eclipse.jetty.io.Buffer;

interface Callback {
    void onMessage(Buffer arg);
}