package se.emilsjolander.sprinkles.annotations;

import android.test.AndroidTestCase;

import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Sprinkles;
import se.emilsjolander.sprinkles.models.UniqueTestModel;

public class UniqueTest extends AndroidTestCase {

	public void testUniqueAnnotationIsEnforced() {

		Sprinkles sprinkles = Sprinkles.getInstance(getContext());
		sprinkles.addMigration(new Migration().createTable(UniqueTestModel.class));

		assertTrue(new UniqueTestModel().setName("testName").save());
		assertTrue(new UniqueTestModel().setName("testName2").save());
		assertFalse(new UniqueTestModel().setName("testName").save());
	}
}
