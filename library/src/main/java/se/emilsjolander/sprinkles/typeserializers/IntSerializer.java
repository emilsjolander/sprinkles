package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by emilsjolander on 27/12/13.
 */
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
    public SqlType getSqlType() {
        return SqlType.INTEGER;
    }

}
