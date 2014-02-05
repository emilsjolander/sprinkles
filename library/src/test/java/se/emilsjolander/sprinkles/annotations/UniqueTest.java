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
public class UniqueTest {

    @Table("Tests")
    public static class TestModel extends Model {

        @AutoIncrementPrimaryKey
        @Column("id") private long id;

        @Unique
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
	public void enforced() {
        Sprinkles.dropInstances();
		Sprinkles sprinkles = Sprinkles.init(Robolectric.application);
		sprinkles.addMigration(new Migration().createTable(TestModel.class));

        TestModel t1 = new TestModel();
        t1.setTitle("title1");
        TestModel t2 = new TestModel();
        t2.setTitle("title2");
        TestModel t3 = new TestModel();
        t3.setTitle("title1");

		assertTrue(t1.save());
		assertTrue(t2.save());
		assertFalse(t3.save());
	}

}
