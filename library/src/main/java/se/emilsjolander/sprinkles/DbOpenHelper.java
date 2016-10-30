package se.emilsjolander.sprinkles;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;
import java.util.Map;

class DbOpenHelper extends SQLiteOpenHelper {
    private final Sprinkles sprinkles;
    private int baseVersion;

    protected DbOpenHelper(Sprinkles sprinkles, Context context, String databaseName, int baseVersion) {
        super(context, databaseName, null, baseVersion);
        this.baseVersion = baseVersion;
        this.sprinkles = sprinkles;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        executeMigrations(db, baseVersion, baseVersion);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        executeMigrations(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    private void executeMigrations(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Map.Entry<Integer, List<Migration>> entry : sprinkles.mMigrations.entrySet()) {
            if ((entry.getKey() > oldVersion||oldVersion==newVersion)
                    && entry.getValue() != null) {
                int size = entry.getValue().size();
                for (int j = 0; j < size; j++) {
                    if (entry.getValue().get(j) != null) {
                        try {
                            entry.getValue().get(j).execute(db);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
