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
public class DoubleSerializerTest {

    @Test
    public void serialize() {
        DoubleSerializer serializer = new DoubleSerializer();
        String name = "double";

        Double obj = 1d;
        ContentValues cv = new ContentValues();
        serializer.pack(obj, cv, name);

        MatrixCursor c = new MatrixCursor(new String[]{name});
        c.addRow(new Object[]{cv.getAsDouble(name)});

        c.moveToFirst();
        assertTrue(serializer.unpack(c, name).equals(1d));
    }

}
