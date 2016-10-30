package se.emilsjolander.sprinkles;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.emilsjolander.sprinkles.exceptions.NoTypeSerializerFoundException;
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

    Context mContext;
    Map<Integer,List<Migration>> mMigrations = new ConcurrentHashMap<>();
    final DataResolver dataResolver;
    final Map<Class<? extends QueryResult>, ModelInfo> modelInfoCache = new HashMap<>();

    private String databaseName;
    private int initialDatabaseVersion;

    private Map<Class, TypeSerializer> typeSerializers = new ConcurrentHashMap<>();
    private SQLiteDatabase database;

    private Sprinkles() {
        addStandardTypeSerializers();
        dataResolver = new DataResolver(this);
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
        return init(context, "sprinkles.db", 1);
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
     * @return The Sprinkles instance.
     */
    public static synchronized Sprinkles init(Context context, String databaseName, int initialDatabaseVersion) {
        Sprinkles  sprinkles = new Sprinkles();
        sprinkles.mContext = context.getApplicationContext();
        sprinkles.databaseName = databaseName;
        sprinkles.initialDatabaseVersion = initialDatabaseVersion;
        return sprinkles;
    }

    /**
     * Throws SprinklesNotInitializedException if you try to access the database before initializing Sprinkles.
     * @return the SQL Database used by Sprinkles.
     */
    public synchronized SQLiteDatabase getDatabase() {
        if (database != null) {
            return database;
        }
        DbOpenHelper dbOpenHelper = new DbOpenHelper(this, mContext, databaseName, initialDatabaseVersion);
        database = dbOpenHelper.getWritableDatabase();
        return database;
    }

    /**
     * Used by unit tests to reset sprinkles instances between tests. This method can change at any time and should
     * never be called outside of a unit test.
     */
    public synchronized void clearCache() {
        modelInfoCache.clear();
        dataResolver.resetRecordCache();
    }

    /**
     * Add migrations to the underlying database. Every migration increments the database version.
     *
     * @param migration The migration that should be performed.
     */
    public void addMigration(Migration migration) {
        addMigration(migration, initialDatabaseVersion);
    }

    public void addMigration(Migration migration,int dbVersionCode) {
        if(mMigrations.get(dbVersionCode)==null){
            ArrayList<Migration> migrations = new ArrayList<>();
            mMigrations.put(dbVersionCode, migrations);
            migrations.add(migration);
        }else {
            mMigrations.get(dbVersionCode).add(migration);
        }
    }


    public <T> void registerType(Class<T> clazz, TypeSerializer<T> serializer) {
        typeSerializers.put(clazz, serializer);
    }

    public TypeSerializer getTypeSerializer(Class<?> type) {
        if (!typeSerializers.containsKey(type)) {
            throw new NoTypeSerializerFoundException(type);
        }
        return typeSerializers.get(type);
    }

}
