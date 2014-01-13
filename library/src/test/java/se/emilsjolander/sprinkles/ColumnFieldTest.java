package se.emilsjolander.sprinkles;

import com.google.common.testing.EqualsTester;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricGradleTestRunner.class)
public class ColumnFieldTest {

	@Test
	public void equals() {
		EqualsTester tester = new EqualsTester();

		ColumnField f1 = new ColumnField();
		f1.name = "a_name";
		ColumnField f2 = new ColumnField();
		f2.name = "a_name";
		tester.addEqualityGroup(f1, f2);

		ColumnField f3 = new ColumnField();
		f3.name = "another_name";
		tester.addEqualityGroup(f3);

		ColumnField f4 = new ColumnField();
		tester.addEqualityGroup(f4);

		tester.testEquals();
	}
}
