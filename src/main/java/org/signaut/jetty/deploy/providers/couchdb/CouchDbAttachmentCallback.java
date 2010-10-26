package org.signaut.jetty.deploy.providers.couchdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jetty.io.Buffer;

class CouchDbAttachmentCallback implements Callback {
    private final String filename;
    private File directory;

    public CouchDbAttachmentCallback(String filename, File directory) {
        super();
        this.filename = filename;
        this.directory = directory;
    }

    @Override
    public void onMessage(Buffer arg) {
        FileOutputStream file = null;
        try {
            file = new FileOutputStream(new File(directory, filename));
            arg.writeTo(file);
        } catch (Exception e) {
            throw new IllegalStateException(filename, e);
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    throw new IllegalStateException(filename, e);
                }
            }
        }
    }
}
