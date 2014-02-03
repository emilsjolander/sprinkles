package se.emilsjolander.sprinkles;


import android.database.MatrixCursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;

import static org.junit.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CursorIteratorTest {

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

    private CursorIterator<TestModel> iterator;

    @Before
    public void initSprinkles() {
        Sprinkles.getInstance(Robolectric.application);
    }

    @Before
    public void createIterator() {
        MatrixCursor c = new MatrixCursor(new String[]{"title", "id"});
        c.addRow(new Object[]{"title1", 1});
        c.addRow(new Object[]{"title2", 2});
        c.addRow(new Object[]{"title3", 3});
        iterator = new CursorIterator<TestModel>(c, TestModel.class);
    }

    @Test
    public void hasNext() {
        iterator.next();
        iterator.next();
        assertTrue(iterator.hasNext());
        iterator.next();
        assertFalse(iterator.hasNext());
    }

    @Test
    public void next() {
        iterator.next();
        TestModel model = iterator.next();
        assertEquals(model.getTitle(), "title2");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        // move to first and remove
        iterator.next();
        iterator.remove();
    }

}
