package com.radoslavdosev.userstories.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

public final class AndroidComponentUtil {

    private AndroidComponentUtil(){
        // no instances allowed
    }

    public static void toggleComponent(@NonNull final Context context,
                                       @NonNull final Class componentClass,
                                       final boolean enable) {
        final ComponentName componentName = new ComponentName(context, componentClass);
        final PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(componentName,
                enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static boolean isServiceRunning(@NonNull final Context context, @NonNull final Class serviceClass) {
        final ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
