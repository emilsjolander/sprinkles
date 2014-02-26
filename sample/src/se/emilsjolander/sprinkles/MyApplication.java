package se.emilsjolander.sprinkles;

import android.app.Application;
import se.emilsjolander.sprinkles.models.Note;
import se.emilsjolander.sprinkles.models.NoteTagLink;
import se.emilsjolander.sprinkles.models.Tag;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		Sprinkles sprinkles = Sprinkles.init(getApplicationContext());
		
		Migration initialMigration = new Migration();
		initialMigration.createTable(Note.class);
		initialMigration.createTable(Tag.class);
		initialMigration.createTable(NoteTagLink.class);
		sprinkles.addMigration(initialMigration);
    }
}
