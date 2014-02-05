package se.emilsjolander.sprinkles;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.exceptions.NoTypeSerializerFoundException;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SprinklesTest {

    @Test
    public void init() {
        assertTrue(false);
    }

    @Test
    public void addMigration() {
        assertTrue(false);
    }

    @Test
    public void registerType() {
        assertTrue(false);
    }

    @Test
    public void getExistingTypeSerializer() {
        assertTrue(false);
    }

    @Test(expected = NoTypeSerializerFoundException.class)
    public void getNonExistingTypeSerializer() {
        assertTrue(false);
    }

}
