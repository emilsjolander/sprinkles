package se.emilsjolander.sprinkles.annotations;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.Sprinkles;

import java.lang.String;

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

        @Unique(group = "singleGroup")
        @Column("singleGroup")
        private String singleGroup;

        @Unique(group = "comboGroup")
        @Column("comboGroupA")
        private String comboGroupA;

        @Unique(group = "comboGroup")
        @Column("comboGroupB")
        private String comboGroupB;

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public String getSingleGroup() {
            return singleGroup;
        }

        public void setSingleGroup(String singleGroup) {
            this.singleGroup = singleGroup;
        }

        public String getComboGroupA() {
            return comboGroupA;
        }

        public void setComboGroupA(String comboGroupA) {
            this.comboGroupA = comboGroupA;
        }

        public String getComboGroupB() {
            return comboGroupB;
        }

        public void setComboGroupB(String comboGroupB) {
            this.comboGroupB = comboGroupB;
        }
    }

    @Test
	public void enforced() {
        Sprinkles.dropInstances();
		Sprinkles sprinkles = Sprinkles.init(Robolectric.application);
		sprinkles.addMigration(new Migration().createTable(TestModel.class));

        TestModel t1 = new TestModel();
        t1.setTitle("title1");
        t1.setSingleGroup("singleGroup1");
        t1.setComboGroupA("comboGroup1");
        t1.setComboGroupB("comboGroup1");
        TestModel t2 = new TestModel();
        t2.setTitle("title2");
        t2.setSingleGroup("singleGroup2");
        t2.setComboGroupA("comboGroup2");
        t2.setComboGroupB("comboGroup2");
        TestModel t3 = new TestModel();
        t3.setTitle("title1");
        t3.setSingleGroup("singleGroup3");
        t3.setComboGroupA("comboGroup3");
        t3.setComboGroupB("comboGroup3");
        TestModel t4 = new TestModel();
        t4.setTitle("title3");
        t4.setSingleGroup("singleGroup1");
        t4.setComboGroupA("comboGroup4");
        t4.setComboGroupB("comboGroup4");
        TestModel t5 = new TestModel();
        t5.setTitle("title4");
        t5.setSingleGroup("singleGroup4");
        t5.setComboGroupA("comboGroup4");
        t5.setComboGroupB("comboGroup5");
        TestModel t6 = new TestModel();
        t6.setTitle("title5");
        t6.setSingleGroup("singleGroup5");
        t6.setComboGroupA("comboGroup5");
        t6.setComboGroupB("comboGroup4");
        TestModel t7 = new TestModel();
        t7.setTitle("title6");
        t7.setSingleGroup("singleGroup6");
        t7.setComboGroupA("comboGroup1");
        t7.setComboGroupB("comboGroup1");

		assertTrue(t1.save());
		assertTrue(t2.save());
		assertFalse(t3.save());
        assertFalse(t4.save());
        assertTrue(t5.save());
        assertTrue(t6.save());
        assertFalse(t7.save());
	}

}
