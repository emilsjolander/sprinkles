package se.emilsjolander.sprinkles;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class QueryTest {

    public static class TestActivity extends Activity{}

    @Before
    public void initTables() {
        Sprinkles.dropInstances();
        Sprinkles.init(Robolectric.application).addMigration(TestModel.MIGRATION);
    }

    @Test
    public void one() {
        TestModel t = new TestModel();
        t.title = "title";
        t.save();
        TestModel result = Query.one(TestModel.class, "select * from Tests where title=?", "title").get();
        assertEquals(result.title, "title");
    }

    @Test
    public void many() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.save();
        }
        CursorList<TestModel> result = Query.many(TestModel.class, "select * from Tests where title like 'title%'", "title").get();
        assertEquals(result.size(), 10);
        result.close();
    }

    @Test
    public void all() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.save();
        }
        CursorList<TestModel> result = Query.all(TestModel.class).get();
        assertEquals(result.size(), 10);
        result.close();
    }

    @Test
    public void dynamicColumnResult() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.save();
        }

        TestModel m = Query.one(TestModel.class, "select *, count(*) AS count from Tests limit 1").get();
        assertEquals(m.count, 10);
    }

}
