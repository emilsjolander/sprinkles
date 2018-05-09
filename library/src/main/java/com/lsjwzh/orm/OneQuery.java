package com.lsjwzh.orm;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Object representing a Query that will return a single result.
 *
 * @param <T> The type of model to return
 */
public final class OneQuery<T extends QueryResult> {

    Class<T> resultClass;
    String placeholderQuery;
    String rawQuery;
    Sprinkles sprinkles;

    OneQuery(Sprinkles sprinkles) {
        this.sprinkles = sprinkles;
    }

    /**
     * Execute the query synchronously
     *
     * @return the result of the query.
     */
    public T get() {
        final SQLiteDatabase db = sprinkles.getDatabase();
        sprinkles.dataResolver.assureTableExist(ModelInfo.from(sprinkles, resultClass));
        final Cursor c = db.rawQuery(rawQuery, null);
        T result = null;
        if (c.moveToFirst()) {
            result = sprinkles.dataResolver.getResultFromCursor(resultClass, c);
        }
        c.close();
        return result;
    }

}
