package se.emilsjolander.sprinkles;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class Sprinkles {

	static Sprinkles sInstance;
	Context mContext;
	List<Migration> mMigrations = new ArrayList<Migration>();

	private Sprinkles() {
		// do nothing
	}

    /**
     *
     * @param context
     *      A context which is used for database operations. This context is not saved, however it's application context is.
     *
     * @return The singleton Sprinkles instance. Use this to add migrations.
     */
	public static Sprinkles getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new Sprinkles();
		}
		sInstance.mContext = context.getApplicationContext();
		return sInstance;
	}

    /**
     * Add migrations to the underlying database. Every migration increments the database version.
     *
     * @param migration
     *      The migration that should be performed.
     */
	public void addMigration(Migration migration) {
		mMigrations.add(migration);
	}

}
