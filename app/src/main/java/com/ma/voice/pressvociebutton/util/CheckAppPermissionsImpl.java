package com.ma.voice.pressvociebutton.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 检查存储空间的读写权限
 * Created by ma on 2016/9/23.
 */
public class CheckAppPermissionsImpl implements CheckAppPermissions {

    @Override
    public boolean check(Activity activity, String permission) {
        int hasPermission = ContextCompat.checkSelfPermission(activity, permission);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    @Override
    public boolean check(Activity activity, String[] permissions) {
        StringBuilder logMsg = new StringBuilder("");
        final List<String> permissionNeededList = new ArrayList<>();
        for (String permission : permissions) {
            logMsg.append(permission.toString());
            int hasPermission = ContextCompat.checkSelfPermission(activity, permission);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                permissionNeededList.add(permission);
            }
        }
        if (permissionNeededList.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
}
