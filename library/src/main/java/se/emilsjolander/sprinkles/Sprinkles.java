package se.emilsjolander.sprinkles;

import android.accounts.Account;
import android.content.Context;
import android.database.ContentObserver;
import se.emilsjolander.sprinkles.exceptions.NoTypeSerializerFoundException;
import se.emilsjolander.sprinkles.typeserializers.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Sprinkles {

	static Sprinkles sInstance;

    Context mContext;
    List<Migration> mMigrations = new ArrayList<Migration>();
    Map<Class, ContentObserver> observers = new ConcurrentHashMap<Class, ContentObserver>();

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
    }

    /**
     *
     * Initialize sprinkles so queries and migrations can be performed
     *
     * @param context
     *      A context which is used for database operations. This context is not saved, however it's application context is.
     *
     * @return The singleton Sprinkles instance. Use this to add migrations.
     */
    public static Sprinkles init(Context context) {
        if (sInstance == null) {
            sInstance = new Sprinkles();
        }
        sInstance.mContext = context.getApplicationContext();
        return sInstance;
    }

    /**
     * Use init() instead.
     */
    @Deprecated
	public static Sprinkles getInstance(Context context) {
		return init(context);
	}

    /**
     * Used by unit tests to reset sprinkles instances between tests. This method can change at any time and should
     * never be called outside of a unit test.
     */
    public static void dropInstances() {
        sInstance = null;
        DbOpenHelper.sInstance = null;
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

    public <T> void registerType(Class<T> clazz, TypeSerializer<T> serializer) {
        typeSerializers.put(clazz, serializer);
    }

    TypeSerializer getTypeSerializer(Class<?> type) {
        if (!typeSerializers.containsKey(type)) {
            throw new NoTypeSerializerFoundException(type);
        }
        return typeSerializers.get(type);
    }

    public void addContentObserver(Class<? extends Model> clazz, Account account, String authority) {
        ContentObserver observer = SprinklesContentObserver.observer(account, authority);
        mContext.getContentResolver().registerContentObserver(Utils.getNotificationUri(clazz), true, observer);
        observers.put(clazz, observer);
    }

    public void removeContentObserver(Class<? extends Model> clazz) {
        ContentObserver observer = observers.get(clazz);
        if (observer != null) {
            mContext.getContentResolver().unregisterContentObserver(observer);
            observers.remove(clazz);
        }
    }
}
