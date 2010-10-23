package org.signaut.jetty.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.security.auth.Subject;

import org.eclipse.jetty.server.UserIdentity;
import org.junit.Test;
import org.signaut.jetty.server.security.SerializableIdentityService;
import org.signaut.jetty.server.security.SerializablePrincipal;

public class TestIdentityService {

    @Test
    public void testSerialization() throws IOException {
        SerializableIdentityService service = new SerializableIdentityService();
        UserIdentity identity = service.newUserIdentity(new Subject(), new SerializablePrincipal("testUser"),
                                                        new String[] { "myRole" });
        ObjectOutputStream outputStream = new ObjectOutputStream(new ByteArrayOutputStream());
        outputStream.writeObject(identity);
        outputStream.close();

    }
}
