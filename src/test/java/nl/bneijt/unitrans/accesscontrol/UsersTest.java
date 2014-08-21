package nl.bneijt.unitrans.accesscontrol;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class UsersTest {
    @Test
    public void addingAUserShouldMakeItKnown() {
        Users users = new Users();
        users.addUser(new User(new byte[]{0}));

        assertThat(users.isKnown(new User(new byte[]{0})), is(true));
    }
}
