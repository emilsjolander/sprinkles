package se.emilsjolander.sprinkles;

import android.accounts.Account;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;

import static org.junit.Assert.assertEquals;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SprinklesContentObserverTest {

    public static final String AUTHORITY = "AUTHORITY";

    Account account;

    @Table("Tests")
    public static class TestModel extends Model {

        @AutoIncrementPrimaryKey
        @Column("id") private long id;

    }

    @Before
    public void init() {
        account = new Account("name", "type");
    }

    @Test
    public void testCreateContentObserver() {
        SprinklesContentObserver observer = new SprinklesContentObserver(account, AUTHORITY);
        assertEquals(account, observer.mAccount);
        assertEquals(AUTHORITY, observer.mAuthority);
    }
}
