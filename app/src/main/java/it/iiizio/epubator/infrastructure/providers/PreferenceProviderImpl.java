package it.iiizio.epubator.infrastructure.providers;

import android.content.SharedPreferences;

public class PreferenceProviderImpl implements PreferenceProvider {

	//<editor-fold desc="Attributes">
	private final SharedPreferences preferences;
	//</editor-fold>

	//<editor-fold desc="Constructors">
	public PreferenceProviderImpl(SharedPreferences preferences) {
		this.preferences = preferences;
	}
	//</editor-fold>

	//<editor-fold desc="Methods">
	@Override
	public boolean getBoolean(String key, boolean defaultValue){
		return preferences.getBoolean(key, defaultValue);
	}

	@Override
	public boolean getBoolean(String key){
		return getBoolean(key, false);
	}

	@Override
	public String getString(String key, String defaultValue){
		return preferences.getString(key, defaultValue);
	}

	@Override
	public String getString(String key){
		return getString(key, null);
	}

	@Override
	public int getInt(String key, int defaultValue){
		return preferences.getInt(key, defaultValue);
	}

	@Override
	public int getInt(String key){
		return getInt(key, 0);
	}

	@Override
	public int getParsedString(String key, int defaultValue){
		String value = preferences.getString(key, String.valueOf(defaultValue));
		return Integer.parseInt(value);
	}

	@Override
	public int getParsedString(String key, String defaultValue){
		return getParsedString(key, Integer.parseInt(defaultValue));
	}

	@Override
	public int getParsedString(String key){
		return getParsedString(key, 0);
	}

	@Override
	public void save(String key, boolean value){
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	@Override
	public void save(String key, String value){
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	@Override
	public void save(String key, int value) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	//</editor-fold>

}
