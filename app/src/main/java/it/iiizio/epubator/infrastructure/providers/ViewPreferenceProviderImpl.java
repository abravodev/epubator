package it.iiizio.epubator.infrastructure.providers;

import android.app.Activity;
import android.content.Context;

public class ViewPreferenceProviderImpl extends PreferenceProviderImpl {

	public ViewPreferenceProviderImpl(Activity activity) {
		super(activity.getPreferences(Context.MODE_PRIVATE));
	}
}
