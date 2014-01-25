package se.emilsjolander.sprinkles.annotations;

import org.junit.Test;
import org.junit.runner.RunWith;

import se.emilsjolander.sprinkles.RobolectricGradleTestRunner;

import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class NotNullTest {

    @Test
    public void enforced() {
        assertTrue(false);
    }

}
