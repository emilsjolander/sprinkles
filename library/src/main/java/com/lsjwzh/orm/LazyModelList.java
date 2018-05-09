package com.lsjwzh.orm;

import com.lsjwzh.orm.exceptions.LazyModelListLoadFailException;

import java.lang.reflect.Field;

import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * LazyModelList.
 */
public class LazyModelList<T extends Model> extends Observable<T> {
  final Sprinkles sprinkles;
  Class<T> modelClass;
  ModelInfo.OneToManyColumnField oneToManyColumnField;
  Object parent;
  ModelList<T> cache;


  public LazyModelList(final Sprinkles sprinkles, final Class<T> modelClass, final Object parent,
                       final ModelInfo.OneToManyColumnField columnField) {
    this.modelClass = modelClass;
    this.parent = parent;
    oneToManyColumnField = columnField;
    this.sprinkles = sprinkles;
  }

  @Override
  protected void subscribeActual(Observer<? super T> observer) {
    if (cache == null) {
      try {
        Field oneColumnField = parent.getClass().getDeclaredField(oneToManyColumnField.oneColumn);
        oneColumnField.setAccessible(true);
        Object foreignKeyValue = oneColumnField.get(parent);
        cache = new Query(sprinkles).find(QueryBuilder.from(modelClass)
            .where()
            .equalTo(oneToManyColumnField.manyColumn, foreignKeyValue)
            .end());
      } catch (Exception e) {
        throw new LazyModelListLoadFailException(e);
      }
    }
    for (T model : cache) {
      observer.onNext(model);
    }
    observer.onComplete();
  }
}
