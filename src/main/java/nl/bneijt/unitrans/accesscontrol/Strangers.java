package nl.bneijt.unitrans.accesscontrol;

import com.google.common.base.Function;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class Strangers {
    EvictingQueue<User> strangers = EvictingQueue.create(10);

    public void addStranger(User user) {
        synchronized (strangers) {
            if (!strangers.contains(user)) {
                strangers.add(user);
            }
        }
    }

    public List<String> getStrangerIds() {
        synchronized (strangers) {
            List<User> users = ImmutableList.copyOf(strangers.iterator());
            return Lists.transform(users, new Function<User, String>() {
                @Nullable
                @Override
                public String apply(@Nullable User user) {
                    return user.publicDigestUrl();
                }
            });
        }
    }
}
