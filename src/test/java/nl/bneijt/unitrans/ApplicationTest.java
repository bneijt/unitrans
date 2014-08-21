package nl.bneijt.unitrans;

import com.google.inject.Injector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ApplicationTest {

    @Test
    public void shouldBeAbleToStartAndStopSslContextFactory() throws Exception {
        Injector injector = TestUnitransModule.createInjector();
        Application application = injector.getInstance(Application.class);
        SslContextFactory sslContextFactory = application.newSslContextFactory();
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
