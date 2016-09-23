package com.example.wechatemojilayout;

import android.app.Application;

/**
 * Created by 青松 on 2016/9/23.
 */
public class BaseApplication extends Application {

    private static BaseApplication instance;

    public static BaseApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
