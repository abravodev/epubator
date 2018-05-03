package it.iiizio.epubator.infrastructure.providers;

import android.content.Context;
import android.preference.PreferenceManager;

public class SharedPreferenceProviderImpl extends PreferenceProviderImpl {

	public SharedPreferenceProviderImpl(Context context){
		super(PreferenceManager.getDefaultSharedPreferences(context));
	}
}
