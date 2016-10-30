package com.lsjwzh.orm;

import java.lang.reflect.Field;

import com.lsjwzh.orm.exceptions.LazyModelListLoadFailException;

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
            mCache = Query.where(sprinkles, mModelClass)
                    .equalTo(mOneToManyColumnField.manyColumn,foreignKeyValue)
                    .find();
            return mCache;
        } catch (Exception e) {
            throw new LazyModelListLoadFailException(e);
        }
    }
}
