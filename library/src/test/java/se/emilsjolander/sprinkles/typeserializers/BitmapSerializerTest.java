package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class BitmapSerializerTest {

    @Test
    public void serialize() {

        byte[] bytes = {-1, 0, 0, -1};
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap in = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);

        ContentValues cv = new ContentValues();
        BitmapSerializer serializer = new BitmapSerializer();
        serializer.pack(in, cv, "bitmap");

        MatrixCursor c = new MatrixCursor(new String[]{"bitmap"});
        c.addRow(new Object[]{cv.getAsByteArray("bitmap")});

        c.moveToFirst();
        Bitmap out = serializer.unpack(c, "bitmap");
        assertEquals(in.getPixel(0,0), out.getPixel(0,0));
    }

}