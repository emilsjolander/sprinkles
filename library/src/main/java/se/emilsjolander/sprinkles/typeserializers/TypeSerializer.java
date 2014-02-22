package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.Cursor;

public interface TypeSerializer<T> {

    T unpack(Cursor c, String name);

    void pack(T object, ContentValues cv, String name);

    String toSql(T object);

    SqlType getSqlType();

}
