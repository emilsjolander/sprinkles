package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by emilsjolander on 27/12/13.
 */
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
    public SqlType getSqlType() {
        return SqlType.TEXT;
    }

}
