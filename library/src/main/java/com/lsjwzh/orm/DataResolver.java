package com.lsjwzh.orm;

import android.database.Cursor;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.lsjwzh.orm.annotations.Table;
import com.lsjwzh.orm.exceptions.NoTableAnnotationException;

/**
 * Created by panwenye on 14-10-12.
 */
class DataResolver {
    /**
     * when the number of cached records for specific model is larger than RECORD_CACHE_LIMIT,recycleRecordCache method will be triggered
     */
    public  final int  RECORD_CACHE_LIMIT = 100;
    // use map instead!
    Hashtable<Class,Hashtable<String,WeakReference<Object>>> cachePool = new Hashtable<>();
    Sprinkles sprinkles;

    DataResolver(Sprinkles sprinkles) {
        this.sprinkles = sprinkles;
    }
    /**
     * update record in cache.usually be called after save or update data
     * @param m
     */
    void updateRecordCache(Model m){
        synchronized (m) {
            Hashtable<String, WeakReference<Object>> cacheForModel = cachePool.get(m.getClass());
            if (cacheForModel == null) {
                cacheForModel = new Hashtable<>();
                cachePool.put(m.getClass(), cacheForModel);
            }
            if (cacheForModel.size() > RECORD_CACHE_LIMIT) {
                recycleRecordCache(m.getClass());
            }
            cacheForModel.put(getKeyValueTag(m), new WeakReference<Object>(m));
        }
    }

    void removeRecordCache(Model m){
        synchronized (m) {
            Hashtable<String, WeakReference<Object>> cacheForModel = cachePool.get(m.getClass());
            if (cacheForModel != null) {
                cacheForModel.remove(getKeyValueTag(m));
            }
        }
    }
    void resetRecordCache() {
        cachePool.clear();
    }

    /**
     * get the cached model
     * @param keyTag
     * @return
     */
    public Model getCachedModel(Class modelClass,String keyTag){
        synchronized (modelClass) {
            if (cachePool.get(modelClass) == null) {
                cachePool.put(modelClass, new Hashtable<String, WeakReference<Object>>());
            }
            Hashtable<String, WeakReference<Object>> cacheForModel = cachePool.get(modelClass);
            if (cacheForModel.containsKey(keyTag)
                    && cacheForModel.get(keyTag) != null) {
                return (Model)cacheForModel.get(keyTag).get();
            }
            return null;
        }
    }



    /**
         * remove useless record from cache
         * @param modelClazz
         */
    void recycleRecordCache(Class modelClazz){
        Hashtable<String, WeakReference<Object>> cacheForModel = cachePool.get(modelClazz);
        if(cacheForModel !=null){
            for (Object key : cacheForModel.keySet().toArray()){
                if(cacheForModel.get(key)==null
                        ||cacheForModel.get(key).get()==null){
                    cacheForModel.remove(key);
                }
            }
        }
    }


