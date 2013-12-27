package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.MatrixCursor;

import org.junit.Test;
import org.junit.runner.RunWith;

import se.emilsjolander.sprinkles.RobolectricGradleTestRunner;

import static junit.framework.Assert.*;

@RunWith(RobolectricGradleTestRunner.class)
public class FloatSerializerTest {

    @Test
    public void serialize() {
        FloatSerializer serializer = new FloatSerializer();
        String name = "float";

        Float obj = 1f;
        ContentValues cv = new ContentValues();
        serializer.pack(obj, cv, name);

        MatrixCursor c = new MatrixCursor(new String[]{name});
        c.addRow(new Object[]{cv.getAsFloat(name)});

        c.moveToFirst();
        assertTrue(serializer.unpack(c, name).equals(1f));
    }

}
