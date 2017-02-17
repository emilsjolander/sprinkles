package com.lsjwzh.orm;

import android.app.Activity;

import com.google.gson.Gson;
import com.lsjwzh.orm.model.TestModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class QueryTest {

    private Sprinkles sprinkles;

    public static class TestActivity extends Activity {
    }

    @Before
    public void initTables() {
        sprinkles = Sprinkles.init(Robolectric.application);//.addMigration(TestModel.MIGRATION);
    }

    @Test
    public void one() {
        TestModel t = new TestModel();
        t.title = "title";
        sprinkles.save(t);
        TestModel result = Query.one(sprinkles, TestModel.class, "select * from Tests where title=?", "title").get();
        assertEquals(result.title, "title");
    }

    @Test
    public void many() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            sprinkles.save(t);
        }
        CursorList<TestModel> result = Query.many(sprinkles, TestModel.class, "select * from Tests where title like 'title%'", "title").get();
        assertEquals(result.size(), 10);
        result.close();
    }

    @Test
    public void all() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            sprinkles.save(t);
        }
        CursorList<TestModel> result = Query.all(sprinkles, TestModel.class).get();
        assertEquals(result.size(), 10);
        result.close();
    }

    @Test
    public void dynamicColumnResult() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            sprinkles.save(t);
        }

        TestModel m = Query.one(sprinkles, TestModel.class, "select *, count(*) AS count from Tests limit 1").get();
//        assertEquals(m.count, 10);
    }

    @Test
    public void findSingle() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            t.sn = i;
            sprinkles.save(t);
        }
        TestModel result = new Query(sprinkles)
                .findSingle(QueryBuilder.from(TestModel.class)
                        .where()
                        .equalTo("sn", 1)
                        .end());
        assertEquals(1, result.sn);
    }

    @Test
    public void equalTo() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            t.sn = i;
            sprinkles.save(t);
        }
        ModelList<TestModel> queryForEqualTo = new Query(sprinkles)
                .find(QueryBuilder.from(TestModel.class)
                        .where()
                        .equalTo("sn", 1)
                        .end());
        assertEquals(1, queryForEqualTo.size());
    }

    @Test
    public void notEqualTo() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            t.sn = i;
            sprinkles.save(t);
        }

        ModelList<TestModel> queryForNotEqualTo = new Query(sprinkles).find(
                QueryBuilder.from(TestModel.class)
                        .where()
                        .notEqualTo("sn", 1)
                        .end());
        assertEquals(9, queryForNotEqualTo.size());


    }

    @Test
    public void greaterThan() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            t.sn = i;
            sprinkles.save(t);
        }

        ModelList<TestModel> queryForGreaterThan = new Query(sprinkles).find(
                QueryBuilder.from(TestModel.class)
                        .where()
                        .greaterThan("sn", 1)
                        .end());
        assertEquals(8, queryForGreaterThan.size());
    }

    @Test
    public void greaterThanOrEqualTo() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            t.sn = i;
            sprinkles.save(t);
        }

        ModelList<TestModel> query = new Query(sprinkles).find(
                QueryBuilder.from(TestModel.class)
                        .where()
                        .greaterThanOrEqualTo("sn", 1)
                        .end());
        assertEquals(9, query.size());
    }

    @Test
    public void lessThan() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            t.sn = i;
            sprinkles.save(t);
        }

        ModelList<TestModel> query = new Query(sprinkles).find(
                QueryBuilder.from(TestModel.class)
                        .where()
                        .lessThan("sn", 1)
                        .end());
        assertEquals(1, query.size());
    }

    @Test
    public void lessThanOrEqualTo() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            t.sn = i;
            sprinkles.save(t);
        }

        ModelList<TestModel> query = new Query(sprinkles).find(
                QueryBuilder.from(TestModel.class)
                        .where()
                        .lessThanOrEqualTo("sn", 1)
                        .end());
        assertEquals(2, query.size());
    }

    @Test
    public void take() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            t.sn = i;
            sprinkles.save(t);
        }

        ModelList<TestModel> query = new Query(sprinkles).find(
                QueryBuilder.from(TestModel.class)
                        .where()
                        .lessThanOrEqualTo("sn", 10)
                        .take(2)
                        .end());
        assertEquals(2, query.size());
    }

    @Test
    public void skip() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            t.sn = i;
            sprinkles.save(t);
        }

        ModelList<TestModel> query = new Query(sprinkles).find(
                QueryBuilder.from(TestModel.class)
                        .where()
                        .lessThanOrEqualTo("sn", 10)
                        .skip(8)
                        .end());
        assertEquals(2, query.size());
        assertEquals(8, query.get(0).sn);

    }

    @Test
    public void like() {
        for (int i = 0; i < 11; i++) {
            TestModel t = new TestModel();
            t.title = "title" + i;
            t.sn = i;
            sprinkles.save(t);
        }

        ModelList<TestModel> query = new Query(sprinkles).find(
                QueryBuilder.from(TestModel.class)
                        .where()
                        .like("title", "title1%")
                        .end());
        assertEquals(2, query.size());

    }

    @Test
    public void multiConditionQuery() {
        for (int i = 0; i < 10; i++) {
            TestModel t = new TestModel();
            t.title = i + "title" + i;
            t.sn = i;
            sprinkles.save(t);
        }

        ModelList<TestModel> query = new Query(sprinkles).find(
                QueryBuilder.from(TestModel.class)
                        .where()
                        .lessThanOrEqualTo("sn", 10)
                        .and()
                        .like("title", "1%")
                        .end());
        assertEquals(1, query.size());
        assertEquals("1title1", query.get(0).title);
    }

}
