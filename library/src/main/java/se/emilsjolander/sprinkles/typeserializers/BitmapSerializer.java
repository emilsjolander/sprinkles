package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class BitmapSerializer implements TypeSerializer<Bitmap> {

    @Override
    public Bitmap unpack(Cursor c, String name) {
        byte[] bytes = c.getBlob(c.getColumnIndex(name));
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inMutable = true;
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        return bmp;
    }

    @Override
    public void pack(Bitmap object, ContentValues cv, String name) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        object.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        cv.put(name, bytes);
    }

    @Override
    public String toSql(Bitmap object) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        object.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return new String(bytes);
    }

    @Override
    public SqlType getSqlType() {
        return SqlType.BLOB;
    }

}
