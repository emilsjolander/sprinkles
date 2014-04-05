package se.emilsjolander.sprinkles;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.emilsjolander.sprinkles.exceptions.NoTypeSerializerFoundException;
import se.emilsjolander.sprinkles.exceptions.SprinklesNotInitializedException;
import se.emilsjolander.sprinkles.typeserializers.BitmapSerializer;
import se.emilsjolander.sprinkles.typeserializers.BooleanSerializer;
import se.emilsjolander.sprinkles.typeserializers.DateSerializer;
import se.emilsjolander.sprinkles.typeserializers.DoubleSerializer;
import se.emilsjolander.sprinkles.typeserializers.FloatSerializer;
import se.emilsjolander.sprinkles.typeserializers.IntSerializer;
import se.emilsjolander.sprinkles.typeserializers.LongSerializer;
import se.emilsjolander.sprinkles.typeserializers.StringSerializer;
import se.emilsjolander.sprinkles.typeserializers.TypeSerializer;

public final class Sprinkles {

    static Sprinkles sInstance;
    static SQLiteDatabase sDatabase;

    Context mContext;
    List<Migration> mMigrations = new ArrayList<Migration>();

    private String databaseName;
    private int initialDatabaseVersion;

    private Map<Class, TypeSerializer> typeSerializers = new ConcurrentHashMap<Class, TypeSerializer>();

    private Sprinkles() {
        addStandardTypeSerializers();
    }

    private void addStandardTypeSerializers() {
        typeSerializers.put(int.class, new IntSerializer());
        typeSerializers.put(Integer.class, new IntSerializer());

        typeSerializers.put(long.class, new LongSerializer());
        typeSerializers.put(Long.class, new LongSerializer());

        typeSerializers.put(float.class, new FloatSerializer());
        typeSerializers.put(Float.class, new FloatSerializer());

        typeSerializers.put(double.class, new DoubleSerializer());
        typeSerializers.put(Double.class, new DoubleSerializer());

        typeSerializers.put(boolean.class, new BooleanSerializer());
        typeSerializers.put(Boolean.class, new BooleanSerializer());

        typeSerializers.put(String.class, new StringSerializer());
        typeSerializers.put(Date.class, new DateSerializer());
        typeSerializers.put(Bitmap.class, new BitmapSerializer());
    }

    /**
     * Initialize sprinkles so queries and migrations can be performed
     *
     * @param context A context which is used for database operations. This context is not saved, however it's application context is.
     * @return The singleton Sprinkles instance. Use this to add migrations.
     *
     * The default DB name is "sprinkles.db".
     */
    public static synchronized Sprinkles init(Context context) {
        return init(context, "sprinkles.db", 0);
    }

    /**
     *
     * Initialize sprinkles so queries can be performed
     *
     * @param context
     *      A context which is used for database operations. This context is not saved, however it's application context is.
     *
     * @param databaseName
     *     The name of the database to use.
     *     This is useful if you start to use Sprinkles with an app with an existing DB.
     *
     * @param initialDatabaseVersion
     *     The version of the existing database.
     *
     * @return The singleton Sprinkles instance.
     */
    public static synchronized Sprinkles init(Context context, String databaseName, int initialDatabaseVersion) {
        if (sInstance == null) {
            sInstance = new Sprinkles();
        }
        sInstance.mContext = context.getApplicationContext();
        sInstance.databaseName = databaseName;
        sInstance.initialDatabaseVersion = initialDatabaseVersion;
        return sInstance;
    }

    /**
     * Use init() instead.
     */
    @Deprecated
    public static synchronized Sprinkles getInstance(Context context) {
        return init(context);
    }

    /**
     * Throws SprinklesNotInitializedException if you try to access the database before initializing Sprinkles.
     * @return the SQL Database used by Sprinkles.
     */
    static synchronized SQLiteDatabase getDatabase() {
        if(sInstance == null) {
           throw new SprinklesNotInitializedException();
        }

        if(sDatabase == null) {
            DbOpenHelper dbOpenHelper = new DbOpenHelper(sInstance.mContext, sInstance.databaseName, sInstance.initialDatabaseVersion);
            sDatabase = dbOpenHelper.getWritableDatabase();
        }

        return sDatabase;
    }

    /**
     * Used by unit tests to reset sprinkles instances between tests. This method can change at any time and should
     * never be called outside of a unit test.
     */
    public static synchronized void dropInstances() {
        sInstance = null;
        sDatabase = null;
    }

    /**
     * Add migrations to the underlying database. Every migration increments the database version.
     *
     * @param migration The migration that should be performed.
     */
    public void addMigration(Migration migration) {
        mMigrations.add(migration);
    }

    public <T> void registerType(Class<T> clazz, TypeSerializer<T> serializer) {
        typeSerializers.put(clazz, serializer);
    }

    TypeSerializer getTypeSerializer(Class<?> type) {
        if (!typeSerializers.containsKey(type)) {
            throw new NoTypeSerializerFoundException(type);
        }
        return typeSerializers.get(type);
    }

}
