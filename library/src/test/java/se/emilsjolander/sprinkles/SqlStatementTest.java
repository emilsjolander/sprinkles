package se.emilsjolander.sprinkles;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SqlStatementTest {

    @Before
    public void setup() {
        Sprinkles.dropInstances();
        Sprinkles s = Sprinkles.init(Robolectric.application);
        s.addMigration(TestModel.MIGRATION);
    }

    @Test
    public void execute() {
        new TestModel().save();
        new TestModel().save();
        new TestModel().save();
        CursorList<TestModel> result = Query.all(TestModel.class).get();
        assertEquals(result.size(), 3);
        result.close();

        new SqlStatement("delete from Tests").execute();
        result = Query.all(TestModel.class).get();
        assertEquals(result.size(), 0);
        result.close();
    }

}
