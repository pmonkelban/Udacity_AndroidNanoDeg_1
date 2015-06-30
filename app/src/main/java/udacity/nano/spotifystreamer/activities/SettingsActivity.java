package udacity.nano.spotifystreamer.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.data.StreamerContract;

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    public static final String ON_SETTINGS_CHANGED_CACHE_INVALID =
            "settings-changed-cache-invalid";

    public static final String ON_SETTINGS_CHANGED_NOTIFICATIONS_INVALID =
            "settings-changed-notifications-invalid";

    private static final Set<String> VALID_COUNTRY_CODES =
            new HashSet<>(Arrays.asList(Locale.getISOCountries()));

    private String mLastValidCountryCode;
    private Boolean mLastExplicitValue;
    private Boolean mLastOnLockValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference(MainActivity.PREF_COUNTRY_CODE));
        bindPreferenceSummaryToValue(findPreference(MainActivity.PREF_ALLOW_EXPLICIT));
        bindPreferenceSummaryToValue(findPreference(MainActivity.PREF_ALLOW_ON_LOCK));


        /*
        * Get the most recent values for the preferences.
        */
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mLastValidCountryCode = prefs.getString(MainActivity.PREF_COUNTRY_CODE,
                getResources().getString(R.string.prefs_default_country_code));

        mLastExplicitValue = prefs.getBoolean(MainActivity.PREF_ALLOW_EXPLICIT, true);
        mLastOnLockValue = prefs.getBoolean(MainActivity.PREF_ALLOW_ON_LOCK, true);

    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        if (preference instanceof EditTextPreference) {
            onPreferenceChange(preference,
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .getString(preference.getKey(), ""));

        } else if (preference instanceof CheckBoxPreference) {
            onPreferenceChange(preference,
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .getBoolean(preference.getKey(), true));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        switch (preference.getKey()) {
            case MainActivity.PREF_COUNTRY_CODE: {
                handleCountryCodePrefChange(preference, (String) newValue);
                break;
            }
            case MainActivity.PREF_ALLOW_EXPLICIT: {
                handleExplicitPrefChange(preference, (boolean) newValue);
                break;
            }
            case MainActivity.PREF_ALLOW_ON_LOCK: {
                handleAllowOnLockPrefChange(preference, (boolean) newValue);
                break;
            }
            default: // Do Nothing
        }

        return true;
    }

    private void handleCountryCodePrefChange(Preference preference, String newCountryCode) {

        if (mLastValidCountryCode.equals(newCountryCode))  return;

        // Check for a valid country code.
        if (VALID_COUNTRY_CODES.contains(newCountryCode)) {
            mLastValidCountryCode = newCountryCode;
            flushDbCache();
            notifyPrefsChange(ON_SETTINGS_CHANGED_CACHE_INVALID);

        } else {
            String msg = getString(R.string.invalid_country_code, newCountryCode);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

               /*
               * Reset country to last know good value.
               */
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .edit()
                    .putString(MainActivity.PREF_COUNTRY_CODE, mLastValidCountryCode)
                    .commit();
        }

        preference.setSummary(mLastValidCountryCode);

    }

    private void handleExplicitPrefChange(Preference preference, Boolean newValue)  {

        if (mLastExplicitValue.equals(newValue)) return;

        mLastExplicitValue = newValue;
        flushDbCache();
        notifyPrefsChange(ON_SETTINGS_CHANGED_CACHE_INVALID);

        preference.setSummary(newValue.toString());

    }

    private void handleAllowOnLockPrefChange(Preference preference, Boolean newValue)  {

        if (mLastOnLockValue.equals(newValue)) return;

        mLastOnLockValue = newValue;
        notifyPrefsChange(ON_SETTINGS_CHANGED_NOTIFICATIONS_INVALID);

        preference.setSummary(newValue.toString());

    }




    private void flushDbCache() {

        /*
        * If Preferences change, our database cache is no longer valid.
        */
        getApplicationContext()
                .getContentResolver()
                .delete(
                        StreamerContract.BASE_CONTENT_URI,
                        null,
                        null
                );

    }

    private void notifyPrefsChange(String pref) {

        // Notify other activities that that Settings have changed.
        Intent intent = new Intent(pref);

        LocalBroadcastManager.getInstance(SettingsActivity.this)
                .sendBroadcast(intent);
    }

}
