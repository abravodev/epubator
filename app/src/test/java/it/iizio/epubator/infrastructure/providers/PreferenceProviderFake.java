package it.iizio.epubator.infrastructure.providers;

import java.util.HashMap;

import it.iiizio.epubator.infrastructure.providers.PreferenceProvider;

public class PreferenceProviderFake implements PreferenceProvider {

	private final HashMap<String, Object> preferences;

	public PreferenceProviderFake() {
		preferences = new HashMap<>();
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		return (boolean) preferences.getOrDefault(key, defaultValue);
	}

	@Override
	public boolean getBoolean(String key) {
		return (boolean) preferences.getOrDefault(key, false);
	}

	@Override
	public String getString(String key, String defaultValue) {
		return (String) preferences.getOrDefault(key, defaultValue);
	}

	@Override
	public String getString(String key) {
		return (String) preferences.getOrDefault(key, null);
	}

	@Override
	public int getInt(String key, int defaultValue) {
		return 0;
	}

	@Override
	public int getInt(String key) {
		return (int) preferences.getOrDefault(key, 0);
	}

	@Override
	public int getParsedString(String key, int defaultValue) {
		return (int) preferences.getOrDefault(key, defaultValue);
	}

	@Override
	public int getParsedString(String key, String defaultValue) {
		return getParsedString(key, Integer.parseInt(defaultValue));
	}

	@Override
	public int getParsedString(String key) {
		return (int) preferences.getOrDefault(key, 0);
	}

	@Override
	public void save(String key, boolean value) {
		preferences.put(key, value);
	}

	@Override
	public void save(String key, String value) {
		preferences.put(key, value);
	}

	@Override
	public void save(String key, int value) {
		preferences.put(key, value);
	}
}
