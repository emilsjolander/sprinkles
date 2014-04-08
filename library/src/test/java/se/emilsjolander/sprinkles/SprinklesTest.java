package se.emilsjolander.sprinkles;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.exceptions.NoTypeSerializerFoundException;
import se.emilsjolander.sprinkles.typeserializers.SqlType;
import se.emilsjolander.sprinkles.typeserializers.TypeSerializer;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SprinklesTest {

    class MyObject {}
    class MyObjectSerializer implements TypeSerializer<MyObject> {

        @Override
        public MyObject unpack(Cursor c, String name) {
            return null;
        }

        @Override
        public void pack(MyObject object, ContentValues cv, String name) {

        }

        @Override
        public String toSql(MyObject object) {
            return null;
        }

        @Override
        public SqlType getSqlType() {
            return null;
        }
    }


    @Before
    public void reset() {
        Sprinkles.dropInstances();
    }

    @Test
    public void init() {
        assertNull(Sprinkles.sInstance);
        Sprinkles s1 = Sprinkles.init(Robolectric.application);
        assertNotNull(Sprinkles.sInstance);
        Sprinkles s2 = Sprinkles.init(Robolectric.application);
        assertEquals(s1, s2);
    }

    @Test
    public void addMigration() {
        Sprinkles s = Sprinkles.init(Robolectric.application);
        assertEquals(s.mMigrations.size(), 0);
        Migration m = new Migration(){
            @Override
            protected void doMigration(SQLiteDatabase db) {

            }
        };
        s.addMigration(m);
        assertEquals(s.mMigrations.get(0), m);
    }

    @Test
    public void registerType() {
        Sprinkles s = Sprinkles.init(Robolectric.application);
        TypeSerializer serializer = new MyObjectSerializer();
        s.registerType(MyObject.class, serializer);
        assertEquals(s.getTypeSerializer(MyObject.class), serializer);
    }

    @Test(expected = NoTypeSerializerFoundException.class)
    public void getNonExistingTypeSerializer() {
        Sprinkles s = Sprinkles.init(Robolectric.application);
        s.getTypeSerializer(MyObject.class);

    }

}
