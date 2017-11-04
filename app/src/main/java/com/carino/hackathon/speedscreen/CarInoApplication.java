package com.carino.hackathon.speedscreen;

import android.app.Application;
import android.content.Context;

/**
 * Created by alimertozdemir on 4.11.2017.
 */


public class CarInoApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
