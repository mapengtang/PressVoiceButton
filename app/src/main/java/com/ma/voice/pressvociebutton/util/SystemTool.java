package com.ma.voice.pressvociebutton.util;

import android.os.Build;
import android.os.Environment;

/**
 * Android系统工具类
 * Created by mapengtang on 2016/10/20.
 */

public class SystemTool {

    public static boolean isSDCardExist() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    public static boolean isNeedRequestPermisssion() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        return true;
    }
}
