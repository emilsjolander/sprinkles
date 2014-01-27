package se.emilsjolander.sprinkles;

import android.database.MatrixCursor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import se.emilsjolander.sprinkles.models.TestModel;

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
        assertTrue(list.size() == 3);
    }

    @Test
    public void get() {
        assertTrue(list.get(1).getTitle().equals("title2"));
    }

    @Test
    public void asList() {
        List<TestModel> list = this.list.asList();
        assertTrue(list.get(1).getTitle().equals(this.list.get(1).getTitle()));
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
