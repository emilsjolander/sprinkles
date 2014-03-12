package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.Cursor;

public class BooleanSerializer implements TypeSerializer<Boolean> {

    @Override
    public Boolean unpack(Cursor c, String name) {
        return c.getInt(c.getColumnIndexOrThrow(name)) > 0;
    }

    @Override
    public void pack(Boolean object, ContentValues cv, String name) {
        cv.put(name, object ? 1 : 0);
    }

    @Override
    public String toSql(Boolean object) {
        return object ? "1" : "0";
    }

    @Override
    public SqlType getSqlType() {
        return SqlType.INTEGER;
    }

}
