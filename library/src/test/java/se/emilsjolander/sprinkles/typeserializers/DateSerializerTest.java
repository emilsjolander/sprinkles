package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.MatrixCursor;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import se.emilsjolander.sprinkles.RobolectricGradleTestRunner;

import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class DateSerializerTest {

    @Test
    public void serialize() {
        DateSerializer serializer = new DateSerializer();
        String name = "date";

        Date obj = new Date(100);
        ContentValues cv = new ContentValues();
        serializer.pack(obj, cv, name);

        MatrixCursor c = new MatrixCursor(new String[]{name});
        c.addRow(new Object[]{cv.getAsLong(name)});

        c.moveToFirst();
        assertTrue(serializer.unpack(c, name).equals(new Date(100)));
    }

}
