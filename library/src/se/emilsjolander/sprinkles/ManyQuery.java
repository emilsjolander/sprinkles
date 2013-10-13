package se.emilsjolander.sprinkles;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.sprinkles.Query.OnQueryResultHandler;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

public final class ManyQuery<T extends Model> {

	Class<T> resultClass;
	String sqlQuery;

	ManyQuery() {
	}

	public List<T> get() {
		final SQLiteDatabase db = DbOpenHelper.getInstance();
		final Cursor c = db.rawQuery(sqlQuery, null);

		List<T> result = new ArrayList<T>();
		while (c.moveToNext()) {
			result.add(Utils.getModelFromCursor(resultClass, c));
		}

		c.close();
		return result;
	}

	public void getAsync(LoaderManager lm, OnQueryResultHandler<List<T>> handler) {
		final int loaderId = sqlQuery.hashCode();
		lm.initLoader(loaderId, null,
				getLoaderCallbacks(sqlQuery, resultClass, handler, false, null));
	}

	public void getAsyncWithUpdates(LoaderManager lm,
			OnQueryResultHandler<List<T>> handler,
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
			final OnQueryResultHandler<List<T>> handler,
			final boolean getUpdates,
			final Class<? extends Model>[] respondsToUpdatedOf) {
		return new LoaderCallbacks<Cursor>() {

			@Override
			public void onLoaderReset(Loader<Cursor> arg0) {
				handler.onResult(new ArrayList<T>());
			}

			@Override
			public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
				List<T> result = new ArrayList<T>();
				while (c.moveToNext()) {
					result.add(Utils.getModelFromCursor(resultClass, c));
				}
				handler.onResult(result);

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
			OnQueryResultHandler<List<T>> handler) {
		final int loaderId = sqlQuery.hashCode();
		lm.initLoader(
				loaderId,
				null,
				getSupportLoaderCallbacks(sqlQuery, resultClass, handler,
						false, null));
	}

	public void getAsyncWithUpdates(android.support.v4.app.LoaderManager lm,
			OnQueryResultHandler<List<T>> handler,
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
			final OnQueryResultHandler<List<T>> handler,
			final boolean getUpdates,
			final Class<? extends Model>[] respondsToUpdatedOf) {
		return new android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>() {

			@Override
			public void onLoaderReset(
					android.support.v4.content.Loader<Cursor> arg0) {
				handler.onResult(new ArrayList<T>());
			}

			@Override
			public void onLoadFinished(
					android.support.v4.content.Loader<Cursor> loader, Cursor c) {
				List<T> result = new ArrayList<T>();
				while (c.moveToNext()) {
					result.add(Utils.getModelFromCursor(resultClass, c));
				}
				handler.onResult(result);

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
