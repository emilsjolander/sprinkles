package se.emilsjolander.sprinkles;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;
import se.emilsjolander.sprinkles.exceptions.NoTableAnnotationException;

import static junit.framework.Assert.*;
import static se.emilsjolander.sprinkles.ModelInfo.ColumnField;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ModelInfoTest {

    public static class AbsTestModel extends Model {

        @AutoIncrementPrimaryKey
        @Column("id") private long id;
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
        assertEquals(info.primaryKeys.size(), 1);
        assertEquals(info.foreignKeys.size(), 0);
    }

    @Test(expected = NoTableAnnotationException.class)
    public void getTableNameNoAnnotation() {
        ModelInfo.from(AbsTestModel.class);
    }

    @Test
    public void caching() {
        assertTrue(ModelInfo.from(TestModel.class) == ModelInfo.from(TestModel.class));
    }

}
