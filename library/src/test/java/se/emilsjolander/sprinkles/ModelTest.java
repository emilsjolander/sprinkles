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

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ModelTest {

    @Before
    public void initTables() {
        Sprinkles.dropInstances();
        Sprinkles sprinkles = Sprinkles.init(Robolectric.application);
        sprinkles.addMigration(TestModel.MIGRATION);
    }

    @Test
    public void isValid() {
        TestModel m = new TestModel();
        m.title = "hej";

        m.valid = false;
        assertFalse(m.save());

        m.valid = true;
        assertTrue(m.save());
    }

    @Test
    public void beforeCreate() {
        TestModel m = new TestModel();
        m.title = "hej";
        m.save();

        ContentValues contentValues = Utils.getContentValues(m);
        assertEquals(2, contentValues.size());
        assertNotNull(contentValues.get("created_at"));

        assertTrue(m.created);
        m.created = false;

        m.title = "tjena";
        m.save();
        assertFalse(m.created);
    }

    @Test
    public void beforeSave() {
        TestModel m = new TestModel();
        m.title = "hej";

        m.save();
        assertTrue(m.saved);
        m.saved = false;

        m.title = "tjena";
        m.save();
        assertTrue(m.saved);
    }

    @Test
    public void afterDelete() {
        TestModel m = new TestModel();
        m.title = "hej";

        m.save();
        assertFalse(m.deleted);

        m.delete();
        assertTrue(m.deleted);
    }

    @Test
    public void exists() {
        TestModel m = new TestModel();
        m.title = "hej";

        assertFalse(m.exists());
        m.save();
        assertTrue(m.exists());
    }

    @Test
    public void save() {
        TestModel m = new TestModel();
        m.title = "hej";

        m.save();
        assertTrue(m.exists());
    }

    @Test
    public void setAutoIncrementKeyOnCreate() {
        TestModel m = new TestModel();
        m.title = "hej";
        m.save();
        assertFalse(m.id == 0);
    }

    @Test
    public void saveWithNullField() {
        TestModel m = new TestModel();
        m.title = "hej";
        assertTrue(m.save());
        assertEquals(Query.one(TestModel.class, "select * from Tests").get().title, "hej");
        m.title = null;
        assertTrue(m.save());
        assertEquals(Query.one(TestModel.class, "select * from Tests").get().title, null);
    }

    @Test
    public void saveAsync() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        TestModel m = new TestModel();
        m.title = "hej";
        m.saveAsync(new Model.OnSavedCallback() {
            @Override
            public void onSaved() {
                latch.countDown();
            }
        });

        assertTrue(latch.await(30, TimeUnit.SECONDS));
    }

    @Test
    public void delete() {
        TestModel m = new TestModel();
        m.title = "hej";

        m.save();
        m.delete();
        assertFalse(m.exists());
    }

    @Test
    public void deleteAsync() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        TestModel m = new TestModel();
        m.title = "hej";
        m.save();
        m.deleteAsync(new Model.OnDeletedCallback() {
            @Override
            public void onDeleted() {
                latch.countDown();
            }
        });

        assertTrue(latch.await(30, TimeUnit.SECONDS));
    }

    @Test
    public void notifyContentChangeOnSave() {
        TestModel m = new TestModel();
        m.title = "hej";
        final boolean[] notified = new boolean[1];
        Sprinkles.sInstance.mContext.getContentResolver().
                registerContentObserver(Utils.getNotificationUri(TestModel.class), false, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        notified[0] = true;
                    }
                });
        m.save();
        assertTrue(notified[0]);
    }

    @Test
    public void notifyContentChangeOnDelete() {
        TestModel m = new TestModel();
        m.title = "hej";
        m.save();
        final boolean[] notified = new boolean[1];
        Sprinkles.sInstance.mContext.getContentResolver().
                registerContentObserver(Utils.getNotificationUri(TestModel.class), false, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        notified[0] = true;
                    }
                });
        assertFalse(notified[0]);
        m.delete();
        assertTrue(notified[0]);
    }

}
