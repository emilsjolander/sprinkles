package com.lsjwzh.orm;

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
        void onTransactionRollback();
	}

	private final Sprinkles sprinkles;
	private SQLiteDatabase mDb;
	private boolean mSuccessful;
	private List<OnTransactionCommittedListener> mOnTransactionCommittedListeners = new ArrayList<OnTransactionCommittedListener>();

	public Transaction(Sprinkles sprinkles) {
		this.sprinkles = sprinkles;
		mDb = sprinkles.getDatabase();
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
		}else {
            for (OnTransactionCommittedListener listener : mOnTransactionCommittedListeners) {
                listener.onTransactionRollback();
            }
        }
	}

	public long insert(ModelInfo table, ContentValues values) {
        sprinkles.dataResolver.assureTableExist(table);
		return mDb.insert(table.tableName, null, values);
	}

	public int update(ModelInfo table, ContentValues values, String where) {
		sprinkles.dataResolver.assureTableExist(table);
		return mDb.update(table.tableName, values, where, null);
	}

	public int delete(ModelInfo table, String where) {
		return mDb.delete(table.tableName, where, null);
	}

	public void addOnTransactionCommittedListener(OnTransactionCommittedListener listener) {
		mOnTransactionCommittedListeners.add(listener);
	}



}
