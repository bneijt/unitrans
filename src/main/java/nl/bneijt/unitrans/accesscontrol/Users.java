package nl.bneijt.unitrans.accesscontrol;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


@Singleton
public class Users {
    List<User> users = new ArrayList<>();
    Logger logger = LoggerFactory.getLogger(Users.class);

    public boolean isKnown(User user) {
        if(users.size() == 0) {
            addUser(user);
        }
        logger.debug("Current user count: {}, isKnown for {}", users.size(), user);
        return users.contains(user);
    }

    public void addUser(User user) {
        logger.info("Adding user {}", user);
        users.add(user);
    }
}
