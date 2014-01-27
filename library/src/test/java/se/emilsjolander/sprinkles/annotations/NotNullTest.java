package se.emilsjolander.sprinkles.annotations;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertTrue;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class NotNullTest {

    @Test
    public void enforced() {
        assertTrue(false);
    }

}
