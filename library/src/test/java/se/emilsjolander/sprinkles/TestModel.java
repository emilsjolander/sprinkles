package se.emilsjolander.sprinkles;


import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.DynamicColumn;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("Tests")
public class TestModel extends Model {

    public static Migration MIGRATION = new Migration() {
        @Override
        protected void doMigration(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE Tests("+
                            "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                            "title TEXT,"+
                            "created_at INTEGER"+
                    ");"
            );
        }
    };

    @Key
    @AutoIncrement
    @Column("id") public long id;
    @Column("title") public String title;
    @Column("created_at") public Date createdAt;
    @DynamicColumn("count") public int count;

    public boolean valid = true;
    public boolean created;
    public boolean saved;
    public boolean deleted;

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void beforeCreate() {
        createdAt = new Date();
        created = true;
    }

    @Override
    public void beforeSave() {
        saved = true;
    }

    @Override
    public void afterDelete() {
        deleted = true;
    }

}