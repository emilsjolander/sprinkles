package se.emilsjolander.sprinkles;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction represents a database transaction in sprinkles.
 * Transactions are past as parameters to certain methods in a model such as save() and delete().
 */
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

    /**
     * Mark a transaction as successful before calling finish() to commit the transaction.
     * @param successful
     *      Whether or not the transaction was successful
     */
	public void setSuccessful(boolean successful) {
		mSuccessful = successful;
	}

    /**
     * Just a getting for the successful property
     *
     * @return if the transaction is marked as successful or not
     */
	public boolean isSuccessful() {
		return mSuccessful;
	}

    /**
     * Finish the transaction.
     * This will commit or rollback the transaction depending on whether is was marked as successful or not
     */
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
