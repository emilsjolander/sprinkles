package se.emilsjolander.sprinkles;

import android.database.Cursor;

import java.util.Iterator;

/**
 * Created by emilsjolander on 28/11/13.
 */
public class CursorIterator<T extends Model> implements Iterator<T> {

    private Cursor cursor;
    private Class<T> type;
    private int pos = -1;

    CursorIterator(Cursor cursor, Class<T> type) {
        this.cursor = cursor;
        this.type = type;
    }

    @Override
    public boolean hasNext() {
        cursor.moveToPosition(pos);
        return !cursor.isLast();
    }

    @Override
    public T next() {
        pos++;
        cursor.moveToPosition(pos);
        return Utils.getModelFromCursor(type, cursor);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
