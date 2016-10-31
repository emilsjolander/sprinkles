package com.lsjwzh.orm;


import com.lsjwzh.orm.exceptions.LazyModelLoadFailException;

import java.lang.reflect.Field;

import rx.Observable;
import rx.Subscriber;

/**
 * LazyModel.
 */
public class LazyModel<T extends Model> extends Observable<T> {
    final Sprinkles sprinkles;
    Class<T> modelClass;
    ModelInfo.ManyToOneColumnField manyToOneColumnField;
    Object source;


    LazyModel(final Sprinkles sprinkles, final Class<T> modelClass, final Object source, final ModelInfo.ManyToOneColumnField columnField) {
        super(new OnSubscribe<T>() {
            T cache;

            @Override
            public void call(Subscriber<? super T> subscriber) {
                subscriber.onStart();
                if (cache == null) {
                    try {
                        Field manyColumnField = source.getClass().getDeclaredField(columnField.manyColumn);
                        Object foreignKeyValue = null;
                        if (manyColumnField != null) {
                            manyColumnField.setAccessible(true);
                            foreignKeyValue = manyColumnField.get(source);
                        } else {
                            //if manyColumn is not been implicit declared,find it from mHiddenFieldsMap
                            foreignKeyValue = ((Model) source).mHiddenFieldsMap.get(columnField.manyColumn);
                        }
                        if (foreignKeyValue != null) {
                            cache = new Query(sprinkles).findSingle(QueryBuilder.from(modelClass)
                                    .where()
                                    .equalTo(columnField.oneColumn, foreignKeyValue)
                                    .end());
                        }
                    } catch (Exception e) {
                        throw new LazyModelLoadFailException(e);
                    }
                }
                subscriber.onNext(cache);
                subscriber.onCompleted();
            }
        });
        this.sprinkles = sprinkles;
        this.modelClass = modelClass;
        this.source = source;
        this.manyToOneColumnField = columnField;
    }

}
