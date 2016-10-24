package com.ma.voice.pressvociebutton.util;

import android.os.Environment;

/**
 * 全局变量
 * Created by ma on 2016/10/20.
 */

public class Global {

    static boolean mIsSDCardExist = false;
    static boolean mIsReqestStorage = false;

    public static void setIsSDCardExist(boolean isSDCardExist) {
        mIsSDCardExist = isSDCardExist;
    }

    public static String getFileRootDir() {
        if (mIsSDCardExist) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();//SD卡上的存储
        } else {
            return MyApplication.getMyContext().getCacheDir().getAbsolutePath();//手机内部的存储
        }
    }

    public static void setIsReuqestStorage(boolean isReuqestStorage) {
        mIsReqestStorage = isReuqestStorage;
    }

    public static boolean getIsRequestStorage() {
        return mIsReqestStorage;
    }
}
