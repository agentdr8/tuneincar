package com.dr8.xposed.tuneincar;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class PrefsActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

		// Display the fragment as the main content.
		if (savedInstanceState == null)
			getFragmentManager().beginTransaction().replace(android.R.id.content,
					new PrefsFragment()).commit();

	}
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public static class PrefsFragment extends PreferenceFragment {
		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
			addPreferencesFromResource(R.xml.preferences);
		}
	}

}
