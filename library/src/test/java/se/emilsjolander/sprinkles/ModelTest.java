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

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ModelTest {

    @Table("Tests")
    public static class TestModel extends Model {

        @AutoIncrementPrimaryKey
        @Column("id") private long id;

        @Column("title")
        private String title;

        @Column("created_at")
        private Date createdAt;

        private boolean valid = true;
        public boolean created;
        public boolean saved;
        public boolean deleted;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void beforeCreate() {
            createdAt = new Date();
            created = true;
        }

        @Override
        public void beforeSave() {
            saved = true;
        }

        @Override
        public void afterDelete() {
            deleted = true;
        }

    }

    @Before
    public void initTables() {
        Sprinkles.dropInstances();
        Sprinkles sprinkles = Sprinkles.init(Robolectric.application);
        sprinkles.addMigration(new Migration().createTable(TestModel.class));
    }

    @Test
    public void isValid() {
        TestModel m = new TestModel();
        m.setTitle("hej");

        m.setValid(false);
        assertFalse(m.save());

        m.setValid(true);
        assertTrue(m.save());
    }

    @Test
    public void beforeCreate() {
        TestModel m = new TestModel();
        m.setTitle("hej");
        m.save();

        ContentValues contentValues = Utils.getContentValues(m);
        assertEquals(2, contentValues.size());
        assertNotNull(contentValues.get("created_at"));

        assertTrue(m.created);
        m.created = false;

        m.setTitle("tjena");
        m.save();
        assertFalse(m.created);
    }

    @Test
    public void beforeSave() {
        TestModel m = new TestModel();
        m.setTitle("hej");

        m.save();
        assertTrue(m.saved);
        m.saved = false;

        m.setTitle("tjena");
        m.save();
        assertTrue(m.saved);
    }

    @Test
    public void afterDelete() {
        TestModel m = new TestModel();
        m.setTitle("hej");

        m.save();
        assertFalse(m.deleted);

        m.delete();
        assertTrue(m.deleted);
    }

    @Test
    public void exists() {
        TestModel m = new TestModel();
        m.setTitle("hej");

        assertFalse(m.exists());
        m.save();
        assertTrue(m.exists());
    }

    @Test
    public void save() {
        TestModel m = new TestModel();
        m.setTitle("hej");

        m.save();
        assertTrue(m.exists());
    }

    @Test
    public void setAutoIncrementKeyOnCreate() {
        TestModel m = new TestModel();
        m.setTitle("hej");
        m.save();
        assertFalse(m.id == 0);
    }

    @Test
    public void saveWithNullField() {
        TestModel m = new TestModel();
        m.setTitle("hej");
        assertTrue(m.save());
        assertEquals(Query.one(TestModel.class, "select * from Tests").get().getTitle(), "hej");
        m.setTitle(null);
        assertTrue(m.save());
        assertEquals(Query.one(TestModel.class, "select * from Tests").get().getTitle(), null);
    }

    @Test
    public void saveAsync() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        TestModel m = new TestModel();
        m.setTitle("hej");
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
        m.setTitle("hej");

        m.save();
        m.delete();
        assertFalse(m.exists());
    }

    @Test
    public void deleteAsync() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        TestModel m = new TestModel();
        m.setTitle("hej");
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
        m.setTitle("hej");
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
        m.setTitle("hej");
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
