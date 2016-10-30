package com.lsjwzh.orm;


import android.database.MatrixCursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.lsjwzh.orm.model.TestModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CursorIteratorTest {

    private CursorIterator<TestModel> iterator;
    private Sprinkles sprinkles;

    @Before
    public void createIterator() {
        sprinkles = Sprinkles.init(Robolectric.application);
        MatrixCursor c = new MatrixCursor(new String[]{"title", "id"});
        c.addRow(new Object[]{"title1", 1});
        c.addRow(new Object[]{"title2", 2});
        c.addRow(new Object[]{"title3", 3});
        iterator = new CursorIterator<>(sprinkles, c, TestModel.class);
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
        assertEquals(model.title, "title2");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        // move to first and remove
        iterator.next();
        iterator.remove();
    }

}
