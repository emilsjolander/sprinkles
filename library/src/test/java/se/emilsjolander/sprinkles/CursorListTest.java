package se.emilsjolander.sprinkles;

import android.database.MatrixCursor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CursorListTest {

    private CursorList<TestModel> list;

    @Before
    public void initList() {
        MatrixCursor c = new MatrixCursor(new String[]{"title", "id"});
        c.addRow(new Object[]{"title1", 1});
        c.addRow(new Object[]{"title2", 2});
        c.addRow(new Object[]{"title3", 3});
        list = new CursorList<TestModel>(c, TestModel.class);
    }

    @After
    public void closeList() {
        list.close();
    }

    @Test
    public void size() {
        assertEquals(list.size(), 3);
    }

    @Test
    public void get() {
        assertEquals(list.get(1).title, "title2");
    }

    @Test
    public void asList() {
        List<TestModel> list = this.list.asList();
        assertEquals(list.get(1).title, this.list.get(1).title);
    }

    @Test
    public void iterator() {
        assertTrue(list.iterator() instanceof CursorIterator);
    }

    @Test(expected = IllegalStateException.class)
    public void close() {
        list.close();
        list.get(0);
    }

}
