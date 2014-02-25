package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.Cursor;

public class IntSerializer implements TypeSerializer<Integer> {

    @Override
    public Integer unpack(Cursor c, String name) {
        return c.getInt(c.getColumnIndexOrThrow(name));
    }

    @Override
    public void pack(Integer object, ContentValues cv, String name) {
        cv.put(name, object);
    }

    @Override
    public String toSql(Integer object) {
        return String.valueOf(object);
    }

    @Override
    public SqlType getSqlType() {
        return SqlType.INTEGER;
    }

}
