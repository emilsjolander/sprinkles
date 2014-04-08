package se.emilsjolander.sprinkles.sample;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import se.emilsjolander.sprinkles.Migration;
import se.emilsjolander.sprinkles.Sprinkles;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		Sprinkles sprinkles = Sprinkles.init(getApplicationContext());

        sprinkles.addMigration(new Migration() {
            @Override
            protected void onPreMigrate() {
                // do nothing
            }

            @Override
            protected void doMigration(SQLiteDatabase db) {
                db.execSQL(
                        "CREATE TABLE Notes (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                                "content TEXT,"+
                                "created_at INTEGER,"+
                                "updated_at INTEGER"+
                        ")"
                );
                db.execSQL(
                        "CREATE TABLE Tags (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                                "name TEXT"+
                        ")"
                );
                db.execSQL(
                        "CREATE TABLE NoteTagLinks (" +
                                "note_id INTEGER,"+
                                "tag_id INTEGER,"+
                                "PRIMARY KEY(note_id, tag_id),"+
                                "FOREIGN KEY(note_id) REFERENCES Notes(id) ON DELETE CASCADE,"+
                                "FOREIGN KEY(tag_id) REFERENCES Tags(id) ON DELETE CASCADE"+
                        ")"
                );
            }

            @Override
            protected void onPostMigrate() {
                // do nothing
            }
        });
    }
}
