package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.MatrixCursor;

import org.junit.Test;
import org.junit.runner.RunWith;

import se.emilsjolander.sprinkles.RobolectricGradleTestRunner;

import static junit.framework.Assert.*;

@RunWith(RobolectricGradleTestRunner.class)
public class LongSerializerTest {

    @Test
    public void serialize() {
        LongSerializer serializer = new LongSerializer();
        String name = "long";

        Long obj = 1l;
        ContentValues cv = new ContentValues();
        serializer.pack(obj, cv, name);

        MatrixCursor c = new MatrixCursor(new String[]{name});
        c.addRow(new Object[]{cv.getAsLong(name)});

        c.moveToFirst();
        assertTrue(serializer.unpack(c, name).equals(1l));
    }

}
