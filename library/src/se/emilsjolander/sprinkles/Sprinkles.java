package se.emilsjolander.sprinkles;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class Sprinkles {

	static Sprinkles sInstance;
	Context mContext;
	List<Migration> mMigrations = new ArrayList<Migration>();

	private Sprinkles() {
		// do nothing
	}

	public static Sprinkles getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new Sprinkles();
		}
		sInstance.mContext = context.getApplicationContext();
		return sInstance;
	}

	public void addMigration(Migration migration) {
		mMigrations.add(migration);
	}

}
