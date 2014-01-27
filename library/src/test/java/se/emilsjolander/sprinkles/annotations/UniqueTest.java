package se.emilsjolander.sprinkles.annotations;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Sprinkles;
import se.emilsjolander.sprinkles.models.UniqueTestModel;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class UniqueTest {

    @Test
	public void enforced() {
        Sprinkles.dropInstances();
		Sprinkles sprinkles = Sprinkles.getInstance(Robolectric.application);
		sprinkles.addMigration(new Migration().createTable(UniqueTestModel.class));

		assertTrue(new UniqueTestModel().setName("testName").save());
		assertTrue(new UniqueTestModel().setName("testName2").save());
		assertFalse(new UniqueTestModel().setName("testName").save());
	}

}
