package se.emilsjolander.sprinkles.annotations;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.RobolectricGradleTestRunner;
import se.emilsjolander.sprinkles.Sprinkles;
import se.emilsjolander.sprinkles.models.UniqueTestModel;

import static junit.framework.Assert.*;

@RunWith(RobolectricGradleTestRunner.class)
public class UniqueTest {

    @Test
	public void uniqueEnforced() {
		Sprinkles sprinkles = Sprinkles.getInstance(Robolectric.application);
		sprinkles.addMigration(new Migration().createTable(UniqueTestModel.class));

		assertTrue(new UniqueTestModel().setName("testName").save());
		assertTrue(new UniqueTestModel().setName("testName2").save());
		assertFalse(new UniqueTestModel().setName("testName").save());
	}

}
