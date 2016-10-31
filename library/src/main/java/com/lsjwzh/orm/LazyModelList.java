package com.lsjwzh.orm;

import com.lsjwzh.orm.exceptions.LazyModelListLoadFailException;

import java.lang.reflect.Field;

import rx.Observable;
import rx.Subscriber;

/**
 * LazyModelList.
 */
public class LazyModelList<T extends Model> extends Observable<T> {
    final Sprinkles sprinkles;
    Class<T> modelClass;
    ModelInfo.OneToManyColumnField oneToManyColumnField;
    Object parent;


    public LazyModelList(final Sprinkles sprinkles, final Class<T> modelClass, final Object parent, final ModelInfo.OneToManyColumnField columnField){
        super(new Observable.OnSubscribe<T>() {
            ModelList<T> cache;

            @Override
            public void call(Subscriber<? super T> subscriber) {
                subscriber.onStart();
                if (cache == null) {
                    try {
                        Field oneColumnField = parent.getClass().getDeclaredField(columnField.oneColumn);
                        oneColumnField.setAccessible(true);
                        Object foreignKeyValue = oneColumnField.get(parent);
                        cache = new Query(sprinkles).find(QueryBuilder.from(modelClass)
                                .where()
                                .equalTo(columnField.manyColumn, foreignKeyValue)
                                .end());
                    } catch (Exception e) {
                        throw new LazyModelListLoadFailException(e);
                    }
                }
                for (T model : cache) {
                    subscriber.onNext(model);
                }
                subscriber.onCompleted();
            }
        });
        this.modelClass = modelClass;
        this.parent = parent;
        oneToManyColumnField = columnField;
        this.sprinkles = sprinkles;
    }
}
