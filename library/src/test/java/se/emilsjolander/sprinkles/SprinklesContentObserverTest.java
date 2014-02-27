package se.emilsjolander.sprinkles;

import android.accounts.Account;
import android.database.ContentObserver;
import android.net.Uri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SprinklesContentObserverTest {

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
        ContentObserver observer = mock(ContentObserver.class);
        SprinklesContentObserver sprinklesContentObserver = new SprinklesContentObserver(observer);
        assertEquals(sprinklesContentObserver.observer, observer);
        sprinklesContentObserver.onChange(true);
        verify(observer).onChange(true);
    }

    @Test
    public void testOnChangeWithCustomObserver() {
        ContentObserver observer = mock(ContentObserver.class);
        SprinklesContentObserver sprinklesContentObserver = new SprinklesContentObserver(observer);

        sprinklesContentObserver.onChange(true);
        verify(observer).onChange(true);

        reset(observer);
        sprinklesContentObserver.onChange(true, Uri.EMPTY);
        verify(observer).onChange(true);

        reset(observer);
        sprinklesContentObserver.onChange(false, Uri.EMPTY);
        verify(observer).onChange(false);
    }
}
