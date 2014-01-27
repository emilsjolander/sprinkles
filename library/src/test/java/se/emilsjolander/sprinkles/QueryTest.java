package se.emilsjolander.sprinkles;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;
import se.emilsjolander.sprinkles.annotations.Unique;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class QueryTest {

    @Table("Tests")
    public static class TestModel extends Model {

        @AutoIncrementPrimaryKey
        @Column("id") private long id;

        @Column("title")
        private String title;

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    @Before
    public void initTables() {
        Sprinkles.dropInstances();
        Sprinkles.getInstance(Robolectric.application).addMigration(new Migration().createTable(TestModel.class));
    }

    @Test
    public void one() {
        TestModel t = new TestModel();
        t.setTitle("title");
        t.save();
        TestModel result = Query.one(TestModel.class, "select * from Tests where title=?", "title").get();
        assertEquals(result.getTitle(), "title");
    }

    @Test
    public void many() {
        for (int i = 0 ; i<10 ; i++) {
            TestModel t = new TestModel();
            t.setTitle("title"+i);
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
            t.setTitle("title"+i);
            t.save();
        }
        CursorList<TestModel> result = Query.all(TestModel.class).get();
        assertEquals(result.size(), 10);
        result.close();
    }

    @Test
    public void async() {
        assertTrue(false);
    }

    @Test
    public void updates() {
        assertTrue(false);
    }

    @Test
    public void noUpdates() {
        assertTrue(false);
    }

    @Test
    public void dynamicColumnResult() {
        assertTrue(false);
    }

}
