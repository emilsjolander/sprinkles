package se.emilsjolander.sprinkles;

import android.database.Cursor;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CursorList<T extends Model> implements Iterable<T>, Closeable {

    private Cursor cursor;
    private Class<T> type;
    private boolean closed;

    CursorList(Cursor cursor, Class<T> type) {
        this.cursor = cursor;
        this.type = type;
    }

    public int size() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public T get(int pos) {
        requireOpen();
        cursor.moveToPosition(pos);
        return Utils.getModelFromCursor(type, cursor);
    }

    public List<T> asList() {
        List<T> l = new ArrayList<T>(size());
        for (T t : this) {
            l.add(t);
        }
        return l;
    }

    @Override
    public Iterator<T> iterator() {
        return new CursorIterator(cursor, type);
    }

    @Override
    public void close() {
        if (cursor != null) {
            cursor.close();
        }
        closed = true;
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("Cannot call methods on a closed CursorList");
        }
    }

}
