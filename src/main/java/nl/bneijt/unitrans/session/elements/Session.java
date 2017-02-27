package nl.bneijt.unitrans.session.elements;

import org.joda.time.DateTime;

import java.util.UUID;

public class Session {
    public final UUID ident;
    public final String username;
    public final UUID rootBlock;

    public Session(UUID ident, String username, UUID rootBlock) {
        this.ident = ident;
        this.username = username;
        this.rootBlock = rootBlock;
    }

    //TODO Timeout and remote IP management
    // DateTime started;
    // String remoteHostAddress

}
