package it.iiizio.epubator.presentation.views.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import it.iiizio.epubator.R;

public class PreferencesFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_app);
	}
}
