package com.lsjwzh.orm;

import com.lsjwzh.orm.exceptions.LazyModelLoadFailException;

import java.lang.reflect.Field;

/**
 * Created by panwenye on 14-10-14.
 */
public class LazyModel<T extends Model> {
    final Sprinkles sprinkles;
    Class<T> mModelClass;
    ModelInfo.ManyToOneColumnField mManyToOneColumnField;
    Object mSource;

    T mCache;

    public LazyModel(Sprinkles sprinkles, Class<T> modelClass, Object source, ModelInfo.ManyToOneColumnField columnField){
        this.sprinkles = sprinkles;
        mModelClass = modelClass;
        mSource = source;
        mManyToOneColumnField = columnField;
    }
    public T load(){
        if(mCache!=null){
            return mCache;
        }
        try {
            Field manyColumnField = mSource.getClass().getDeclaredField(mManyToOneColumnField.manyColumn);
            Object foreignKeyValue = null;
            if(manyColumnField!=null){
                manyColumnField.setAccessible(true);
                foreignKeyValue = manyColumnField.get(mSource);
            }else {
                //if manyColumn is not been implicit declared,find it from mHiddenFieldsMap
                foreignKeyValue = ((Model)mSource).mHiddenFieldsMap.get(mManyToOneColumnField.manyColumn);
            }
            if(foreignKeyValue==null){
                return null;
            }
            mCache = new Query(sprinkles).findSingle(QueryBuilder.from(mModelClass)
                    .where()
                    .equalTo(mManyToOneColumnField.oneColumn, foreignKeyValue)
                    .end());
            return mCache;
        } catch (Exception e) {
            throw new LazyModelLoadFailException(e);
        }
    }
}
