package se.emilsjolander.sprinkles;

import se.emilsjolander.sprinkles.exceptions.LazyModelLoadFailException;

/**
 * Created by panwenye on 14-10-14.
 */
public class LazyModel<T extends Model> {
    Class<T> mModelClass;
    ModelInfo.ManyToOneColumnField mManyToOneColumnField;
    Object mSource;
    public LazyModel(Class<T> modelClass, Object source, ModelInfo.ManyToOneColumnField columnField){
        mModelClass = modelClass;
        mSource = source;
    }
    public T load(){
        try {
            mManyToOneColumnField.field.setAccessible(true);
            Object foreignKeyValue = mManyToOneColumnField.field.get(mSource);
            return Query.Where(mModelClass)
                    .equalTo(mManyToOneColumnField.oneColumn,foreignKeyValue)
                    .findSingle();
        } catch (Exception e) {
            throw new LazyModelLoadFailException(e);
        }
    }
}
