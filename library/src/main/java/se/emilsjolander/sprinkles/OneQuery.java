package se.emilsjolander.sprinkles;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

/**
 * Object representing a Query that will return a single result.
 *
 * @param <T>
 *     The type of model to return
 */
public final class OneQuery<T extends QueryResult> {

    /**
     * Implement to get results delivered from a asynchronous query.
     *
     * @param <T>
     *     The type of model that the result will represent.
     */
    public interface ResultHandler<T extends QueryResult> {

        /**
         * @param result
         *      The result of the query.
         *
         * @return whether or not you want updated result when something changes in the underlying data.
         *
         */
        boolean handleResult(T result);
    }

	Class<T> resultClass;
	String sqlQuery;

	OneQuery() {
	}

    /**
     * Execute the query synchronously
     *
     * @return the result of the query.
     */
	public T get() {
		final SQLiteDatabase db = Sprinkles.getDatabase();
		final Cursor c = db.rawQuery(sqlQuery, null);

		T result = null;
		if (c.moveToFirst()) {
			result = Utils.getResultFromCursor(resultClass, c);
		}

		c.close();
		return result;
	}

    /**
     * Execute the query asynchronously
     *
     * @param lm
     *      The loader manager to use for loading the data
     *
     * @param handler
     *      The ResultHandler to notify of the query result and any updates to that result.
     *
     * @param respondsToUpdatedOf
     *      A list of models excluding the queried model that should also trigger a update to the result if they change.
     */
    @SuppressWarnings("unchecked")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void getAsync(LoaderManager lm,
			ResultHandler<T> handler,
            Class<? extends Model>... respondsToUpdatedOf) {
        if (Model.class.isAssignableFrom(resultClass)) {
            respondsToUpdatedOf = Utils.concatArrays(respondsToUpdatedOf, new Class[]{resultClass});
        }
		final int loaderId = sqlQuery.hashCode();
		lm.initLoader(loaderId, null,
				getLoaderCallbacks(sqlQuery, resultClass, handler, respondsToUpdatedOf));
	}

    /**
     * Execute the query asynchronously
     *
     * @param lm
     *      The loader manager to use for loading the data
     *
     * @param handler
     *      The ResultHandler to notify of the query result and any updates to that result.
     *
     * @param respondsToUpdatedOf
     *      A list of models excluding the queried model that should also trigger a update to the result if they change.
     */
    @SuppressWarnings("unchecked")
	public void getAsync(android.support.v4.app.LoaderManager lm,
			ResultHandler<T> handler,
            Class<? extends Model>... respondsToUpdatedOf) {
        if (Model.class.isAssignableFrom(resultClass)) {
            respondsToUpdatedOf = Utils.concatArrays(respondsToUpdatedOf, new Class[]{resultClass});
        }
		final int loaderId = sqlQuery.hashCode();
		lm.initLoader(loaderId, null,
				getSupportLoaderCallbacks(sqlQuery, resultClass, handler, respondsToUpdatedOf));
	}

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private LoaderCallbacks<Cursor> getLoaderCallbacks(final String sqlQuery,
                                                       final Class<T> resultClass,
                                                       final ResultHandler<T> handler,
                                                       final Class<? extends Model>[] respondsToUpdatedOf) {
        return new LoaderCallbacks<Cursor>() {

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                if (!loader.isAbandoned()) {
                    handler.handleResult(null);
                }
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
                T result = null;
                if (c.moveToFirst()) {
                    result = Utils.getResultFromCursor(resultClass, c);
                }

                if (!handler.handleResult(result)) {
                    loader.abandon();
                }
            }

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(Sprinkles.sInstance.mContext, sqlQuery, respondsToUpdatedOf);
            }
        };
    }

	private android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> getSupportLoaderCallbacks(
			final String sqlQuery,
            final Class<T> resultClass,
			final ResultHandler<T> handler,
			final Class<? extends Model>[] respondsToUpdatedOf) {
		return new android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>() {

			@Override
			public void onLoaderReset(
					android.support.v4.content.Loader<Cursor> loader) {
                if (!loader.isAbandoned()) {
                    handler.handleResult(null);
                }
			}

			@Override
			public void onLoadFinished(
					android.support.v4.content.Loader<Cursor> loader, Cursor c) {
				T result = null;
				if (c.moveToFirst()) {
					result = Utils.getResultFromCursor(resultClass, c);
				}

				if (!handler.handleResult(result)) {
					loader.abandon();
				}
			}

			@Override
			public android.support.v4.content.Loader<Cursor> onCreateLoader(
					int id, Bundle args) {
				return new SupportCursorLoader(Sprinkles.sInstance.mContext, sqlQuery, respondsToUpdatedOf);
			}
		};
	}

}
