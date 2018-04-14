package it.iiizio.epubator.presentation.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesHelper {

    private final SharedPreferences preferences;

    public static PreferencesHelper getAppPreferences(Context context){
        return new PreferencesHelper(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public static PreferencesHelper getViewPreferences(Activity activity){
        return new PreferencesHelper(activity.getPreferences(Context.MODE_PRIVATE));
    }

    public PreferencesHelper(Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public PreferencesHelper(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean getBoolean(String key, boolean defaultValue){
        return preferences.getBoolean(key, defaultValue);
    }

    public boolean getBoolean(String key){
        return getBoolean(key, false);
    }

    public String getString(String key, String defaultValue){
        return preferences.getString(key, defaultValue);
    }

    public String getString(String key){
        return getString(key, null);
    }

    public int getInt(String key, int defaultValue){
        return preferences.getInt(key, defaultValue);
    }

    public int getInt(String key){
        return getInt(key, 0);
    }

    public int getParsedString(String key, int defaultValue){
        String value = preferences.getString(key, String.valueOf(defaultValue));
        return Integer.parseInt(value);
    }

    public int getParsedString(String key, String defaultValue){
        return getParsedString(key, String.valueOf(defaultValue));
    }

    public int getParsedString(String key){
        return getParsedString(key, 0);
    }

    public void save(String key, boolean value){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void save(String key, String value){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
