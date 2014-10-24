package se.emilsjolander.sprinkles;

import java.lang.reflect.Field;

import se.emilsjolander.sprinkles.exceptions.LazyModelListLoadFailException;

/**
 * Created by panwenye on 14-10-14.
 */
public class LazyModelList<T extends Model> {
    Class<T> mModelClass;
    ModelInfo.OneToManyColumnField mOneToManyColumnField;
    Object mParent;
    public LazyModelList(Class<T> modelClass,Object parent,ModelInfo.OneToManyColumnField columnField){
        mModelClass = modelClass;
        mParent = parent;
        mOneToManyColumnField = columnField;
    }
    public ModelList<T> load(){
        try {
            Field oneColumnField = mParent.getClass().getDeclaredField(mOneToManyColumnField.oneColumn);
            oneColumnField.setAccessible(true);
            Object foreignKeyValue = oneColumnField.get(mParent);
            return Query.where(mModelClass)
                    .equalTo(mOneToManyColumnField.manyColumn,foreignKeyValue)
                    .find();
        } catch (Exception e) {
            throw new LazyModelListLoadFailException(e);
        }
    }
}
