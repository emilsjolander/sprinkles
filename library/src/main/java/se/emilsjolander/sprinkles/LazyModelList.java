package se.emilsjolander.sprinkles;

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
            mOneToManyColumnField.field.setAccessible(true);
            Object foreignKeyValue = mOneToManyColumnField.field.get(mParent);
            return Query.Where(mModelClass)
                    .equalTo(mOneToManyColumnField.manyColumn,foreignKeyValue)
                    .find();
        } catch (Exception e) {
            throw new LazyModelListLoadFailException(e);
        }
    }
}
