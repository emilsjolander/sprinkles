package se.emilsjolander.sprinkles;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collection;

public class ModelList<E extends Model> extends ArrayList<E> {

    private static final long serialVersionUID = 9111033070491580889L;
    private final Sprinkles sprinkles;

    public interface OnAllSavedCallback {
        void onAllSaved();
    }

    public interface OnAllDeletedCallback {
        void onAllDeleted();
    }

    public static <E extends Model> ModelList<E> from(Sprinkles sprinkles, CursorList<E> cursorList) {
        return new ModelList<E>(sprinkles, cursorList.asList());
    }
    public static <E extends Model> ModelList<E> from(Sprinkles sprinkles, CursorList<E> cursorList,int skip) {
        ModelList<E> modelList = new ModelList<E>(sprinkles, cursorList.size()>skip?cursorList.size() - skip:0);
        for (int i = skip; i < cursorList.size(); i++) {
            modelList.add(cursorList.get(i));
        }
        return modelList;
    }

    public ModelList(Sprinkles sprinkles) {
        super();
        this.sprinkles = sprinkles;
    }

    public ModelList(Sprinkles sprinkles, int capacity) {
        super(capacity);
        this.sprinkles = sprinkles;
    }

    public ModelList(Sprinkles sprinkles, Collection<? extends E> collection) {
        super(collection);
        this.sprinkles = sprinkles;
    }

    public boolean saveAll() {
        Transaction t = new Transaction(sprinkles);
        try {
            t.setSuccessful(saveAll(t));
        } finally {
            t.finish();
        }

        return t.isSuccessful();
    }

    public boolean saveAll(Transaction t) {
        for (Model m : this) {
            if (!m.save(t)) {
                return false;
            }
        }

        return true;
    }

    public void saveAllAsync() {
        saveAllAsync(null);
    }

    public void saveAllAsync(final OnAllSavedCallback callback) {
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
        Transaction t = new Transaction(sprinkles);
        try {
            deleteAll(t);
            t.setSuccessful(true);
        } finally {
            t.finish();
        }
    }

    public void deleteAll(Transaction t) {
        for (Model m : this) {
            m.delete(t);
        }
    }

    public void deleteAllAsync() {
        deleteAllAsync(null);
    }

    public void deleteAllAsync(final OnAllDeletedCallback callback) {
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
