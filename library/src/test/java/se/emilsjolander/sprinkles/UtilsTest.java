package se.emilsjolander.sprinkles;

import android.content.ContentValues;
import android.database.MatrixCursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;

import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;
import se.emilsjolander.sprinkles.exceptions.NoTableAnnotationException;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class UtilsTest {

    public static class AbsTestModel extends Model {

        @Key
        @AutoIncrement
        @Column("id") private long id;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

    }

    @Table("Tests")
    public static class TestModel extends AbsTestModel {

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
    public void initSprinkles() {
        Sprinkles.init(Robolectric.application);
    }

    @Test
    public void getResultFromCursor() {
        MatrixCursor c = new MatrixCursor(new String[]{"title", "id"});
        c.addRow(new Object[]{"title1", 1});
        c.addRow(new Object[]{"title2", 2});
        c.addRow(new Object[]{"title3", 3});
        c.moveToPosition(2);
        TestModel m = Utils.getResultFromCursor(TestModel.class, c);
        assertEquals(m.getId(), 3);
        assertEquals(m.getTitle(), "title3");
    }

    @Test
    public void getWhereStatement() {
        TestModel m = new TestModel();
        m.setId(4);
        String where = Utils.getWhereStatement(m);
        assertEquals(where, "id=4");
    }

    @Test
    public void getContentValues() {
        TestModel m = new TestModel();
        m.setId(1);
        m.setTitle("tjena");
        ContentValues cv = Utils.getContentValues(m);
        assertFalse(cv.containsKey("id"));
        assertEquals(cv.getAsString("title"), "tjena");
    }

    @Test
    public void getNotificationUri() {
        String result = Utils.getNotificationUri(TestModel.class).toString();
        assertTrue(result.contains("Tests"));
    }

    @Test
    public void getTableName() {
        assertEquals(Utils.getTableName(TestModel.class), "Tests");
    }

    @Test(expected = NoTableAnnotationException.class)
    public void getTableNameNoAnnotation() {
        Utils.getTableName(AbsTestModel.class);
    }

    @Test
    public void insertSqlArgs() {
        String result = Utils.insertSqlArgs("? ?", new Object[]{1, "hej"});
        assertEquals(result, "1 'hej'");
    }

    @Test
    public void insertTypeSerializedSqlArgs() {
        Date date = new Date();
        String result = Utils.insertSqlArgs("?", new Object[]{date});
        assertEquals(result, ""+date.getTime());
    }

    @Test
    public void getAllDeclaredFields() {
        Field[] fields = Utils.getAllDeclaredFields(TestModel.class, Model.class);
        System.out.println(fields[0].getName());
        assertEquals(fields.length, 2);
        assertEquals(fields[0].getName(), "title");
        assertEquals(fields[1].getName(), "id");
    }

    @Test
    public void concatArrays() {
        String[] result = Utils.concatArrays(new String[]{"1", "2", "3"}, new String[]{"4", "5"});
        assertTrue(Arrays.equals(result, new String[]{"1", "2", "3", "4", "5"}));
    }

}
