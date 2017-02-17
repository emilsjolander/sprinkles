package com.lsjwzh.orm;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.Subscriber;

/**
 * RxSprinkles.
 */
public final class RxSprinkles {
    public final Sprinkles sprinkles;

    public RxSprinkles(Sprinkles sprinkles) {
        this.sprinkles = sprinkles;
    }

    public <T extends Model> Observable<T> query(final QueryBuilder<T> queryBuilder) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                subscriber.onStart();
                ModelList<T> queryResult = new Query(sprinkles).find(queryBuilder);
                for (T model : queryResult) {
                    subscriber.onNext(model);
                }
                subscriber.onCompleted();
            }
        });
    }

    public <T extends Model> Observable<T> save(@NonNull final T model) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                subscriber.onStart();
                if (sprinkles.save(model)) {
                    subscriber.onNext(model);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException("Error while saving data."));
                }
            }
        });
    }

    public <T extends Model> Observable<T> saveAll(@NonNull final ModelList<T> listData) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                subscriber.onStart();
                if (sprinkles.saveAll(listData)) {
                    for (T model : listData) {
                        subscriber.onNext(model);
                    }
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException("Error while saving data."));
                }
            }
        });
    }


    public <T extends Model> Observable<T> delete(@NonNull final T model) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                subscriber.onStart();
                if (sprinkles.delete(model)) {
                    subscriber.onNext(model);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException("Error while saving data."));
                }
            }
        });
    }


    public <T extends Model> Observable<T> deleteAll(@NonNull final ModelList<T> listData) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                subscriber.onStart();
                if (sprinkles.deleteAll(listData)) {
                    for (T model : listData) {
                        subscriber.onNext(model);
                    }
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException("Error while saving data."));
                }
            }
        });
    }
}
