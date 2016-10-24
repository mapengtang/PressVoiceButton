package com.ma.voice.pressvociebutton.util;

import android.app.Application;
import android.content.Context;

/**
 * 基类的Application类
 * Created by mpt on 2016/10/20.
 */

public class MyApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getBaseContext();
    }

    public static Context getMyContext() {
        return mContext;
    }
}
