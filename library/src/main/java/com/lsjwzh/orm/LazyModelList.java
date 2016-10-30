package com.lsjwzh.orm;

import com.lsjwzh.orm.exceptions.LazyModelListLoadFailException;

import java.lang.reflect.Field;

/**
 * Created by panwenye on 14-10-14.
 */
public class LazyModelList<T extends Model> {
    final Sprinkles sprinkles;
    Class<T> mModelClass;
    ModelInfo.OneToManyColumnField mOneToManyColumnField;
    Object mParent;

    ModelList<T> mCache;

    public LazyModelList(Sprinkles sprinkles, Class<T> modelClass,Object parent,ModelInfo.OneToManyColumnField columnField){
        mModelClass = modelClass;
        mParent = parent;
        mOneToManyColumnField = columnField;
        this.sprinkles = sprinkles;
    }
    public ModelList<T> load(){
        if(mCache!=null){
            return mCache;
        }
        try {
            Field oneColumnField = mParent.getClass().getDeclaredField(mOneToManyColumnField.oneColumn);
            oneColumnField.setAccessible(true);
            Object foreignKeyValue = oneColumnField.get(mParent);
            mCache = new Query(sprinkles).find(QueryBuilder.from(mModelClass)
                    .where()
                    .equalTo(mOneToManyColumnField.manyColumn, foreignKeyValue)
                    .end());
            return mCache;
        } catch (Exception e) {
            throw new LazyModelListLoadFailException(e);
        }
    }
}
