package com.lsjwzh.orm.rx;

import rx.Observable;
import rx.Subscriber;
import com.lsjwzh.orm.Model;
import com.lsjwzh.orm.Sprinkles;

/**
 * RxORM.
 */

public class RxORM {
    Sprinkles sprinkles;

    public RxORM(Sprinkles sprinkles) {
        this.sprinkles = sprinkles;
    }

    public <T extends Model> Observable<T> query() {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {

            }
        });
    }

}
