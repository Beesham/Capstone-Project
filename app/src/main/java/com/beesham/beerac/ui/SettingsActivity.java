package com.beesham.beerac.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import com.beesham.beerac.R;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On handset devices,
 * settings are presented as a single list. On tablets, settings are split by category, with
 * category headers shown to the left of the list of settings. <p> See <a
 * href="http://developer.android.com/design/patterns/settings.html"> Android Design: Settings</a>
 * for design guidelines and the <a href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

    private static String KEY_PREF_UNITS;
    private static String KEY_PREF_BODY_WEIGHT;
    private static String KEY_PREF_GENDER;

    private static Preference mBodyWeightPref;

    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    /**
     * A preference value change listener that updates the preference's summary to reflect its new
     * value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            setPreferenceSummary(preference, value);
            return true;
        }
    };

    private static void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
        String key = preference.getKey();
        
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

            if(key.equals(KEY_PREF_UNITS)){
                //Commit the change so the pref will be save instantly so as to avoid a delay for when
                //body weight pref updates
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext()).edit()
                        .putString(preference.getContext().getString(R.string.pref_units_key), stringValue)
                        .commit();

                sBindPreferenceSummaryToValueListener.onPreferenceChange(mBodyWeightPref,
                        PreferenceManager
                                .getDefaultSharedPreferences(preference.getContext())
                                .getString(preference.getContext().getString(R.string.pref_body_weight_key), ""));
            }
        }else {

            String[] mUnitsArray =  preference.getContext().getResources().getStringArray(R.array.pref_weight_units_list_values);

            // For all other preferences, set the summary to the value's
            // simple string representation.
            if(key.equals(KEY_PREF_BODY_WEIGHT)) {
                if (PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getContext().getString(R.string.pref_units_key), "").equals("imperial")) {
                    preference.setSummary(stringValue + " " + mUnitsArray[0]);
                } else if (PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getContext().getString(R.string.pref_units_key), "").equals("metric")){
                    preference.setSummary(stringValue + " " +mUnitsArray[1]);
                }
            }else{
                preference.setSummary(stringValue);
            }
        }
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is
     * changed, its summary (line of text below the preference title) is updated to reflect the
     * value. The summary is also immediately updated upon calling this method. The exact display
     * format is dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.pref_general);

        KEY_PREF_UNITS = getString(R.string.pref_units_key);
        KEY_PREF_BODY_WEIGHT = getString(R.string.pref_body_weight_key);
        KEY_PREF_GENDER = getString(R.string.pref_gender_key);

        mBodyWeightPref = findPreference(getString(R.string.pref_body_weight_key));

        bindPreferenceSummaryToValue(findPreference(KEY_PREF_GENDER));
        bindPreferenceSummaryToValue(findPreference(KEY_PREF_UNITS));
        bindPreferenceSummaryToValue(mBodyWeightPref);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
