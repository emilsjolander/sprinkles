package se.emilsjolander.sprinkles;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.models.TestModel;
import se.emilsjolander.sprinkles.models.UniqueTestModel;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class MigrationTest {

    @Test
    public void createTable() {
        Migration m = new Migration();
        m.createTable(TestModel.class);
        assertEquals(m.mStatements.get(m.mStatements.size() - 1),
                "CREATE TABLE Tests(title TEXT, id INTEGER PRIMARY KEY AUTOINCREMENT);"
        );
    }

    @Test
    public void createTableWithUniqueColumn() {
        Migration m = new Migration();
        m.createTable(UniqueTestModel.class);
        assertEquals(m.mStatements.get(m.mStatements.size() - 1),
                "CREATE TABLE UniqueTests(name TEXT UNIQUE, id INTEGER PRIMARY KEY AUTOINCREMENT);"
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
    public void addRawStatement() {
        Migration m = new Migration();
        m.addRawStatement("my little pony, this should not be changed.");
        assertEquals(m.mStatements.get(m.mStatements.size() - 1),
                "my little pony, this should not be changed."
        );
    }

}
