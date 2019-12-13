package com.sample.learn.hostapp;

import android.app.Application;
import android.content.Context;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/13 10:29
 * Description:
 */
public class HostApplication extends Application {

    private static Context mBaseContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mBaseContext = this;
    }

    public static Context getContext(){
        return mBaseContext;
    }
}
