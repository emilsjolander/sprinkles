package se.emilsjolander.sprinkles;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

public final class ManyQuery<T extends Model> {

    public interface ResultHandler<T extends Model> {
        void handleResult(CursorList<T> result);
    }

	Class<T> resultClass;
	String sqlQuery;

	ManyQuery() {
	}

	public CursorList<T> get() {
		final SQLiteDatabase db = DbOpenHelper.getInstance();
		final Cursor c = db.rawQuery(sqlQuery, null);
		return new CursorList<T>(c, resultClass);
	}

	public void getAsync(LoaderManager lm, ResultHandler<T> handler) {
		final int loaderId = sqlQuery.hashCode();
		lm.initLoader(loaderId, null,
				getLoaderCallbacks(sqlQuery, resultClass, handler, false, null));
	}

	public void getAsyncWithUpdates(LoaderManager lm,
			ResultHandler<T> handler,
			Class<?>... respondsToUpdatedOf) {
		final int loaderId = sqlQuery.hashCode();
		lm.initLoader(
				loaderId,
				null,
				getLoaderCallbacks(sqlQuery, resultClass, handler, true,
						(Class<? extends Model>[]) Utils.concatClassArrays(
								respondsToUpdatedOf,
								new Class[] { resultClass })));
	}

	private LoaderCallbacks<Cursor> getLoaderCallbacks(final String sqlQuery,
			final Class<T> resultClass,
			final ResultHandler<T> handler,
			final boolean getUpdates,
			final Class<? extends Model>[] respondsToUpdatedOf) {
		return new LoaderCallbacks<Cursor>() {

			@Override
			public void onLoaderReset(Loader<Cursor> arg0) {
				handler.handleResult(new CursorList<T>(null, null));
			}

			@Override
			public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
				handler.handleResult(new CursorList<T>(c, resultClass));
				if (!getUpdates) {
					loader.abandon();
				}
			}

			@Override
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				return new CursorLoader(Sprinkles.sInstance.mContext, sqlQuery,
						respondsToUpdatedOf);
			}
		};
	}

	public void getAsync(android.support.v4.app.LoaderManager lm,
			ResultHandler<T> handler) {
		final int loaderId = sqlQuery.hashCode();
		lm.initLoader(
				loaderId,
				null,
				getSupportLoaderCallbacks(sqlQuery, resultClass, handler,
						false, null));
	}

	public void getAsyncWithUpdates(android.support.v4.app.LoaderManager lm,
			ResultHandler<T> handler,
			Class<?>... respondsToUpdatedOf) {
		final int loaderId = sqlQuery.hashCode();
		lm.initLoader(
				loaderId,
				null,
				getSupportLoaderCallbacks(sqlQuery, resultClass, handler, true,
						(Class<? extends Model>[]) Utils.concatClassArrays(
								respondsToUpdatedOf,
								new Class[] { resultClass })));
	}

	private android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> getSupportLoaderCallbacks(
			final String sqlQuery, final Class<T> resultClass,
			final ResultHandler<T> handler,
			final boolean getUpdates,
			final Class<? extends Model>[] respondsToUpdatedOf) {
		return new android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>() {

			@Override
			public void onLoaderReset(
					android.support.v4.content.Loader<Cursor> arg0) {
				handler.handleResult(new CursorList<T>(null, null));
			}

			@Override
			public void onLoadFinished(
					android.support.v4.content.Loader<Cursor> loader, Cursor c) {
                handler.handleResult(new CursorList<T>(c, resultClass));
                if (!getUpdates) {
                    loader.abandon();
                }

				if (!getUpdates) {
					loader.abandon();
				}
			}

			@Override
			public android.support.v4.content.Loader<Cursor> onCreateLoader(
					int id, Bundle args) {
				return new SupportCursorLoader(Sprinkles.sInstance.mContext,
						sqlQuery, respondsToUpdatedOf);
			}
		};
	}

}
