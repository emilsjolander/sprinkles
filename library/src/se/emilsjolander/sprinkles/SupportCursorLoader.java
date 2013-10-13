package se.emilsjolander.sprinkles;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;


class SupportCursorLoader extends AsyncTaskLoader<Cursor> {

	private final ForceLoadContentObserver mObserver;

	private String mSql;
	private Class<? extends Model>[] mDependencies;
	private Cursor mCursor;

	public SupportCursorLoader(Context context, String sql,
			Class<? extends Model>[] dependencies) {
		super(context);
		mObserver = new ForceLoadContentObserver();
		mSql = sql;
		mDependencies = dependencies;
	}

	/* Runs on a worker thread */
	@Override
	public Cursor loadInBackground() {
		final SQLiteDatabase db = DbOpenHelper.getInstance();
		Cursor cursor = db.rawQuery(mSql, null);
		
		if (cursor != null) {
			// Ensure the cursor window is filled
			cursor.getCount();

			if (mDependencies != null) {
				cursor.registerContentObserver(mObserver);
				for (Class<? extends Model> dependency : mDependencies) {
					getContext().getContentResolver().registerContentObserver(
							Utils.getNotificationUri(dependency), false, mObserver);
				}
			}
		}
		return cursor;
	}

	/* Runs on the UI thread */
	@Override
	public void deliverResult(Cursor cursor) {
		if (isReset()) {
			// An async query came in while the loader is stopped
			if (cursor != null) {
				cursor.close();
			}
			return;
		}
		Cursor oldCursor = mCursor;
		mCursor = cursor;

		if (isStarted()) {
			super.deliverResult(cursor);
		}

		if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
			oldCursor.close();
		}
	}

	/**
	 * Starts an asynchronous load of the contacts list data. When the result is
	 * ready the callbacks will be called on the UI thread. If a previous load
	 * has been completed and is still valid the result may be passed to the
	 * callbacks immediately.
	 * 
	 * Must be called from the UI thread
	 */
	@Override
	protected void onStartLoading() {
		if (mCursor != null) {
			deliverResult(mCursor);
		}
		if (takeContentChanged() || mCursor == null) {
			forceLoad();
		}
	}

	/**
	 * Must be called from the UI thread
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	@Override
	public void onCanceled(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}
	
	@Override
	protected void onAbandon() {
		super.onAbandon();
		getContext().getContentResolver().unregisterContentObserver(mObserver);
	}

	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}
		mCursor = null;
	}

}
