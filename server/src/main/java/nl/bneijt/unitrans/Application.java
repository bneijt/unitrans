package nl.bneijt.unitrans;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.bneijt.unitrans.filter.AccessLoggingFilter;
import nl.bneijt.unitrans.metadata.MetadataService;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Provider;
import java.security.Security;
import java.util.EnumSet;
import java.util.logging.LogManager;

public class Application {
    static final Logger logger;

    static {

        //Initialize JDK logging
        final InputStream inputStream = Application.class.getResourceAsStream("/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(inputStream);
        } catch (final IOException e) {
            System.err.println("Could not load default logging.properties file");
        }
        logger = LoggerFactory.getLogger(Application.class);
    }

    private final Injector injector;
    private final CommandlineConfiguration commandlineConfiguration;
    private Server jettyServer;
    private final MetadataService metadataService;

    @Inject
    public Application(Injector injector, CommandlineConfiguration commandlineConfiguration, MetadataService metadataService) {
        this.injector = injector;
        this.commandlineConfiguration = commandlineConfiguration;
        this.metadataService = metadataService;
    }


    public static ArgumentParser argumentParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("unitrans")
                .description("Unitrans server interface");

        parser.addArgument("--" + CommandlineConfiguration.DATA_LOCATION)
                .type(String.class)
                .setDefault(CommandlineConfiguration.DATA_LOCATION_DEFAULT)
                .help("The location of the data storage");

        parser.addArgument("--" + CommandlineConfiguration.META_LOCATION)
                .type(String.class)
                .setDefault(CommandlineConfiguration.META_LOCATION_DEFAULT)
                .help("The location of the metadata storage");

        parser.addArgument("--" + CommandlineConfiguration.SERVER_PORT)
                .type(Integer.class)
                .setDefault(CommandlineConfiguration.SERVER_PORT_DEFAULT)
                .help("The port to host the server on");

        parser.addArgument("--" + CommandlineConfiguration.WEB_RESOURCES_LOCATION)
                .type(String.class)
                .setDefault(CommandlineConfiguration.WEB_RESOURCES_LOCATION_DEFAULT)
                .help("Location of web resources to load. When this location does not exist, the internal webapp will be used.");

        return parser;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ArgumentParser parser = argumentParser();

        try {
            Namespace res = parser.parseArgs(args);
            Injector injector = Guice.createInjector(new UnitransModule(new CommandlineConfiguration(res)));

            Application application = injector.getInstance(Application.class);
            application.runServer();
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

    }

    public void runServer() throws IOException, InterruptedException {
        startServer();
        joinServer();
    }

    private void joinServer() throws InterruptedException {
        try {
            jettyServer.join();
        } catch (InterruptedException e) {
            logger.warn("Server was interrupted", e);
            throw e;
        }
    }

    public boolean startServer() throws IOException {
        removeProviderWith("SunPKCS11-NSS");
        ResourcesApplication resourcesApplication = injector.getInstance(ResourcesApplication.class);
        jettyServer = createJettyServer(resourcesApplication);

        try {
            jettyServer.start();
        } catch (Exception e) {
            logger.error("Jetty server could not be started", e);
            return true;
        }
        return false;
    }

    private void removeProviderWith(String substringOfName) {
        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            String providerName = provider.getName();
            if (providerName.contains(substringOfName)) {
                logger.error("Removing {} from security providers", providerName);
                Security.removeProvider(providerName);
                return;
            }
        }

    }

    public Server createJettyServer(ResourcesApplication resourcesApplication) throws IOException {
        ServletHolder jerseyServlet = new ServletHolder("jersey-servlet", new ServletContainer(resourcesApplication));

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
//        context.addFilter(X509ClientAuthenticationFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        context.addFilter(AccessLoggingFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        context.addServlet(jerseyServlet, "/api/*");
        context.addServlet(DefaultServlet.class, "/*");

        File localResources = new File(commandlineConfiguration.getWebResourcesLocation());
        if (localResources.isDirectory()) {
            logger.debug("Using resources from {}", localResources);
            context.setBaseResource(Resource.newResource(localResources));
        } else {
            context.setBaseResource(Resource.newClassPathResource("/webapp"));
        }
        context.setWelcomeFiles(new String[]{"index.html"});


        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{context});


        Server server = new Server();

        SslContextFactory sslContextFactory = newSslContextFactory(commandlineConfiguration.getKeyStoreLocation(), commandlineConfiguration.getKeyStorePassword());

        // SSL HTTP Configuration
        HttpConfiguration https_config = new HttpConfiguration();
        https_config.addCustomizer(new SecureRequestCustomizer());

        // SSL Connector
        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(https_config));
        sslConnector.setPort(commandlineConfiguration.getServerPort());
        logger.info("SSL connector port is {}", sslConnector.getPort());
        server.addConnector(sslConnector);

        server.setHandler(handlers);
        return server;

    }

    public SslContextFactory newSslContextFactory(File keystoreLocation, String password) throws IOException {

        if (!keystoreLocation.canRead()) {
            logger.warn("Could not read keystore file, trying to generate it at {}", keystoreLocation);
            CertificateBuilder.generateKeyStore(keystoreLocation, password);
        }

        SslContextFactory sslContextFactory = new SslContextFactory();

        sslContextFactory.setTrustStoreType("JKS");
        sslContextFactory.setTrustStorePath(keystoreLocation.getPath());
        sslContextFactory.setTrustStorePassword(password);

        sslContextFactory.setKeyStoreType("JKS");
        sslContextFactory.setKeyStorePath(keystoreLocation.getPath());
        sslContextFactory.setKeyStorePassword(password);
        sslContextFactory.setKeyManagerPassword(password);

        sslContextFactory.setExcludeCipherSuites(
                "SSL_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");

        return sslContextFactory;
    }

    public void stopServer() throws Exception {
        jettyServer.stop();
        metadataService.close();
    }
}