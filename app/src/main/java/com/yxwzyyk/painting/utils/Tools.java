package com.yxwzyyk.painting.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.List;

/**
 * Created by yyk on 30/09/2016.
 */

public class Tools {

    /**
     * 获取应用版本号
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            String version = packInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            return "0";
        }
    }

    /**
     * 申请权限
     *
     * @param activity
     * @param list
     * @param requestCode
     */
    public static void requestAlertWindowPermission(Activity activity, List<String> list, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = list.toArray(new String[list.size()]);
            if (permissions.length > 0) {
                activity.requestPermissions(permissions, requestCode);
            }
        }
    }
}
