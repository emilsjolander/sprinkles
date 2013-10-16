package se.emilsjolander.sprinkles;

import java.util.List;

import se.emilsjolander.sprinkles.Transaction.OnTransactionCommittedListener;
import android.os.AsyncTask;

public abstract class Model {

	public interface OnSavedCallback {
		void onSaved();
	}

	public interface OnDeletedCallback {
		void onDeleted();
	}

	public boolean isValid() {
		// optionally implemented by subclass
		return true;
	}

	protected void beforeCreate() {
		// optionally implemented by subclass
	}

	protected void beforeSave() {
		// optionally implemented by subclass
	}

	protected void afterDelete() {
		// optionally implemented by subclass
	}

	final public boolean exists() {
		final Model m = Query.one(
				getClass(),
				String.format("SELECT * FROM %s WHERE %s LIMIT 1",
						Utils.getTableName(getClass()),
						Utils.getWhereStatement(this))).get();
		return m != null;
	}

	final public boolean save() {
		Transaction t = new Transaction();
		try {
			t.setSuccessful(save(t));
		} finally {
			t.finish();
		}
		return t.isSuccessful();
	}

	final public boolean save(Transaction t) {
		if (!isValid()) {
			return false;
		}

		beforeSave();
		if (exists()) {
			t.update(Utils.getTableName(getClass()),
					Utils.getContentValues(this), Utils.getWhereStatement(this));
		} else {
			beforeCreate();
			long id = t.insert(Utils.getTableName(getClass()),
					Utils.getContentValues(this));

			// set the @AutoIncrement column if one exists
			final List<ColumnField> columns = Utils.getColumns(getClass());
			for (ColumnField column : columns) {
				if (column.isAutoIncrementPrimaryKey) {
					column.field.setAccessible(true);
					try {
						column.field.set(this, id);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					break;
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

	final public void saveAsync() {
		saveAsync(null);
	}

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

	final public void delete() {
		Transaction t = new Transaction();
		try {
			delete(t);
			t.setSuccessful(true);
		} finally {
			t.finish();
		}
	}

	final public void delete(Transaction t) {
		t.delete(Utils.getTableName(getClass()), Utils.getWhereStatement(this));
		afterDelete();
	}

	final public void deleteAsync() {
		deleteAsync(null);
	}

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
