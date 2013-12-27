package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by emilsjolander on 27/12/13.
 */
public class FloatSerializer implements TypeSerializer<Float> {

    @Override
    public Float unpack(Cursor c, String name) {
        return c.getFloat(c.getColumnIndexOrThrow(name));
    }

    @Override
    public void pack(Float object, ContentValues cv, String name) {
        cv.put(name, object);
    }

    @Override
    public SqlType getSqlType() {
        return SqlType.REAL;
    }

}
