package com.lsjwzh.orm;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Iterator;

class CursorIterator<T extends QueryResult> implements Iterator<T> {

    private final Sprinkles sprinkles;
    private Cursor cursor;
    private Class<T> type;
    private int pos = -1;
    private int count;

    CursorIterator(@NonNull Sprinkles sprinkles, Cursor cursor, Class<T> type) {
        this.sprinkles = sprinkles;
        this.cursor = cursor;
        this.type = type;
        this.count = cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public boolean hasNext() {
        return (pos+1) < count;
    }

    @Override
    public T next() {
        pos++;
        cursor.moveToPosition(pos);
        return sprinkles.dataResolver.getResultFromCursor(type, cursor);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
