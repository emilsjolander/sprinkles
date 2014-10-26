package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Date;

public class DateSerializer implements TypeSerializer<Date> {

    @Override
    public Date unpack(Cursor c, String name) {
        long l = c.getLong(c.getColumnIndexOrThrow(name));
        return l > 0 ? new Date(l) : null;
    }

    @Override
    public void pack(Date object, ContentValues cv, String name) {
        if(object != null)
            cv.put(name, object.getTime());
    }

    @Override
    public String toSql(Date object) {
        return object != null ? String.valueOf(object.getTime()) : null;
    }

    @Override
    public SqlType getSqlType() {
        return SqlType.INTEGER;
    }

}
