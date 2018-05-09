package com.lsjwzh.orm;

import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * RxSprinkles.
 */
public final class RxSprinkles {
  public final Sprinkles sprinkles;

  public RxSprinkles(Sprinkles sprinkles) {
    this.sprinkles = sprinkles;
  }

  public <T extends Model> Observable<T> query(final QueryBuilder<T> queryBuilder) {
    return Observable.create(new ObservableOnSubscribe<T>() {
      @Override
      public void subscribe(ObservableEmitter<T> e) throws Exception {
        ModelList<T> queryResult = new Query(sprinkles).find(queryBuilder);
        for (T model : queryResult) {
          e.onNext(model);
        }
        e.onComplete();
      }
    });
  }

  public <T extends Model> Observable<T> save(@NonNull final T model) {
    return Observable.create(new ObservableOnSubscribe<T>() {
      @Override
      public void subscribe(ObservableEmitter<T> e) throws Exception {
        if (sprinkles.save(model)) {
          e.onNext(model);
          e.onComplete();
        } else {
          e.onError(new IllegalStateException("Error while saving data."));
        }
      }
    });
  }

  public <T extends Model> Observable<T> saveAll(@NonNull final ModelList<T> listData) {
    return Observable.create(new ObservableOnSubscribe<T>() {
      @Override
      public void subscribe(ObservableEmitter<T> e) throws Exception {
        if (sprinkles.saveAll(listData)) {
          for (T model : listData) {
            e.onNext(model);
          }
          e.onComplete();
        } else {
          e.onError(new IllegalStateException("Error while saving data."));
        }
      }
    });
  }


  public <T extends Model> Observable<T> delete(@NonNull final T model) {
    return Observable.create(new ObservableOnSubscribe<T>() {
      @Override
      public void subscribe(ObservableEmitter<T> e) throws Exception {
        if (sprinkles.delete(model)) {
          e.onNext(model);
          e.onComplete();
        } else {
          e.onError(new IllegalStateException("Error while saving data."));
        }
      }
    });
  }


  public <T extends Model> Observable<T> deleteAll(@NonNull final ModelList<T> listData) {
    return Observable.create(new ObservableOnSubscribe<T>() {
      @Override
      public void subscribe(ObservableEmitter<T> e) throws Exception {
        if (sprinkles.deleteAll(listData)) {
          for (T model : listData) {
            e.onNext(model);
          }
          e.onComplete();
        } else {
          e.onError(new IllegalStateException("Error while saving data."));
        }
      }
    });
  }
}
