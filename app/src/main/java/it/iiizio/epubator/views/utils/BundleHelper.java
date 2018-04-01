package it.iiizio.epubator.views.utils;

import android.content.Intent;
import android.os.Bundle;

public class BundleHelper {

    public static String getExtraStringOrEmpty(Bundle bundle, String key){
        return getExtraStringOrDefault(bundle, key, "");
    }

    public static String getExtraStringOrDefault(Bundle bundle, String key) {
        return getExtraStringOrDefault(bundle, key, null);
    }

    public static String getExtraStringOrDefault(Bundle bundle, String key, String defaultValue){
        if(bundle == null){
            return defaultValue;
        }
        return bundle.getString(key, defaultValue);
    }

    public static String getExtraStringOrEmpty(Intent intent, String key){
        return getExtraStringOrDefault(intent.getExtras(), key, "");
    }

    public static String getExtraStringOrDefault(Intent intent, String key) {
        return getExtraStringOrDefault(intent.getExtras(), key, null);
    }

    public static String getExtraStringOrDefault(Intent intent, String key, String defaultValue){
        return getExtraStringOrDefault(intent.getExtras(), key, defaultValue);
    }

}
