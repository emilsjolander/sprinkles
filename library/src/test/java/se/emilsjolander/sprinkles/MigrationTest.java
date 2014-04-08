package se.emilsjolander.sprinkles;

import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertTrue;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class MigrationTest {

    class TestMigration extends Migration {

        boolean onPreMigrateCalled;
        boolean doMigrationCalled;
        boolean onPostMigrateCalled;

        @Override
        protected void onPreMigrate() {
            onPreMigrateCalled = true;
            if (doMigrationCalled || onPostMigrateCalled) {
                throw new IllegalStateException();
            }
        }

        @Override
        protected void doMigration(SQLiteDatabase db) {
            doMigrationCalled = true;
            if (!onPreMigrateCalled || onPostMigrateCalled) {
                throw new IllegalStateException();
            }
        }

        @Override
        protected void onPostMigrate() {
            onPostMigrateCalled = true;
            if (!onPreMigrateCalled || !doMigrationCalled) {
                throw new IllegalStateException();
            }
        }
    }

    @Test
    public void callbacks() {
        TestMigration m = new TestMigration();
        m.execute(null);
        assertTrue(m.onPreMigrateCalled);
        assertTrue(m.doMigrationCalled);
        assertTrue(m.onPostMigrateCalled);
    }

}
