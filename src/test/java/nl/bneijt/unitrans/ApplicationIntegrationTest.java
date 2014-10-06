package nl.bneijt.unitrans;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Socket;

public class ApplicationIntegrationTest {

    private static Application application;
    private static CommandlineConfiguration commandlineConfiguration;

    @BeforeClass
    public static void startServer() throws Exception {
        ArgumentParser parser = Application.argumentParser();
        Namespace res = parser.parseArgs(new String[]{});
        commandlineConfiguration = new CommandlineConfiguration(res);
        Injector injector = Guice.createInjector(new UnitransModule(commandlineConfiguration));

        application = injector.getInstance(Application.class);
        application.startServer();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        application.stopServer();
    }


    @Test
    public void shouldBeConnectable() throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", commandlineConfiguration.getServerPort()));
            MatcherAssert.assertThat(socket.isConnected(), CoreMatchers.is(true));
            socket.close();
        }
    }
}