    public <T extends QueryResult> T getResultFromCursor(Class<T> resultClass, Cursor c) {
        try {
            if(cachePool.get(resultClass)==null){
                cachePool.put(resultClass, new Hashtable<String, WeakReference<Object>>());
            }
            Hashtable<String,WeakReference<Object>> cacheForModel = cachePool.get(resultClass);
            final ModelInfo info = ModelInfo.from(sprinkles, resultClass);
            String keyValueTag = getKeyValueTag(info, c);
            if(cacheForModel.containsKey(keyValueTag)
                    &&cacheForModel.get(keyValueTag)!=null){
                if(cacheForModel.get(keyValueTag).get()!=null) {
                    return (T) cacheForModel.get(keyValueTag).get();
                }
            }

            T result = resultClass.newInstance();

            List<String> colNames = Arrays.asList(c.getColumnNames());
            for (ModelInfo.ColumnField column : info.columns) {
                if (!colNames.contains(column.name)) {
                    continue;
                }
                column.field.setAccessible(true);
                final Class<?> type = column.field.getType();
                Object o = sprinkles.getTypeSerializer(type).unpack(c, column.name);
                column.field.set(result, o);
            }
            /**
             * call updateRecordCache before fill o2m and m2o fields,to avoid stackoverflow error when has circular relation
             * class Person{
             *     ...
             *   @ManyToOne
             *   public Email email;
             * }
             * *class Email{
             *     ...
             *   @ManyToOne
             *   public Person owner;
             * }
             */
            updateRecordCache((Model)result);
            //fill oneToMany field
            for (ModelInfo.OneToManyColumnField oneToManyColumnField : info.oneToManyColumns) {
                if (!colNames.contains(oneToManyColumnField.oneColumn)) {
                    continue;
                }
                if(!LazyModelList.class.isAssignableFrom(oneToManyColumnField.field.getType())){
                    final ManyQuery query = new ManyQuery(sprinkles);
                    query.resultClass = oneToManyColumnField.manyModelClass;
                    query.placeholderQuery = "SELECT * FROM " + getTableName(oneToManyColumnField.manyModelClass)
                            + " WHERE " + oneToManyColumnField.manyColumn + "=?";
                    Integer foreignKeyValue = c.getInt(c.getColumnIndexOrThrow(oneToManyColumnField.oneColumn));
                    query.rawQuery = Utils.insertSqlArgs(sprinkles, query.placeholderQuery, new Object[]{foreignKeyValue});
                    CursorList cursorList = query.get();
                    ModelList manyModels = ModelList.from(cursorList);
                    if (manyModels != null) {
                        try {
                            oneToManyColumnField.field.setAccessible(true);
                            oneToManyColumnField.field.set(result, manyModels);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    cursorList.close();
                }
            }
            //fill manyToOne field
            for (ModelInfo.ManyToOneColumnField manyToOneColumnField : info.manyToOneColumns) {
                if (!colNames.contains(manyToOneColumnField.manyColumn)) {
                    continue;
                }
                Integer foreignKeyValue = c.getInt(c.getColumnIndexOrThrow(manyToOneColumnField.manyColumn));

                if(LazyModel.class.isAssignableFrom(manyToOneColumnField.field.getType())){
                    //if is lazy load,just put foreign key value to hiddenFieldsMap
                    ((Model)result).mHiddenFieldsMap.put(manyToOneColumnField.manyColumn,foreignKeyValue);
                }else {
                    final OneQuery query = new OneQuery(sprinkles);
                    query.resultClass = manyToOneColumnField.field.getType();
                    query.placeholderQuery = "SELECT * FROM " + getTableName(query.resultClass)
                            + " WHERE " + manyToOneColumnField.oneColumn + "=?";
                    query.rawQuery = Utils.insertSqlArgs(sprinkles, query.placeholderQuery, new Object[]{foreignKeyValue});
                    Object oneModel = query.get();
                    if (oneModel != null) {
                        try {
                            manyToOneColumnField.field.setAccessible(true);
                            manyToOneColumnField.field.set(result, oneModel);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
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
    public String getKeyValueTag(Model m){
        final ModelInfo info = ModelInfo.from(sprinkles, m.getClass());
        final StringBuilder keyValuesTag = new StringBuilder();
        for (ModelInfo.ColumnField column : info.columns) {
            if(column.isKey) {
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
            if(column.isKey) {
                if(c.getColumnIndex(column.name)>=0) {
                    column.field.setAccessible(true);
                    keyValuesTag.append(c.getString(c.getColumnIndex(column.name)) + "_");
                }
            }
        }
        return keyValuesTag.toString();
    }


    public static String getTableName(Class<? extends Model> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            String tabName = clazz.getAnnotation(Table.class).value();
            return TextUtils.isEmpty(tabName) ? clazz.getName().replace(".","_") : tabName;
        }
        throw new NoTableAnnotationException();
    }

    public void assureTableExist(ModelInfo table) {
        synchronized (table) {
            if (!isTableExist(table)) {
                String sql = Utils.getCreateTableSQL(table);
                sprinkles.getDatabase().execSQL(sql);
            }
        }
    }

    /**
     * check is table exist
     *
     * @param table
     * @return
     */
    public boolean isTableExist(ModelInfo table) {
        if (table.isTableChecked) {
            return true;
        }

        Cursor cursor = null;
        try {
            String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"
                    + table.tableName + "' ";
            cursor = sprinkles.getDatabase().rawQuery(sql, null);
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
