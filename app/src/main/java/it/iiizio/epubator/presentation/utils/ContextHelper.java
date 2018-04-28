package it.iiizio.epubator.presentation.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class ContextHelper {

    public static boolean isServiceRunning(Context context, Class<?> serviceClass){
        List<ActivityManager.RunningServiceInfo> runningServices = getRunningServices(context);
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private static List<ActivityManager.RunningServiceInfo> getRunningServices(Context context){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return manager.getRunningServices(Integer.MAX_VALUE);
    }

}
