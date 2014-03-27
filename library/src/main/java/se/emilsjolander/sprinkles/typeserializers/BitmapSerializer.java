package se.emilsjolander.sprinkles.typeserializers;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.ByteArrayOutputStream;

public class BitmapSerializer implements TypeSerializer<Bitmap> {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Bitmap unpack(Cursor c, String name) {
        byte[] bytes = c.getBlob(c.getColumnIndex(name));
        BitmapFactory.Options opts = new BitmapFactory.Options();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            opts.inMutable = true;
        }
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
