package com.lsjwzh.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lsjwzh.orm.exceptions.ContentValuesEmptyException;
import com.lsjwzh.orm.exceptions.IllegalOneToManyColumnException;
import com.lsjwzh.orm.exceptions.NoTypeSerializerFoundException;
import com.lsjwzh.orm.typeserializers.BitmapSerializer;
import com.lsjwzh.orm.typeserializers.BooleanSerializer;
import com.lsjwzh.orm.typeserializers.DateSerializer;
import com.lsjwzh.orm.typeserializers.DoubleSerializer;
import com.lsjwzh.orm.typeserializers.FloatSerializer;
import com.lsjwzh.orm.typeserializers.IntSerializer;
import com.lsjwzh.orm.typeserializers.LongSerializer;
import com.lsjwzh.orm.typeserializers.StringSerializer;
import com.lsjwzh.orm.typeserializers.TypeSerializer;

import rx.Observable;
import rx.Subscriber;

public final class Sprinkles {

    Context mContext;
    Map<Integer, List<Migration>> mMigrations = new ConcurrentHashMap<>();
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
     * <p>
     * The default DB name is "sprinkles.db".
     */
    public static synchronized Sprinkles init(Context context) {
        return init(context, "sprinkles.db", 1);
    }

    /**
     * Initialize sprinkles so queries can be performed
     *
     * @param context                A context which is used for database operations. This context is not saved, however it's application context is.
     * @param databaseName           The name of the database to use.
     *                               This is useful if you start to use Sprinkles with an app with an existing DB.
     * @param initialDatabaseVersion The version of the existing database.
     * @return The Sprinkles instance.
     */
    public static synchronized Sprinkles init(Context context, String databaseName, int initialDatabaseVersion) {
        Sprinkles sprinkles = new Sprinkles();
        sprinkles.mContext = context.getApplicationContext();
        sprinkles.databaseName = databaseName;
        sprinkles.initialDatabaseVersion = initialDatabaseVersion;
        return sprinkles;
    }

    /**
     * Throws SprinklesNotInitializedException if you try to access the database before initializing Sprinkles.
     *
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

    public void addMigration(Migration migration, int dbVersionCode) {
        if (mMigrations.get(dbVersionCode) == null) {
            ArrayList<Migration> migrations = new ArrayList<>();
            mMigrations.put(dbVersionCode, migrations);
            migrations.add(migration);
        } else {
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


    //////////////////////////Model Operation////////////////////////////

    /**
     * Check whether this model exists in the database
     *
     * @return true if this model is currently saved in the database (could be an older version)
     */
    final public boolean exists(Model model) {
        if (!dataResolver.isTableExist(ModelInfo.from(this, model.getClass()))) {
            return false;
        }
        return getOlderModel(model) != null;
    }

