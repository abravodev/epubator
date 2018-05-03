package it.iiizio.epubator.presentation.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionHelper {

    public static boolean isPending(Context context, String permission){
        return ContextCompat.checkSelfPermission(context, permission)
			!= PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isGranted(Context context, String permission){
        return ContextCompat.checkSelfPermission(context, permission)
			== PackageManager.PERMISSION_GRANTED;
    }

    public static boolean writePermissionIsGranted(Context context){
        return isGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean writePermissionIsPending(Context context){
        return isPending(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void showPermissionRequest(Activity activity, String permission, int requestCode){
        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            ActivityCompat.requestPermissions(activity, new String[]{ permission },requestCode);
        }
    }

    public static void showWritePermissionRequest(Activity activity, int requestCode){
        showPermissionRequest(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, requestCode);
    }

    public static void checkPermission(Activity activity, String permission, int requestCode){
        if (isPending(activity, permission)) {
            showPermissionRequest(activity, permission, requestCode);
        }
    }

    public static void checkWritePermission(Activity activity, int requestCode){
        checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, requestCode);
    }

}
