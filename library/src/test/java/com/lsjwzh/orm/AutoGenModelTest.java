package com.lsjwzh.orm;

import android.database.ContentObserver;
import android.os.Handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.lsjwzh.orm.model.AutoGenTestModel;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class AutoGenModelTest {

    private Sprinkles sprinkles;

    @Before
    public void initTables() {
        sprinkles = Sprinkles.init(Robolectric.application);
    }

    @Test
    public void exists() {
        AutoGenTestModel m = new AutoGenTestModel();
        m.title = "hej";

        assertFalse(sprinkles.exists(m));
        sprinkles.save(m);
        assertTrue(sprinkles.exists(m));
    }

    @Test
    public void save() {
        AutoGenTestModel m = new AutoGenTestModel();
        m.title = "hej";

        sprinkles.save(m);
        assertTrue(sprinkles.exists(m));
    }

    @Test
    public void setAutoIncrementKeyOnCreate() {
        AutoGenTestModel m = new AutoGenTestModel();
        m.title = "hej";
        sprinkles.save(m);
        assertFalse(m.id == 0);
    }

    @Test
    public void saveWithNullField() {
        AutoGenTestModel m = new AutoGenTestModel();
        m.title = "hej";
        assertTrue(sprinkles.save(m));
        assertEquals(Query.one(sprinkles, AutoGenTestModel.class, "select * from " + DataResolver.getTableName(AutoGenTestModel.class)).get().title, "hej");
        m.title = null;
        assertTrue(sprinkles.save(m));
        assertEquals(Query.one(sprinkles, AutoGenTestModel.class, "select * from " + DataResolver.getTableName(AutoGenTestModel.class)).get().title, null);
    }

    @Test
    public void delete() {
        AutoGenTestModel m = new AutoGenTestModel();
        m.title = "hej";

        sprinkles.save(m);
        sprinkles.delete(m);
        assertFalse(sprinkles.exists(m));
    }

    @Test
    public void notifyContentChangeOnSave() {
        AutoGenTestModel m = new AutoGenTestModel();
        m.title = "hej";
        final boolean[] notified = new boolean[1];
        sprinkles.mContext.getContentResolver().
                registerContentObserver(Utils.getNotificationUri(AutoGenTestModel.class), false, new ContentObserver(new Handler()) {
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
        AutoGenTestModel m = new AutoGenTestModel();
        m.title = "hej";
        sprinkles.save(m);
        final boolean[] notified = new boolean[1];
        sprinkles.mContext.getContentResolver().
                registerContentObserver(Utils.getNotificationUri(AutoGenTestModel.class), false, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        notified[0] = true;
                    }
                });
        assertFalse(notified[0]);
        sprinkles.delete(m);
        assertTrue(notified[0]);
    }

}
