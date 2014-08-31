package nl.bneijt.unitrans;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ApplicationIntegrationTest {

    private static Application application;

    @BeforeClass
    public static void startServer() throws Exception {
        ArgumentParser parser = Application.argumentParser();
        Namespace res = parser.parseArgs(new String[]{});
        Injector injector = Guice.createInjector(new UnitransModule(new CommandlineConfiguration(res)));

        application = injector.getInstance(Application.class);
        application.runServer();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        application.stop();
    }


    @Test
    public void serverShouldRequireCertificate() throws Exception {
        //TODO
    }
}
