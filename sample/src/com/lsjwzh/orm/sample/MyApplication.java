package com.lsjwzh.orm.sample;

import android.app.Application;

import com.lsjwzh.orm.RxSprinkles;
import com.lsjwzh.orm.Sprinkles;

public class MyApplication extends Application {
    public RxSprinkles rxSprinkles;
    public Sprinkles sprinkles;

    private static MyApplication sApplication;

    public static MyApplication getApplication() {
        return sApplication;
    }

	@Override
	public void onCreate() {
		super.onCreate();
        sprinkles = Sprinkles.init(getApplicationContext());
        rxSprinkles = new RxSprinkles(sprinkles);
        sApplication = this;
    }
}
