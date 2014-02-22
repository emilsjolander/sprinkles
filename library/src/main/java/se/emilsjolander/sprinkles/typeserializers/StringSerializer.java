package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

public class StringSerializer implements TypeSerializer<String> {

    @Override
    public String unpack(Cursor c, String name) {
        return c.getString(c.getColumnIndexOrThrow(name));
    }

    @Override
    public void pack(String object, ContentValues cv, String name) {
        cv.put(name, object);
    }

    @Override
    public String toSql(String object) {
        return  DatabaseUtils.sqlEscapeString(object);
    }

    @Override
    public SqlType getSqlType() {
        return SqlType.TEXT;
    }

}
