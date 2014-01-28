package se.emilsjolander.sprinkles.annotations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.Sprinkles;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CascadeDeleteTest {

    @Table("Tests")
    public class TestModel extends Model {

        @AutoIncrementPrimaryKey
        @Column("id") private long id;

        @CascadeDelete
        @ForeignKey("ForeignModels(id)")
        @Column("foreign_id") private long foreign_id;

        @Column("title") private String title;

    }

    @Table("ForeignModels")
    public class ForeignModel extends Model {

        @AutoIncrementPrimaryKey
        @Column("id") private long id;

        @Column("title") private String title;

    }

    @Before
    public void initTables() {
        Sprinkles.dropInstances();
        Sprinkles.getInstance(Robolectric.application)
                .addMigration(new Migration().createTable(ForeignModel.class).createTable(TestModel.class));
    }

    @Test
    public void enforced() {
        ForeignModel fm = new ForeignModel();
        fm.title = "hej";
        assertTrue(fm.save());

        TestModel tm = new TestModel();
        tm.title = "hej";
        tm.foreign_id = fm.id;
        assertTrue(tm.save());

        fm.delete();
        assertFalse(tm.exists());
    }

}