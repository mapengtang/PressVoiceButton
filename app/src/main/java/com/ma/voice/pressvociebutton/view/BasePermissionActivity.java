package com.ma.voice.pressvociebutton.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.ma.voice.pressvociebutton.BuildConfig;
import com.ma.voice.pressvociebutton.util.CheckAppPermissions;
import com.ma.voice.pressvociebutton.util.CheckAppPermissionsImpl;
import com.ma.voice.pressvociebutton.util.Global;
import com.ma.voice.pressvociebutton.util.SystemTool;

import java.util.ArrayList;
import java.util.List;

/**
 * 申请权限的Activity，适用于所有的Android版本（6.0以下的版本会进行判断）
 * 没有借助第三方库权限的申请
 * Created by ma on 2016/9/21.
 */
public abstract class BasePermissionActivity extends AppCompatActivity {

    final private int REQUEST_CODE_ASK_STORAGE_PERMISSIONS = 0x20;

    final private String storagePermissions[] = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final String mStorageHint = "设置" + BuildConfig.APP_NAME + "的存储空间权限";

    protected String mHint;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkStoragePermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Global.setIsReuqestStorage(false);
    }

    /*------------以下权限申请后返回结果的接收处理------------*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_STORAGE_PERMISSIONS) {
            handlerStoragePermissionsResult(permissions, grantResults);
        } else {
            handlerPermissionsResult(permissions, grantResults, requestCode);
        }
    }

    private void handlerPermissionsResult(String[] permissions, int[] grantResults, int requestCode) {
        if (permissions.length == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onBasePermissionGranted(permissions[0], requestCode);
            } else {
                onBasePermissionDenied(requestCode);
                showRationaleDialog();
            }
        } else {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    showRationaleDialog();
                    onBasePermissionDenied(requestCode);
                    return;
                }
            }
            onBasePermissionsGranted(permissions, requestCode);
        }
    }

    private void handlerStoragePermissionsResult(String[] permissions, int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                Global.setIsSDCardExist(false);
                onBasePermissionDenied(REQUEST_CODE_ASK_STORAGE_PERMISSIONS);
                showRationaleDialog();
                return;
            }
        }
        Global.setIsSDCardExist(true);
        onBasePermissionsGranted(permissions, REQUEST_CODE_ASK_STORAGE_PERMISSIONS);
    }

    /*------------以上权限申请后返回结果的接收处理------------*/

    /*------------以下权限的申请------------*/
    public void baseRequestPermission(@NonNull final String permisssion, @NonNull final String hint, int requestId) {
        mHint = hint;
        int hasPermission = ContextCompat.checkSelfPermission(this, permisssion);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            baseStartRequest(new String[]{permisssion}, requestId);
        } else {
            onBasePermissionGranted(permisssion, requestId);
        }
    }

    public void baseRequestPermissions(@NonNull final String[] permisions, @NonNull final String hint, int requestId) {
        mHint = hint;
        final List<String> permissionNeededList = new ArrayList<>();
        for (String permission : permisions) {
            int hasPermission = ContextCompat.checkSelfPermission(this, permission);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                permissionNeededList.add(permission);
            }
        }
        if (!permissionNeededList.isEmpty()) {
            final String[] permissionNeededArray = permissionNeededList.toArray(new String[permissionNeededList.size()]);
            baseStartRequest(permissionNeededArray, requestId);
        } else {
            onBasePermissionsGranted(permisions, requestId);
        }
    }

    //申请外部存储空间的权限
    public void baseRequestStoragePermissions() {
//        mHint = mStorageHint;
        if (!SystemTool.isSDCardExist()) {
            Global.setIsSDCardExist(false);
            onSDCardNotExist();
            return;
        }
        if (!SystemTool.isNeedRequestPermisssion()) {
            Global.setIsSDCardExist(true);
            onBasePermissionsGranted(storagePermissions, REQUEST_CODE_ASK_STORAGE_PERMISSIONS);
            return;
        }
        if (Global.getIsRequestStorage()) {
            onBasePermissionsGranted(storagePermissions, REQUEST_CODE_ASK_STORAGE_PERMISSIONS);
            return;
        }
        final List<String> permissionNeededList = new ArrayList<>();
        for (String permission : storagePermissions) {
            int hasPermission = ContextCompat.checkSelfPermission(this, permission);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                permissionNeededList.add(permission);
            }
        }
        if (!permissionNeededList.isEmpty()) {
            final String[] permissionNeededArray = permissionNeededList.toArray(new String[permissionNeededList.size()]);
            baseStartRequest(permissionNeededArray, REQUEST_CODE_ASK_STORAGE_PERMISSIONS);
        } else {
            Global.setIsSDCardExist(true);
            onBasePermissionsGranted(storagePermissions, REQUEST_CODE_ASK_STORAGE_PERMISSIONS);
        }
    }

    //请求权限
    private void baseStartRequest(String[] permissionNeededArray, int requestCode) {
        ActivityCompat.requestPermissions(this, permissionNeededArray, requestCode);
    }

    /*------------以上权限的申请------------*/
    /*-----------------以下检查权限------------------*/
    private void checkStoragePermissions() {
        if (!SystemTool.isSDCardExist()) {
            Global.setIsSDCardExist(false);
        }
        CheckAppPermissions checkAppPermissions = new CheckAppPermissionsImpl();
        boolean isGranted = checkAppPermissions.check(this, storagePermissions);
        if (isGranted) {
            Global.setIsSDCardExist(true);
            onBasePermissionsGranted(storagePermissions, REQUEST_CODE_ASK_STORAGE_PERMISSIONS);
        } else {
            Global.setIsSDCardExist(false);
            onBasePermissionDenied(REQUEST_CODE_ASK_STORAGE_PERMISSIONS);
        }
    }
    /*-----------------以上检查权限------------------*/

    /*------------以下权限的显示dialog和sanckBar-------------*/
    private void showRationaleDialog() {
        if (TextUtils.isEmpty(mHint)) {
            return;
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(mHint);
        dialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                jumpToAndroidSetting();
            }
        });
        dialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

     /*------------以上权限的显示dialog和sanckBar-------------*/

    private void jumpToAndroidSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void finishSelf() {
        finish();
    }

    @CallSuper
    public void onBasePermissionGranted(String permission, int requestId) {
        //调用此方法必须判断权限是否为自己申请的权限
        onCheckPermissionComplete();
    }

    @CallSuper
    public void onBasePermissionsGranted(String[] permissions, int requestId) {
        //调用此方法必须判断权限是否为自己申请的权限
        onCheckPermissionComplete();
    }

    @CallSuper
    public void onBasePermissionDenied(int requestId) {
        onCheckPermissionComplete();
    }

    @CallSuper
    public void onSDCardNotExist() {
        onCheckPermissionComplete();
    }

    @CallSuper
    public void onCheckPermissionComplete() {
    }

}
