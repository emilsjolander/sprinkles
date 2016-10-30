package com.lsjwzh.orm.typeserializers;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Date;

public class DateSerializer implements TypeSerializer<Date> {

    @Override
    public Date unpack(Cursor c, String name) {
        return new Date(c.getLong(c.getColumnIndexOrThrow(name)));
    }

    @Override
    public void pack(Date object, ContentValues cv, String name) {
        cv.put(name, object==null?0:object.getTime());
    }

    @Override
    public String toSql(Date object) {
        return String.valueOf(object==null?0:object.getTime());
    }

    @Override
    public SqlType getSqlType() {
        return SqlType.INTEGER;
    }

}
