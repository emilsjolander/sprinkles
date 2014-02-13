package se.emilsjolander.sprinkles;

import android.content.ContentValues;
import android.os.AsyncTask;

import se.emilsjolander.sprinkles.Transaction.OnTransactionCommittedListener;
import se.emilsjolander.sprinkles.exceptions.ContentValuesEmptyException;

public abstract class Model implements QueryResult {

    /**
     * Notifies you when a model has been saved
     */
	public interface OnSavedCallback {
		void onSaved();
	}

    /**
     * Notifies you when a model has been deleted
     */
	public interface OnDeletedCallback {
		void onDeleted();
	}

    /**
     * Check if this model is valid. Returning false will not allow this model to be saved.
     *
     * @return whether or not this model is valid.
     */
	public boolean isValid() {
		// optionally implemented by subclass
		return true;
	}

    /**
     * Override to perform an action before this model is created
     */
	protected void beforeCreate() {
		// optionally implemented by subclass
	}

    /**
     * Override to perform an action before this model is saved
     */
	protected void beforeSave() {
		// optionally implemented by subclass
	}

    /**
     * Override to perform an action before this model is deleted
     */
	protected void afterDelete() {
		// optionally implemented by subclass
	}

    /**
     * Check whether this model exists in the database
     *
     * @return true if this model is currently saved in the database (could be an older version)
     */
	final public boolean exists() {
		final Model m = Query.one(
				getClass(),
				String.format("SELECT * FROM %s WHERE %s LIMIT 1",
						Utils.getTableName(getClass()),
						Utils.getWhereStatement(this))).get();
		return m != null;
	}

    /**
     * Save this model to the database.
     * If this model has an @AutoIncrementPrimaryKey annotation on a property
     * than that property will be set when this method returns.
     *
     * @return whether or not the save was successful.
     */
	final public boolean save() {
		Transaction t = new Transaction();
		try {
			t.setSuccessful(save(t));
		} finally {
			t.finish();
		}
		return t.isSuccessful();
	}

    /**
     * Save this model to the database within the given transaction.
     * If this model has an @AutoIncrementPrimaryKey annotation on a property
     * than that property will be set when this method returns.
     *
     * @param t
     *      The transaction to save this model in
     *
     * @return whether or not the save was successful.
     */
	final public boolean save(Transaction t) {
		if (!isValid()) {
			return false;
		}

        boolean doesExist = exists();
        if (!doesExist) {
            beforeCreate();
        }

        beforeSave();
        final ContentValues cv = Utils.getContentValues(this);
        if (cv.size() == 0) {
            throw new ContentValuesEmptyException();
        }
        final String tableName = Utils.getTableName(getClass());
        if (doesExist) {
            if (t.update(tableName, cv, Utils.getWhereStatement(this)) == 0) {
                return false;
            }
        } else {
            long id = t.insert(tableName, cv);
            if (id == -1) {
                return false;
            }

            // set the @AutoIncrement column if one exists
            final ModelInfo info = ModelInfo.from(getClass());
            if (info.autoIncrementColumn != null) {
                info.autoIncrementColumn.field.setAccessible(true);
                try {
                    info.autoIncrementColumn.field.set(this, id);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

		t.addOnTransactionCommittedListener(new OnTransactionCommittedListener() {

			@Override
			public void onTransactionCommitted() {
				Sprinkles.sInstance.mContext.getContentResolver().notifyChange(
						Utils.getNotificationUri(Model.this.getClass()), null);
			}
		});

		return true;
	}

    /**
     * Call save() asynchronously
     */
	final public void saveAsync() {
		saveAsync(null);
	}

    /**
     * Call save() asynchronously
     *
     * @param callback
     *      The callback to invoke when this model has been saved.
     */
	final public void saveAsync(final OnSavedCallback callback) {
		new AsyncTask<Model, Void, Boolean>() {

			protected Boolean doInBackground(Model... params) {
				return params[0].save();
			}

			protected void onPostExecute(Boolean result) {
				if (result && callback != null) {
					callback.onSaved();
				}
			}

		}.execute(this);
	}

    /**
     * Delete this model
     */
	final public void delete() {
		Transaction t = new Transaction();
		try {
			delete(t);
			t.setSuccessful(true);
		} finally {
			t.finish();
		}
	}

    /**
     * Delete this model within the given transaction
     *
     * @param t
     *      The transaction to delete this model in
     */
	final public void delete(Transaction t) {
		t.delete(Utils.getTableName(getClass()), Utils.getWhereStatement(this));
        t.addOnTransactionCommittedListener(new OnTransactionCommittedListener() {

            @Override
            public void onTransactionCommitted() {
                Sprinkles.sInstance.mContext.getContentResolver().notifyChange(
                        Utils.getNotificationUri(Model.this.getClass()), null);
            }
        });
		afterDelete();
	}

    /**
     * Call delete() asynchronously
     */
	final public void deleteAsync() {
		deleteAsync(null);
	}

    /**
     * Call delete() asynchronously
     *
     * @param callback
     *      The callback to invoke when this model has been deleted.
     */
	final public void deleteAsync(final OnDeletedCallback callback) {
		new AsyncTask<Model, Void, Void>() {

			protected Void doInBackground(Model... params) {
				params[0].delete();
				return null;
			}

			protected void onPostExecute(Void result) {
				if (callback != null) {
					callback.onDeleted();
				}
			}

		}.execute(this);
	}

}