    /**
     * get an older version of this model exists in the database
     *
     * @return true if this model is currently saved in the database (could be an older version)
     */
    final Model getOlderModel(Model model) {
        if (!dataResolver.isTableExist(ModelInfo.from(this, model.getClass()))) {
            return null;
        }
        try {
            return Query.one(this,
                    model.getClass(),
                    String.format("SELECT * FROM %s WHERE %s LIMIT 1",
                            DataResolver.getTableName(model.getClass()),
                            Utils.getWhereStatement(this, model))).get();
        } catch (SQLiteException e) {
            // We can not guarantee getOlderModel will be call safety. See {@TransactionTest.rollback}
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save this model to the database.
     * If this model has an @AutoIncrement annotation on a property
     * than that property will be set when this method returns.
     *
     * @return whether or not the save was successful.
     */
    final public boolean save(Model model) {
        Transaction t = new Transaction(this);
        try {
            t.setSuccessful(save(model, t));
        } finally {
            t.finish();
        }
        return t.isSuccessful();
    }

    /**
     * Save this model to the database within the given transaction.
     * If this model has an @AutoIncrement annotation on a property
     * than that property will be set when this method returns.
     *
     * @param t The transaction to save this model in
     * @return whether or not the save was successful.
     */
    final public boolean save(Model model, Transaction t) {
        return save(model, t, true);
    }

    /**
     * Save this model to the database within the given transaction.
     * If this model has an @AutoIncrement annotation on a property
     * than that property will be set when this method returns.
     *
     * @param t          The transaction to save this model in
     * @param checkOlder whether to check and update older model
     * @return whether or not the save was successful.
     */
    final public boolean save(final Model model, Transaction t, boolean checkOlder) {
        checkRelationship(model);
        ModelInfo table = ModelInfo.from(this, model.getClass());
        dataResolver.assureTableExist(table);
        Model cachedModel = dataResolver.getCachedModel(model.getClass(), dataResolver.getKeyValueTag(model));
        if (checkOlder && !model.equals(cachedModel)) {
            //if the model has been cached,just update the older model and update the order model to db
//            throw new IllegalStateException(""+DataResolver.getKeyValueTag(olderModel));
            if (cachedModel != model && cachedModel != null) {
                //sync changes to older model
                copyTo(model, cachedModel, new Model.IFieldCopyAction() {
                    @Override
                    public void doCopy(ModelInfo.ColumnField columnField, Object from, Object to) {
                        try {
                            //if field is a lazy load field, skip it
                            if (LazyModel.class.isAssignableFrom(columnField.field.getType())
                                    || LazyModelList.class.isAssignableFrom(columnField.field.getType())) {
                                return;
                            }
                            columnField.field.setAccessible(true);
                            Object valueInThis = columnField.field.get(from);
                            Object valueInTarget = columnField.field.get(to);
                            //if field is One2Many Field,then check the modellist
                            //if the modellist is empty,add modellist of this model to target model
                            if (columnField instanceof ModelInfo.OneToManyColumnField
                                    && valueInTarget instanceof ModelList) {
                                if (((ModelList) valueInTarget).size() == 0) {
                                    ((ModelList) valueInTarget).addAll(((ModelList) valueInThis));
                                }
                            }
                            //if the target has not null value , skip it or over write it
                            if (valueInTarget != null && !valueInTarget.equals(valueInThis)) {
                                columnField.field.set(to, valueInThis);
                            } else {
                                columnField.field.set(to, valueInThis);
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return save(cachedModel, t, false);
            }
        }

        boolean doesExist = exists(model);
        if (!doesExist) {
            // do something ??
        }

        final ContentValues cv = Utils.getContentValues(this, model);
        if (cv.size() == 0) {
            throw new ContentValuesEmptyException();
        }
//        final String tableName = Utils.getTableName(getClass());

        if (doesExist) {
            if (t.update(table, cv, Utils.getWhereStatement(this, model)) == 0) {
                return false;
            }
        } else {
            long id = t.insert(table, cv);
            if (id == -1) {
                return false;
            }

            // set the @AutoIncrement column if one exists
            if (table.autoIncrementField != null) {
                table.autoIncrementField.field.setAccessible(true);
                try {
                    if (table.autoIncrementField.field.getType() == Integer.class) {
                        table.autoIncrementField.field.set(model, (int) id);
                    } else {
                        table.autoIncrementField.field.set(model, id);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        t.addOnTransactionCommittedListener(new Transaction.OnTransactionCommittedListener() {

            @Override
            public void onTransactionCommitted() {
                dataResolver.updateRecordCache(model);
                mContext.getContentResolver().notifyChange(
                        Utils.getNotificationUri(model.getClass()), null, true);
            }

            @Override
            public void onTransactionRollback() {
                dataResolver.removeRecordCache(model);
            }
        });

        return true;
    }


    /**
     * Delete this model
     */
    final public boolean delete(Model model) {
        Transaction t = new Transaction(this);
        try {
            t.setSuccessful(delete(model, t));
        } finally {
            t.finish();
        }
        return t.isSuccessful();
    }

    /**
     * Delete this model within the given transaction
     *
     * @param t The transaction to delete this model in
     */
    final public boolean delete(final Model model, Transaction t) {
        t.delete(ModelInfo.from(this, model.getClass()), Utils.getWhereStatement(this, model));
        t.addOnTransactionCommittedListener(new Transaction.OnTransactionCommittedListener() {

            @Override
            public void onTransactionCommitted() {
                dataResolver.removeRecordCache(model);
                mContext.getContentResolver().notifyChange(
                        Utils.getNotificationUri(model.getClass()), null);
            }

            @Override
            public void onTransactionRollback() {

            }
        });
        return true;
    }


    /**
     * Copy this model to another model.
     */
    final public void copyTo(Model originModel, Model targetModel) {
        copyTo(originModel, targetModel, new Model.IFieldCopyAction() {
            @Override
            public void doCopy(ModelInfo.ColumnField columnField, Object from, Object to) {
                try {
                    columnField.field.setAccessible(true);
                    Object valueInThis = columnField.field.get(from);
                    Object valueInTarget = columnField.field.get(to);
                    //if the target has not null value , skip it or over write it
                    if (valueInTarget != null && !valueInTarget.equals(valueInThis)) {
                        columnField.field.set(to, valueInThis);
                    } else {
                        columnField.field.set(to, valueInThis);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Copy this model to another model.
     */
    final public void copyTo(Model originModel, Model targetModel, Model.IFieldCopyAction fieldCopyAction) {
        ModelInfo table = ModelInfo.from(this, originModel.getClass());
        //copy normal columns
        for (ModelInfo.ColumnField columnField : table.columns) {
            if (fieldCopyAction != null) {
                fieldCopyAction.doCopy(columnField, originModel, targetModel);
            }
        }
        ;
        //copy  manyToOneColumns
        for (ModelInfo.ColumnField columnField : table.manyToOneColumns) {
            if (fieldCopyAction != null) {
                fieldCopyAction.doCopy(columnField, originModel, targetModel);
            }
        }
        ;
        //copy  oneToManyColumns
        for (ModelInfo.ColumnField columnField : table.oneToManyColumns) {
            if (fieldCopyAction != null) {
                fieldCopyAction.doCopy(columnField, originModel, targetModel);
            }
        }
    }


    public <E extends Model> boolean saveAll(ModelList<E> modelList) {
        Transaction t = new Transaction(this);
        try {
            t.setSuccessful(saveAll(modelList, t));
        } finally {
            t.finish();
        }

        return t.isSuccessful();
    }

    public <E extends Model> boolean saveAll(ModelList<E> modelList, Transaction t) {
        for (Model m : modelList) {
            if (!save(m, t)) {
                return false;
            }
        }

        return true;
    }

    public <E extends Model> boolean deleteAll(ModelList<E> modelList) {
        Transaction t = new Transaction(this);
        try {
            t.setSuccessful(deleteAll(modelList, t));
        } finally {
            t.finish();
        }
        return t.isSuccessful();
    }

    public <E extends Model> boolean deleteAll(ModelList<E> modelList, Transaction t) {
        for (Model m : modelList) {
            if (!delete(m, t)) {
                return false;
            }
        }
        return true;
    }

    private void checkRelationship(Model model) {
        // TODO remove this
        try {
            //check relationship of model
            final ModelInfo info = ModelInfo.from(this, model.getClass());
            for (ModelInfo.OneToManyColumnField columnField : info.oneToManyColumns) {
                columnField.field.setAccessible(true);
                if (columnField.field.get(model) != null) {
                    continue;
                }
                Class one2ManyContainerType = columnField.field.getType();
                if (LazyModelList.class.isAssignableFrom(one2ManyContainerType)) {
                    columnField.field.set(model, new LazyModelList(this, columnField.manyModelClass, model, columnField));
                } else if (ModelList.class.isAssignableFrom(one2ManyContainerType)) {
                    columnField.field.set(model, one2ManyContainerType.newInstance());
                } else {
                    throw new IllegalOneToManyColumnException();
                }
            }
            for (ModelInfo.ManyToOneColumnField columnField : info.manyToOneColumns) {
                columnField.field.setAccessible(true);
                Class one2ManyContainerType = columnField.field.getType();
                if (LazyModel.class.isAssignableFrom(one2ManyContainerType)) {
                    columnField.field.set(model, new LazyModel(this, columnField.oneModelClass, model, columnField));
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
