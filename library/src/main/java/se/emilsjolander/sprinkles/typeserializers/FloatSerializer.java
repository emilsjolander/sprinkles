package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.Cursor;

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
    public String toSql(Float object) {
        return String.valueOf(object);
    }

    @Override
    public SqlType getSqlType() {
        return SqlType.REAL;
    }

}
