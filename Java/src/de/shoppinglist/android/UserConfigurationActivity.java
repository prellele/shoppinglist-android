package de.shoppinglist.android;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;

public class UserConfigurationActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	public static final String KEY_PREF_LIST_TYPE = "listType";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		// show the current value in the settings screen
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updatePreferences(findPreference(key));
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceCategory) {
			PreferenceCategory cat = (PreferenceCategory) p;
			for (int i = 0; i < cat.getPreferenceCount(); i++) {
				initSummary(cat.getPreference(i));
			}
		} else {
			updatePreferences(p);
		}
	}

	private void updatePreferences(Preference p) {
		if (p instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) p;
			p.setSummary(editTextPref.getText());
		} else if (p instanceof ListPreference) {
			ListPreference listPref = (ListPreference) p;
			((ListPreference) p).setValue(listPref.getValue());
		}
	}

}
