package se.emilsjolander.sprinkles;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.model.TestModel;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class QueryTest {

    public static class TestActivity extends Activity{}

    @Before
    public void initTables() {
        Sprinkles.dropInstances();
        Sprinkles.init(Robolectric.application);//.addMigration(TestModel.MIGRATION);
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

    @Test
    public void findSingle() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.sn = i;
            t.save();
        }
        TestModel result = Query.Where(TestModel.class)
                .equalTo("sn",1)
                .findSingle();
        assertEquals(1, result.sn);
    }

    @Test
    public void equalTo() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.sn = i;
            t.save();
        }
        ModelList<TestModel> queryForEqualTo = Query.Where(TestModel.class)
                .equalTo("sn",1)
                .find();
        assertEquals(1, queryForEqualTo.size());
    }
    @Test
    public void notEqualTo() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.sn = i;
            t.save();
        }

        ModelList<TestModel> queryForNotEqualTo = Query.Where(TestModel.class)
                .notEqualTo("sn",1)
                .find();
        assertEquals(9, queryForNotEqualTo.size());


    }
    @Test
    public void greaterThan() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.sn = i;
            t.save();
        }

        ModelList<TestModel> queryForGreaterThan = Query.Where(TestModel.class)
                .greaterThan("sn",1)
                .find();
        assertEquals(8, queryForGreaterThan.size());
    }
    @Test
    public void greaterThanOrEqualTo() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.sn = i;
            t.save();
        }

        ModelList<TestModel> query = Query.Where(TestModel.class)
                .greaterThanOrEqualTo("sn", 1)
                .find();
        assertEquals(9, query.size());
    }
    @Test
    public void lessThan() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.sn = i;
            t.save();
        }

        ModelList<TestModel> query = Query.Where(TestModel.class)
                .lessThan("sn", 1)
                .find();
        assertEquals(1, query.size());
    }
    @Test
    public void lessThanOrEqualTo() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.sn = i;
            t.save();
        }

        ModelList<TestModel> query = Query.Where(TestModel.class)
                .lessThanOrEqualTo("sn", 1)
                .find();
        assertEquals(2, query.size());
    }

    @Test
    public void take() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.sn = i;
            t.save();
        }

        ModelList<TestModel> query = Query.Where(TestModel.class)
                .lessThanOrEqualTo("sn",10)
                .take(2)
                .find();
        assertEquals(2, query.size());
    }

    @Test
    public void skip() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.sn = i;
            t.save();
        }

        ModelList<TestModel> query = Query.Where(TestModel.class)
                .lessThanOrEqualTo("sn", 10)
                .skip(8)
                .find();
        assertEquals(2, query.size());
        assertEquals(8, query.get(0).sn);

    }

    @Test
    public void like() {
        for (int i = 0 ; i<11 ; i++) {
            TestModel t = new TestModel();
            t.title = "title"+i;
            t.sn = i;
            t.save();
        }

        ModelList<TestModel> query = Query.Where(TestModel.class)
                .like("title", "title1%")
                .find();
        assertEquals(2, query.size());

    }

    @Test
    public void multiConditionQuery() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.title = i+"title"+i;
            t.sn = i;
            t.save();
        }

        ModelList<TestModel> query = Query.Where(TestModel.class)
                .lessThanOrEqualTo("sn",10)
                .and()
                .like("title","1%")
                .find();
        assertEquals(1, query.size());
        assertEquals("1title1", query.get(0).title);
    }

}
