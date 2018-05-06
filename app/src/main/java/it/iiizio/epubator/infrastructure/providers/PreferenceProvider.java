package it.iiizio.epubator.infrastructure.providers;

public interface PreferenceProvider {

	boolean getBoolean(String key, boolean defaultValue);

	boolean getBoolean(String key);

	String getString(String key, String defaultValue);

	String getString(String key);

	int getInt(String key, int defaultValue);

	int getInt(String key);

	int getParsedString(String key, int defaultValue);

	int getParsedString(String key, String defaultValue);

	int getParsedString(String key);

	void save(String key, boolean value);

	void save(String key, String value);

	void save(String key, int value);

}
