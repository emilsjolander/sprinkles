package com.lsjwzh.orm;


import com.lsjwzh.orm.exceptions.LazyModelLoadFailException;

import java.lang.reflect.Field;

import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * LazyModel.
 */
public class LazyModel<T extends Model> extends Observable<T> {
    final Sprinkles sprinkles;
    Class<T> modelClass;
    ModelInfo.ManyToOneColumnField manyToOneColumnField;
    Object source;
    T cache;

    LazyModel(final Sprinkles sprinkles, final Class<T> modelClass, final Object source, final ModelInfo.ManyToOneColumnField columnField) {
        this.sprinkles = sprinkles;
        this.modelClass = modelClass;
        this.source = source;
        this.manyToOneColumnField = columnField;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        if (cache == null) {
            try {
                Field manyColumnField = source.getClass().getDeclaredField(manyToOneColumnField.manyColumn);
                Object foreignKeyValue = null;
                if (manyColumnField != null) {
                    manyColumnField.setAccessible(true);
                    foreignKeyValue = manyColumnField.get(source);
                } else {
                    //if manyColumn is not been implicit declared,find it from mHiddenFieldsMap
                    foreignKeyValue = ((Model) source).mHiddenFieldsMap.get(manyToOneColumnField.manyColumn);
                }
                if (foreignKeyValue != null) {
                    cache = new Query(sprinkles).findSingle(QueryBuilder.from(modelClass)
                        .where()
                        .equalTo(manyToOneColumnField.oneColumn, foreignKeyValue)
                        .end());
                }
            } catch (Exception e) {
                throw new LazyModelLoadFailException(e);
            }
        }
        observer.onNext(cache);
        observer.onComplete();
    }


}
