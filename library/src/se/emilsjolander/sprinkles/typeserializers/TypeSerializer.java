package se.emilsjolander.sprinkles.typeserializers;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by emilsjolander on 27/12/13.
 */
public interface TypeSerializer<T> {

    T unpack(Cursor c, String name);

    void pack(T object, ContentValues cv, String name);

    SqlType getSqlType();

}
