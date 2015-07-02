package udacity.nano.spotifystreamer.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import udacity.nano.spotifystreamer.R;

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

    private String mTrueString;
    private String mFalseString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

         /*
        * Get the most recent values for the preferences.
        */
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mLastValidCountryCode = prefs.getString(MainActivity.PREF_COUNTRY_CODE,
                getResources().getString(R.string.prefs_default_country_code));

        mLastExplicitValue = prefs.getBoolean(MainActivity.PREF_ALLOW_EXPLICIT, true);
        mLastOnLockValue = prefs.getBoolean(MainActivity.PREF_ALLOW_ON_LOCK, true);

        mTrueString = getResources().getString(R.string.true_string);
        mFalseString = getResources().getString(R.string.false_string);

        bindPreferenceSummaryToValue(findPreference(MainActivity.PREF_COUNTRY_CODE));
        bindPreferenceSummaryToValue(findPreference(MainActivity.PREF_ALLOW_EXPLICIT));
        bindPreferenceSummaryToValue(findPreference(MainActivity.PREF_ALLOW_ON_LOCK));

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

        switch (preference.getKey())  {
            case MainActivity.PREF_COUNTRY_CODE:
                preference.setSummary(mLastValidCountryCode);
                break;
            case MainActivity.PREF_ALLOW_EXPLICIT:
                preference.setSummary((mLastExplicitValue) ? mTrueString : mFalseString);
                break;
            case MainActivity.PREF_ALLOW_ON_LOCK:
                preference.setSummary((mLastOnLockValue) ? mTrueString : mFalseString);
                break;
            default:
                // Do Nothing
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        switch (preference.getKey()) {
            case MainActivity.PREF_COUNTRY_CODE: {
                String newCountryCode = ((String) newValue).toUpperCase().trim();
                handleCountryCodePrefChange(preference, (String) newCountryCode);
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

        preference.setSummary((newValue) ? mTrueString : mFalseString);

    }

    private void handleAllowOnLockPrefChange(Preference preference, Boolean newValue)  {

        if (mLastOnLockValue.equals(newValue)) return;

        mLastOnLockValue = newValue;

        preference.setSummary((newValue) ? mTrueString : mFalseString);

    }


}
