package se.emilsjolander.sprinkles;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import se.emilsjolander.sprinkles.models.CallbackTestModel;

import static junit.framework.Assert.*;

@RunWith(RobolectricGradleTestRunner.class)
public class ModelTest {

    @Before
    public void initTables() {
        Sprinkles.dropInstances();
        Sprinkles sprinkles = Sprinkles.getInstance(Robolectric.application);
        sprinkles.addMigration(new Migration().createTable(CallbackTestModel.class));
    }

    @Test
    public void isValid() {
        CallbackTestModel m = new CallbackTestModel();

        m.setValid(false);
        assertFalse(m.save());

        m.setValid(true);
        assertTrue(m.save());
    }

    @Test
    public void beforeCreate() {
        CallbackTestModel m = new CallbackTestModel();
        m.save();
        assertTrue(m.created);

        m = new CallbackTestModel();
        m.save();
        assertFalse(m.created);
    }

    @Test
    public void beforeSave() {
        CallbackTestModel m = new CallbackTestModel();
        m.save();
        assertTrue(m.saved);

        m = new CallbackTestModel();
        m.save();
        assertTrue(m.saved);
    }

    @Test
    public void afterDelete() {
        CallbackTestModel m = new CallbackTestModel();
        m.save();
        assertFalse(m.deleted);

        m.delete();
        assertTrue(m.deleted);
    }

    @Test
    public void exists() {
        CallbackTestModel m = new CallbackTestModel();
        assertFalse(m.exists());
        m.save();
        assertTrue(m.exists());
    }

    @Test
    public void save() {
        CallbackTestModel m = new CallbackTestModel();
        m.save();
        assertTrue(m.exists());
    }

    @Test
    public void saveAsync() {
        assertTrue(false);
    }

    @Test
    public void delete() {
        CallbackTestModel m = new CallbackTestModel();
        m.save();
        m.delete();
        assertFalse(m.exists());
    }

    @Test
    public void deleteAsync() {
        assertTrue(false);
    }

}
