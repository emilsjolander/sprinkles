package se.emilsjolander.sprinkles;

import android.database.Cursor;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import se.emilsjolander.sprinkles.annotations.Table;
import se.emilsjolander.sprinkles.exceptions.NoTableAnnotationException;

/**
 * Created by panwenye on 14-10-12.
 */
public class DataResolver {
    /**
     * when the number of cached records for specific model is larger than RECORD_CACHE_LIMIT,recycleRecordCache method will be triggered
     */
    public static final int  RECORD_CACHE_LIMIT = 100;
    static Hashtable<Class,Hashtable<String,WeakReference<Object>>> sCachePool = new Hashtable<Class, Hashtable<String, WeakReference<Object>>>();

    /**
     * update record in cache.usually be called after save or update data
     * @param m
     */
    static void updateRecordCache(Model m){
        Hashtable<String, WeakReference<Object>> cacheForModel = sCachePool.get(m.getClass());
        if(cacheForModel==null){
            cacheForModel = new Hashtable<String, WeakReference<Object>>();
            sCachePool.put(m.getClass(), cacheForModel);
        }
        if(cacheForModel.size()>RECORD_CACHE_LIMIT){
            recycleRecordCache(m.getClass());
        }
        cacheForModel.put(getKeyValueTag(m),new WeakReference<Object>(m));
    }

    static void removeRecordCache(Model m){
        Hashtable<String, WeakReference<Object>> cacheForModel = sCachePool.get(m.getClass());
        if(cacheForModel!=null){
            cacheForModel.remove(getKeyValueTag(m));
        }
    }


    /**
     * remove useless record from cache
     * @param modelClazz
     */
    static void recycleRecordCache(Class modelClazz){
        Hashtable<String, WeakReference<Object>> cacheForModel = sCachePool.get(modelClazz);
        if(cacheForModel !=null){
            for (String key : cacheForModel.keySet()){
                if(cacheForModel.get(key)==null
                        ||cacheForModel.get(key).get()==null){
                    cacheForModel.remove(key);
                }
            }
        }
    }


    public static <T extends QueryResult> T getResultFromCursor(Class<T> resultClass, Cursor c) {
        try {
            if(sCachePool.get(resultClass)==null){
                sCachePool.put(resultClass, new Hashtable<String, WeakReference<Object>>());
            }
            Hashtable<String,WeakReference<Object>> cacheForModel = sCachePool.get(resultClass);
            final ModelInfo info = ModelInfo.from(resultClass);
            String keyValueTag = getKeyValueTag(info, c);
            if(cacheForModel.containsKey(keyValueTag)&&cacheForModel.get(keyValueTag)!=null){
                return (T)cacheForModel.get(keyValueTag).get();
            }

            T result = resultClass.newInstance();
            List<String> colNames = Arrays.asList(c.getColumnNames());
            for (ModelInfo.ColumnField column : info.columns) {
                if (!colNames.contains(column.name)) {
                    continue;
                }
                column.field.setAccessible(true);
                final Class<?> type = column.field.getType();
                Object o = Sprinkles.sInstance.getTypeSerializer(type).unpack(c, column.name);
                column.field.set(result, o);
            }
            //fill oneToMany field
            for (ModelInfo.OneToManyColumnField oneToManyColumnField : info.oneToManyColumns) {
                if (!colNames.contains(oneToManyColumnField.oneColumn)) {
                    continue;
                }
                final ManyQuery query = new ManyQuery();
                query.resultClass = oneToManyColumnField.manyModelClass;
                query.placeholderQuery = "SELECT * FROM " + getTableName(oneToManyColumnField.manyModelClass)
                        + " WHERE "+oneToManyColumnField.manyColumn+"=?";
                Integer foreignKeyValue = c.getInt(c.getColumnIndexOrThrow(oneToManyColumnField.oneColumn));
                query.rawQuery = Utils.insertSqlArgs(query.placeholderQuery,new Object[]{foreignKeyValue});
                ModelList manyModels = ModelList.from(query.get());
                if(manyModels!=null) {
                    try {
                        oneToManyColumnField.field.set(result, manyModels);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            //fill manyToOne field
            for (ModelInfo.ManyToOneColumnField manyToOneColumnField : info.manyToOneColumns) {
                if (!colNames.contains(manyToOneColumnField.manyColumn)) {
                    continue;
                }
                final OneQuery query = new OneQuery();
                query.resultClass = manyToOneColumnField.field.getType();
                query.placeholderQuery = "SELECT * FROM " + getTableName(query.resultClass)
                        + " WHERE "+manyToOneColumnField.oneColumn+"=?";
                Integer foreignKeyValue = c.getInt(c.getColumnIndexOrThrow(manyToOneColumnField.manyColumn));
                query.rawQuery = Utils.insertSqlArgs(query.placeholderQuery,new Object[]{foreignKeyValue});
                Object oneModel = query.get();
                if(oneModel!=null) {
                    try {
                        manyToOneColumnField.field.set(result, oneModel);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * concat the key values of a model to represent single record
     * @param m
     * @return
     */
    public static String getKeyValueTag(Model m){
        final ModelInfo info = ModelInfo.from(m.getClass());
        final StringBuilder keyValuesTag = new StringBuilder();
        for (ModelInfo.ColumnField column : info.columns) {
            if(column.isKey||column.isDynamic) {
                column.field.setAccessible(true);
                try {
                    keyValuesTag.append(column.field.get(m) + "_");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return keyValuesTag.toString();
    }

    /**
     * concat the key values of a model to represent single record
     * @param c
     * @return
     */
    public static String getKeyValueTag(ModelInfo info,Cursor c){
        final StringBuilder keyValuesTag = new StringBuilder();
        for (ModelInfo.ColumnField column : info.columns) {
            if(column.isKey||column.isDynamic) {
                if(c.getColumnIndex(column.name)>=0) {
                    column.field.setAccessible(true);
                    keyValuesTag.append(c.getString(c.getColumnIndex(column.name)) + "_");
                }
            }
        }
        return keyValuesTag.toString();
    }


    static String getTableName(Class<? extends Model> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            String tabName = clazz.getAnnotation(Table.class).value();
            return TextUtils.isEmpty(tabName) ? clazz.getName().replace(".","_") : tabName;
        }
        throw new NoTableAnnotationException();
    }

    static void assureTableExist(ModelInfo table) {
        synchronized (table) {
            if (!isTableExist(table)) {
                String sql = Utils.getCreateTableSQL(table);
                Sprinkles.getDatabase().execSQL(sql);
            }
        }
    }

    /**
     * check is table exist
     *
     * @param table
     * @return
     */
    static boolean isTableExist(ModelInfo table) {
        if (table.isTableChecked) {
            return true;
        }

        Cursor cursor = null;
        try {
            String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"
                    + table.tableName + "' ";
            cursor = Sprinkles.getDatabase().rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    table.isTableChecked = true;
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }

        return false;

    }

}
