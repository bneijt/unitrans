package nl.bneijt.unitrans.session;

import com.google.inject.Singleton;
import nl.bneijt.unitrans.session.elements.Session;
import org.eclipse.jetty.server.session.JDBCSessionManager;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**In memory session listing
 *
 */
@Singleton
public class SessionService {

    private ConcurrentHashMap<String, Session> sessions;

    public Optional<Session> get(UUID sessionId) {
        String sessionIdString = sessionId.toString();
        if(sessions.containsKey(sessionIdString)) {
            return Optional.of(sessions.get(sessionIdString));
        }
        return Optional.empty();
    }

    /** Open a session for user at the given root
     *
     * @param username
     * @param rootBlock
     * @return A session object
     */
    public Session open(String username, UUID rootBlock) {
        Session session = new Session(UUID.randomUUID(), username, rootBlock);
        sessions.put(session.ident.toString(), session);
        return session;
    }

    public Session reRoot(Session current, UUID rootBlock) {
        return open(current.username, rootBlock);
    }
}
