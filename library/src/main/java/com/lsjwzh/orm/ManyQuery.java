package com.lsjwzh.orm;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Object representing a Query that will return a list of results.
 *
 * @param <T> The type of list to return
 */
public final class ManyQuery<T extends QueryResult> {

    Class<T> resultClass;
    String placeholderQuery;
    String rawQuery;
    private final Sprinkles sprinkles;

    ManyQuery(Sprinkles sprinkles) {
        this.sprinkles = sprinkles;
    }

    /**
     * Execute the query synchronously
     *
     * @return the result of the query. Remember to close me!
     */
    public CursorList<T> get() {
        sprinkles.dataResolver.assureTableExist(ModelInfo.from(sprinkles, resultClass));
        final SQLiteDatabase db = sprinkles.getDatabase();
        final Cursor c = db.rawQuery(rawQuery, null);
        return new CursorList<>(sprinkles, c, resultClass);
    }

}
