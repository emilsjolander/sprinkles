package se.emilsjolander.sprinkles;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.model.TestModel;

import static junit.framework.Assert.*;
import static se.emilsjolander.sprinkles.ModelInfo.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ModelInfoTest {

    private Sprinkles sprinkles;

    @Before
    public void initTables() {
        sprinkles = Sprinkles.init(Robolectric.application);
    }

    @Test
    public void columnFieldEquals() {
        ColumnField f1 = new ColumnField();
        f1.name = "a_name";
        ColumnField f2 = new ColumnField();
        f2.name = "a_name";
        assertEquals(f1, f2);
    }

    @Test
    public void columnFieldNotEquals() {
        ColumnField f1 = new ColumnField();
        f1.name = "a_name";
        ColumnField f2 = new ColumnField();
        f2.name = "another_name";
        assertFalse(f1.equals(f2));
    }

    @Test
    public void fromModel() {
        ModelInfo info = ModelInfo.from(sprinkles, TestModel.class);
        assertEquals(info.tableName, "Tests");
        assertEquals(info.autoIncrementField.name, "id");
        assertEquals(info.columns.size(), 5);
        assertEquals(info.keys.size(), 1);
    }

    @Test
    public void caching() {
        assertTrue(ModelInfo.from(sprinkles, TestModel.class) == ModelInfo.from(sprinkles, TestModel.class));
    }

}
