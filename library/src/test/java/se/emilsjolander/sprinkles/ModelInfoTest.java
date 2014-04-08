package se.emilsjolander.sprinkles;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.*;
import static se.emilsjolander.sprinkles.ModelInfo.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ModelInfoTest {

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
        ModelInfo info = ModelInfo.from(TestModel.class);
        assertEquals(info.tableName, "Tests");
        assertEquals(info.autoIncrementColumn.name, "id");
        assertEquals(info.columns.size(), 2);
        assertEquals(info.dynamicColumns.size(), 0);
        assertEquals(info.keys.size(), 1);
    }

    @Test
    public void caching() {
        assertTrue(ModelInfo.from(TestModel.class) == ModelInfo.from(TestModel.class));
    }

}
