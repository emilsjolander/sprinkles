package se.emilsjolander.sprinkles;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;
import se.emilsjolander.sprinkles.annotations.Unique;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class MigrationTest {

    @Table("Tests")
    public static class TestModel extends Model {

        @AutoIncrementPrimaryKey
        @Column("id") private long id;

        @Column("title")
        private String title;

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    @Table("UniqueTests")
    public static class UniqueTestModel extends Model {

        @AutoIncrementPrimaryKey
        @Column("id") private long id;

        @Unique
        @Column("title")
        private String title;

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    @Test
    public void createTable() {
        Migration m = new Migration();
        m.createTable(TestModel.class);
        assertEquals(m.mStatements.get(m.mStatements.size() - 1),
                "CREATE TABLE Tests(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT);"
        );
    }

    @Test
    public void createTableWithUniqueColumn() {
        Migration m = new Migration();
        m.createTable(UniqueTestModel.class);
        assertEquals(m.mStatements.get(m.mStatements.size() - 1),
                "CREATE TABLE UniqueTests(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT UNIQUE);"
        );
    }

    @Test
    public void dropTable() {
        Migration m = new Migration();
        m.dropTable(TestModel.class);
        assertEquals(m.mStatements.get(m.mStatements.size() - 1),
                "DROP TABLE IF EXISTS Tests;"
        );
    }

    @Test
    public void renameTable() {
        Migration m = new Migration();
        m.renameTable("old", "new");
        assertEquals(m.mStatements.get(m.mStatements.size() - 1),
                "ALTER TABLE old RENAME TO new;"
        );
    }

    @Test
    public void addColumn() {
        Migration m = new Migration();
        m.addColumn(TestModel.class, "title");
        assertEquals(m.mStatements.get(m.mStatements.size() - 1),
                "ALTER TABLE Tests ADD COLUMN title TEXT;"
        );
    }

    @Test
    public void addIndex() {
        Migration m = new Migration();
        m.createIndex("TitleIndex", true, TestModel.class, "title");
        assertEquals(m.mStatements.get(m.mStatements.size() - 1),
                "CREATE UNIQUE INDEX TitleIndex ON Tests(title);"
        );
    }

    @Test
    public void dropIndex() {
        Migration m = new Migration();
        m.dropIndex("TitleIndex");
        assertEquals(m.mStatements.get(m.mStatements.size() - 1),
                "DROP INDEX IF EXISTS TitleIndex;"
        );
    }

    @Test
    public void addRawStatement() {
        Migration m = new Migration();
        m.addRawStatement("my little pony, this should not be changed.");
        assertEquals(m.mStatements.get(m.mStatements.size() - 1),
                "my little pony, this should not be changed."
        );
    }

}
