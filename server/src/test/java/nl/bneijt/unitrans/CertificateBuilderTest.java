package nl.bneijt.unitrans;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class CertificateBuilderTest {
    @Test
    public void generateKeyStore() throws Exception {
        File tempStorageLocation = File.createTempFile("keystoretest", ".jks");
        CertificateBuilder.generateKeyStore(tempStorageLocation, "test");

        assertThat("Should clean up", tempStorageLocation.delete(), is(true));
    }

}