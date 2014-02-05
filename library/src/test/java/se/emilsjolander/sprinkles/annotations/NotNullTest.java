package se.emilsjolander.sprinkles.annotations;

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
public class NotNullTest {

    @Table("Tests")
    public static class TestModel extends Model {

        @AutoIncrementPrimaryKey
        @Column("id") private long id;

        @NotNull
        @Column("title")
        String title;
    }

    @Test
    public void enforced() {
        Sprinkles.dropInstances();
        Sprinkles s = Sprinkles.init(Robolectric.application);
        s.addMigration(new Migration().createTable(TestModel.class));

        TestModel m = new TestModel();
        assertFalse(m.save());
        m.title = "hej";
        assertTrue(m.save());
    }

}
