package se.emilsjolander.sprinkles;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.model.TestModel;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SqlStatementTest {

    private Sprinkles sprinkles;

    @Before
    public void setup() {
        sprinkles = Sprinkles.init(Robolectric.application);
    }

    @Test
    public void execute() {
        new TestModel(sprinkles).save();
        new TestModel(sprinkles).save();
        new TestModel(sprinkles).save();
        CursorList<TestModel> result = Query.all(sprinkles, TestModel.class).get();
        assertEquals(result.size(), 3);
        result.close();

        new SqlStatement(sprinkles, "delete from Tests").execute();
        result = Query.all(sprinkles, TestModel.class).get();
        assertEquals(result.size(), 0);
        result.close();
    }

}
