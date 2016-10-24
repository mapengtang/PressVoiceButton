package com.ma.voice.pressvociebutton.util;

import android.app.Activity;

/**
 * 检查手机的权限
 * Created by mapengtang on 2016/9/23.
 */
public interface CheckAppPermissions {

    boolean check(Activity activity, String permission);

    boolean check(Activity activity, String[] permissions);
}
