package se.emilsjolander.sprinkles;

import android.database.Cursor;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * A cursor list lazily instantiates models of type T from the underlying cursor when requesting a certain item.
 * A CursorList must be closed when you are done with it, this will close the underlying cursor.
 *
 * @param <T>
 *     The type of model this list contains.
 */
public class CursorList<T extends QueryResult> implements Iterable<T>, Closeable {

    private Cursor cursor;
    private Class<T> type;
    private boolean closed;

    CursorList(Cursor cursor, Class<T> type) {
        this.cursor = cursor;
        this.type = type;
    }

    /**
     *
     * @return the number of items in this list.
     *
     */
    public int size() {
        return cursor == null ? 0 : cursor.getCount();
    }

    /**
     *
     * @param pos
     *  The position of the item to return
     *
     * @return A new instance of type T representing the cursor data att the requested row.
     */
    public T get(int pos) {
        requireOpen();
        cursor.moveToPosition(pos);
        return Utils.getResultFromCursor(type, cursor);
    }

    /**
     *
     * @return A list containing all the underlying cursors rows as instantiated objects of type T.
     */
    public List<T> asList() {
        List<T> l = new ArrayList<T>(size());
        for (T t : this) {
            l.add(t);
        }
        return l;
    }

    @Override
    public Iterator<T> iterator() {
        return new CursorIterator<T>(cursor, type);
    }

    /**
     *
     * This method should not be needed unless you are interacting with an API that needs to be passed i cursor.
     *
     * @return the underlying cursor.
     */
    public Cursor getCursor() {
        return cursor;
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
