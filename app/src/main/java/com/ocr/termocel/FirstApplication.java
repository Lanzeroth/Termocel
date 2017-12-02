package com.ocr.termocel;

import android.app.Application;
import android.content.Context;

import com.activeandroid.ActiveAndroid;

/**
 * This class initializes all the goodies on the app
 */
public class FirstApplication extends Application {

    public static Context context;
    private static FirstApplication instance;

    public FirstApplication() {
        instance = this;
    }

    public static Context getContext() {
        return context;
    }

    public static FirstApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize ORM
        ActiveAndroid.initialize(this);

        context = getApplicationContext();


//        Firebase.setAndroidContext(this);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
    }
}
