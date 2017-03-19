package nl.bneijt.unitrans.client;

import com.mashape.unirest.http.Unirest;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import static org.slf4j.LoggerFactory.getLogger;

public class Application {
    static final Logger LOGGER;

    static {

        //Initialize JDK logging
        final InputStream inputStream = Application.class.getResourceAsStream("/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(inputStream);
        } catch (final IOException e) {
            System.err.println("Could not load default logging.properties file");
        }
        LOGGER = getLogger(Application.class);
    }

    private final Namespace res;

    public Application(Namespace res) {

        this.res = res;
    }

    public static ArgumentParser argumentParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("unitrans")
                .description("Unitrans server interface");

        parser.addArgument("--" + CommandlineConfiguration.SERVER)
                .type(String.class)
                .setDefault(CommandlineConfiguration.SERVER_DEFAULT)
                .help("The location of the data storage");

        parser.addArgument("--" + CommandlineConfiguration.UPLOAD)
                .type(String.class)
                .help("A file to upload");

        parser.addArgument("--" + CommandlineConfiguration.LIST)
                .type(Boolean.class)
                .help("List root at server");

        return parser;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ArgumentParser parser = argumentParser();

        try {
            Namespace res = parser.parseArgs(args);

            Application application = new Application(res);
            application.run();
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

    }

    private void run() {
        String serverEndpoint = res.getString(CommandlineConfiguration.SERVER);

        if(res.getBoolean(CommandlineConfiguration.LIST)) {

        }

        if(res.getList(CommandlineConfiguration.UPLOAD).size() > 0) {
            for (String argument : res.<String>getList(CommandlineConfiguration.UPLOAD)) {
                LOGGER.info("Should upload", argument);
                throw new RuntimeException("Not implemented yet");
            }
        }



    }

}