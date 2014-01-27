package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.MatrixCursor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class BooleanSerializerTest {

    @Test
    public void serialize() {
        BooleanSerializer serializer = new BooleanSerializer();
        String name = "boolean";

        Boolean obj = true;
        ContentValues cv = new ContentValues();
        serializer.pack(obj, cv, name);

        MatrixCursor c = new MatrixCursor(new String[]{name});
        c.addRow(new Object[]{cv.getAsInteger(name)});

        c.moveToFirst();
        assertTrue(serializer.unpack(c, name));
    }

}
