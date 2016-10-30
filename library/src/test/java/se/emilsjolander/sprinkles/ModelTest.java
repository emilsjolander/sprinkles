package se.emilsjolander.sprinkles;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.os.Handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import se.emilsjolander.sprinkles.model.TestModel;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ModelTest {

    private Sprinkles sprinkles;

    @Before
    public void initTables() {
        sprinkles = Sprinkles.init(Robolectric.application,"sprinkle.db",1);
    }


    @Test
    public void afterDelete() {
        TestModel m = new TestModel();
        m.title = "hej";

        sprinkles.save(m);
        assertTrue(sprinkles.exists(m));

        sprinkles.delete(m);
        assertFalse(sprinkles.exists(m));

    }

    @Test
    public void exists() {
        TestModel m = new TestModel();
        m.title = "hej";

        assertFalse(sprinkles.exists(m));
        sprinkles.save(m);
        assertTrue(sprinkles.exists(m));
    }

    @Test
    public void save() {
        TestModel m = new TestModel();
        m.title = "hej";

        sprinkles.save(m);
        assertTrue(sprinkles.exists(m));
    }

    @Test
    public void setAutoIncrementKeyOnCreate() {
        TestModel m = new TestModel();
        m.title = "hej";
        sprinkles.save(m);
        assertFalse(m.id == 0);
    }

    @Test
    public void saveWithNullField() {
        TestModel m = new TestModel();
        m.title = "hej";
        assertTrue(sprinkles.save(m));
        assertEquals(Query.one(sprinkles, TestModel.class, "select * from Tests").get().title, "hej");
        m.title = null;
        assertTrue(sprinkles.save(m));
        assertEquals(Query.one(sprinkles, TestModel.class, "select * from Tests").get().title, null);
    }

    @Test
    public void delete() {
        TestModel m = new TestModel();
        m.title = "hej";

        sprinkles.save(m);
        sprinkles.delete(m);
        assertFalse(sprinkles.exists(m));
    }

    @Test
    public void notifyContentChangeOnSave() {
        TestModel m = new TestModel();
        m.title = "hej";
        final boolean[] notified = new boolean[1];
        sprinkles.mContext.getContentResolver().
                registerContentObserver(Utils.getNotificationUri(TestModel.class), false, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        notified[0] = true;
                    }
                });
        sprinkles.save(m);
        assertTrue(notified[0]);
    }

    @Test
    public void notifyContentChangeOnDelete() {
        TestModel m = new TestModel();
        m.title = "hej";
        sprinkles.save(m);
        final boolean[] notified = new boolean[1];
        sprinkles.mContext.getContentResolver().
                registerContentObserver(Utils.getNotificationUri(TestModel.class), false, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        notified[0] = true;
                    }
                });
        assertFalse(notified[0]);
        sprinkles.delete(m);
        assertTrue(notified[0]);
    }

    @Test
    public void updateEntityCache() {
        TestModel originalModel = new TestModel();
        originalModel.title = "hej";
        sprinkles.save(originalModel);
        TestModel newModel = new TestModel();
        newModel.id = originalModel.id;
        newModel.title = "new hej";
        sprinkles.save(newModel);
        assertEquals("new hej", originalModel.title);
    }

    @Test
    public void olderModel() {
        TestModel originalModel = new TestModel();
        originalModel.title = "hej";
        assertNotSame(sprinkles.getOlderModel(originalModel), originalModel);
        sprinkles.save(originalModel);
        assertEquals(sprinkles.getOlderModel(originalModel), originalModel);
    }

}
