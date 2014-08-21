package nl.bneijt.unitrans.accesscontrol;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class UserTest {
    @Test
    public void sameHashShouldBeSameUser() throws Exception {
        User a = new User(new byte[]{0});
        User b = new User(new byte[]{0});
        MatcherAssert.assertThat(a.compareTo(b), Is.is(0));
        MatcherAssert.assertThat(a.equals(b), Is.is(true));
    }
    @Test
    public void differentHashShouldBeDifferentUser() throws Exception {
        User a = new User(new byte[]{1});
        User b = new User(new byte[]{0});
        MatcherAssert.assertThat(a.compareTo(b), Is.is(1));
        MatcherAssert.assertThat(a.equals(b), Is.is(false));
    }
}
