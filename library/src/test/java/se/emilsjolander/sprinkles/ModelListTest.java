package se.emilsjolander.sprinkles;

import android.database.MatrixCursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class    ModelListTest {

    @Table("Tests")
    public static class TestModel extends Model {

        @AutoIncrementPrimaryKey
        @Column("id") private long id;

        @Column("title") private String title;

        public long getId() {
            return id;
        }

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
        Sprinkles sprinkles = Sprinkles.init(Robolectric.application);
        sprinkles.addMigration(new Migration().createTable(TestModel.class));
    }

    @Test
    public void fromCursorList() {
        MatrixCursor c = new MatrixCursor(new String[]{"title", "id"});
        c.addRow(new Object[]{"title1", 1});
        c.addRow(new Object[]{"title2", 2});
        c.addRow(new Object[]{"title3", 3});
        CursorList<TestModel> cursorList = new CursorList<TestModel>(c, TestModel.class);

        ModelList<TestModel> modelList = ModelList.from(cursorList);
        assertEquals(3, modelList.size());
        assertEquals("title1", modelList.get(0).getTitle());
        assertEquals("title2", modelList.get(1).getTitle());
        assertEquals("title3", modelList.get(2).getTitle());
    }

    @Test
    public void saveAllModels() {
        TestModel m1 = new TestModel();
        m1.setTitle("foo");
        TestModel m2 = new TestModel();
        m2.setTitle("bar");

        ModelList<Model> modelList = new ModelList<Model>();
        modelList.add(m1);
        modelList.add(m2);

        assertTrue(modelList.saveAll());
        assertTrue(m1.exists());
        assertTrue(m2.exists());
    }

    @Test
    public void saveAllModelsAsync() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        TestModel m1 = new TestModel();
        m1.setTitle("foo");
        TestModel m2 = new TestModel();
        m2.setTitle("bar");

        ModelList<Model> modelList = new ModelList<Model>();
        modelList.add(m1);
        modelList.add(m2);

        modelList.saveAllAsync(new ModelList.OnAllSavedCallback() {
            @Override public void onAllSaved() {
                latch.countDown();
            }
        });

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        assertTrue(m1.exists());
        assertTrue(m2.exists());
    }

    @Test
    public void deleteAllModels() {
        TestModel m1 = new TestModel();
        m1.setTitle("foo");
        TestModel m2 = new TestModel();
        m2.setTitle("bar");

        ModelList<Model> modelList = new ModelList<Model>();
        modelList.add(m1);
        modelList.add(m2);

        modelList.saveAll();
        modelList.deleteAll();

        assertFalse(m1.exists());
        assertFalse(m2.exists());
    }

    @Test
    public void deleteAllModelsAsync() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        TestModel m1 = new TestModel();
        m1.setTitle("foo");
        TestModel m2 = new TestModel();
        m2.setTitle("bar");

        ModelList<Model> modelList = new ModelList<Model>();
        modelList.add(m1);
        modelList.add(m2);

        modelList.saveAll();
        modelList.deleteAllAsync(new ModelList.OnAllDeletedCallback() {
            @Override public void onAllDeleted() {
                latch.countDown();
            }
        });

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        assertFalse(m1.exists());
        assertFalse(m2.exists());
    }
}
