package com.lsjwzh.orm;

import android.database.MatrixCursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.lsjwzh.orm.model.TestModel;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ModelListTest {

    private Sprinkles sprinkles;

    @Before
    public void initTables() {
        sprinkles = Sprinkles.init(Robolectric.application);
    }

    @Test
    public void fromCursorList() {
        MatrixCursor c = new MatrixCursor(new String[]{"title", "id"});
        c.addRow(new Object[]{"title1", 1});
        c.addRow(new Object[]{"title2", 2});
        c.addRow(new Object[]{"title3", 3});
        CursorList<TestModel> cursorList = new CursorList<TestModel>(sprinkles, c, TestModel.class);

        ModelList<TestModel> modelList = ModelList.from(cursorList);
        assertEquals(3, modelList.size());
        assertEquals("title1", modelList.get(0).title);
        assertEquals("title2", modelList.get(1).title);
        assertEquals("title3", modelList.get(2).title);
        cursorList.close();
    }

    @Test
    public void saveAllModels() {
        TestModel m1 = new TestModel();
        m1.title = "foo";
        TestModel m2 = new TestModel();
        m2.title = "bar";

        ModelList<Model> modelList = new ModelList<>();
        modelList.add(m1);
        modelList.add(m2);

        assertTrue(sprinkles.saveAll(modelList));
        assertTrue(sprinkles.exists(m1));
        assertTrue(sprinkles.exists(m2));
    }


    @Test
    public void deleteAllModels() {
        TestModel m1 = new TestModel();
        m1.title = "foo";
        TestModel m2 = new TestModel();
        m2.title = "bar";

        ModelList<Model> modelList = new ModelList<>();
        modelList.add(m1);
        modelList.add(m2);

        sprinkles.saveAll(modelList);
        sprinkles.deleteAll(modelList);

        assertFalse(sprinkles.exists(m1));
        assertFalse(sprinkles.exists(m2));
    }
}
