package se.emilsjolander.sprinkles;

import android.database.Cursor;
import android.os.AsyncTask;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public class CursorList<T extends Model> implements Iterable<T>, Closeable {

	private static final long serialVersionUID = 9111033070491580889L;

    public interface OnAllSavedCallback {
		void onAllSaved();
	}

	public interface OnAllDeletedCallback {
		void onAllDeleted();
	}

    private Cursor cursor;
    private Class<T> type;
    private boolean closed;

    CursorList(Cursor cursor, Class<T> type) {
        this.cursor = cursor;
        this.type = type;
    }

    public int size() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public T get(int pos) {
        requireOpen();
        cursor.moveToPosition(pos);
        return Utils.getModelFromCursor(type, cursor);
    }

    @Override
    public Iterator<T> iterator() {
        return new CursorIterator(cursor, type);
    }

    @Override
    public void close() throws IOException {
        cursor.close();
        closed = true;
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("Cannot call methods on a closed CursorList");
        }
    }

    public boolean saveAll() {
        requireOpen();
		Transaction t = new Transaction();
		try {
			t.setSuccessful(saveAll(t));
		} finally {
			t.finish();
		}
		return t.isSuccessful();
	}

	public boolean saveAll(Transaction t) {
        requireOpen();
		for (Model m : this) {
			if (!m.save(t)) {
				return false;
			}
		}
		return true;
	}

	public void saveAllAsync() {
        requireOpen();
		saveAllAsync(null);
	}

	public void saveAllAsync(final OnAllSavedCallback callback) {
        requireOpen();
		new AsyncTask<Void, Void, Boolean>() {

			protected Boolean doInBackground(Void... params) {
				return saveAll();
			}

			protected void onPostExecute(Boolean result) {
				if (result && callback != null) {
					callback.onAllSaved();
				}
			}

		}.execute();
	}

	public void deleteAll() {
        requireOpen();
		Transaction t = new Transaction();
		try {
			deleteAll(t);
			t.setSuccessful(true);
		} finally {
			t.finish();
		}
	}

	public void deleteAll(Transaction t) {
        requireOpen();
		for (Model m : this) {
			m.delete(t);
		}
	}

	public void deleteAllAsync() {
        requireOpen();
		deleteAllAsync(null);
	}

	public void deleteAllAsync(final OnAllDeletedCallback callback) {
        requireOpen();
		new AsyncTask<Void, Void, Void>() {

			protected Void doInBackground(Void... params) {
				deleteAll();
				return null;
			}

			protected void onPostExecute(Void result) {
				if (callback != null) {
					callback.onAllDeleted();
				}
			}

		}.execute();
	}

}
