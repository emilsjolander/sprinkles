package se.emilsjolander.sprinkles;

import java.lang.reflect.Field;

import se.emilsjolander.sprinkles.exceptions.LazyModelLoadFailException;

/**
 * Created by panwenye on 14-10-14.
 */
public class LazyModel<T extends Model> {
    Class<T> mModelClass;
    ModelInfo.ManyToOneColumnField mManyToOneColumnField;
    Object mSource;

    T mCache;

    public LazyModel(Class<T> modelClass, Object source, ModelInfo.ManyToOneColumnField columnField){
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
                foreignKeyValue = manyColumnField.get(mSource);
            }else {
                //if manyColumn is not been implicit declared,find it from mHiddenFieldsMap
                foreignKeyValue = ((Model)mSource).mHiddenFieldsMap.get(mManyToOneColumnField.manyColumn);
            }
            if(foreignKeyValue==null){
                return null;
            }
            mCache = Query.where(mModelClass)
                    .equalTo(mManyToOneColumnField.oneColumn,foreignKeyValue)
                    .findSingle();
            return mCache;
        } catch (Exception e) {
            throw new LazyModelLoadFailException(e);
        }
    }
}
