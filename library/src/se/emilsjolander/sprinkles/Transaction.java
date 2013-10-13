package se.emilsjolander.sprinkles;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public final class Transaction {
	
	interface OnTransactionCommittedListener {
		void onTransactionCommitted();
	}
	
	private SQLiteDatabase mDb;
	private boolean mSuccessful;
	private List<OnTransactionCommittedListener> mOnTransactionCommittedListeners = new ArrayList<OnTransactionCommittedListener>();

	public Transaction() {
		mDb = DbOpenHelper.getInstance();
		mDb.beginTransaction();
	}

	public void setSuccessful(boolean successful) {
		mSuccessful = successful;
	}

	public boolean isSuccessful() {
		return mSuccessful;
	}

	public void finish() {
		if (mSuccessful) {
			mDb.setTransactionSuccessful();
		}
		mDb.endTransaction();
		
		if (mSuccessful) {
			for (OnTransactionCommittedListener listener : mOnTransactionCommittedListeners) {
				listener.onTransactionCommitted();
			}
		}
	}
	
	long insert(String table, ContentValues values) {
		return mDb.insert(table, null, values);
	}

	int update(String table, ContentValues values, String where) {
		return mDb.update(table, values, where, null);
	}
	
	int delete(String table, String where) {
		return mDb.delete(table, where, null);
	}
	
	void addOnTransactionCommittedListener(OnTransactionCommittedListener listener) {
		mOnTransactionCommittedListeners.add(listener);
	}

}
