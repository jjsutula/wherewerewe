package com.nono.wherewerewe;

import com.nono.wherewerewe.data.SettingsConst;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {

    public static final String KEY_PREFERENCE_MEASUREMENT = "preference_measurement";
    public static final String KEY_PREFERENCE_COORDINATE = "preference_coordinate";
    public static final String KEY_PREFERENCE_COMPASS_VISIBLE = "preference_compass_visible";
    public static final String KEY_PREFERENCE_COMPASS_SIZE = "preference_compass_size";
    public static final String KEY_PREFERENCE_NAVIGATOR_VISIBLE = "preference_navigator_visible";
    public static final String KEY_PREFERENCE_NAVIGATOR_SIZE = "preference_navigator_size";

    public static final String DEFAULT_PREFERENCE_MEASUREMENT = SettingsConst.METRIC;
    public static final String DEFAULT_PREFERENCE_COORDINATE = SettingsConst.DECIMAL;
    public static final String DEFAULT_PREFERENCE_COMPASS_VISIBLE = "true";
    public static final String DEFAULT_PREFERENCE_COMPASS_SIZE = "4";
    public static final String DEFAULT_PREFERENCE_NAVIGATOR_VISIBLE = "true";
    public static final String DEFAULT_PREFERENCE_NAVIGATOR_SIZE = "4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
	public void onBackPressed() {
		setResult(RESULT_OK);
		super.onBackPressed();
	}
}
