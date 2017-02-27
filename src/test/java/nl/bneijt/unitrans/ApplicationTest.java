package nl.bneijt.unitrans;

import com.google.common.io.Files;
import com.google.inject.Injector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;

public class ApplicationTest {

    @Test
    public void shouldBeAbleToStartAndStopSslContextFactory() throws Exception {
        Injector injector = TestUnitransModule.createInjector();
        Application application = injector.getInstance(Application.class);
        File keystoreLocation = File.createTempFile("server", ".jks");
        keystoreLocation.delete();
        assumeThat(keystoreLocation.exists(), is(false));
        SslContextFactory sslContextFactory = application.newSslContextFactory(keystoreLocation, "test");
        assertThat(keystoreLocation.exists(), is(true));
        try {
            sslContextFactory.start();
            sslContextFactory.stop();
        } catch (IOException e) {
            if (e.getCause() instanceof javax.crypto.BadPaddingException) {
                Assert.fail("Could not start SslContextFactory, possibly using the wrong password for the keystore/truststore");
            }
            throw e;
        }


    }

}
