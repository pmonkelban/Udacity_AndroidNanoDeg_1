package udacity.nano.spotifystreamer.activities;

import android.content.Intent;
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

    public static final String ON_SETTINGS_CHANGED_BROADCAST_FILTER = "settings-changed-broadcast-filter";

    private static final Set<String> VALID_COUNTRY_CODES =
            new HashSet<>(Arrays.asList(Locale.getISOCountries()));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference(MainActivity.PREF_COUNTRY_CODE));
        bindPreferenceSummaryToValue(findPreference(MainActivity.PREF_ALLOW_EXPLICIT));

    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        if (preference instanceof EditTextPreference) {
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));

        } else if (preference instanceof CheckBoxPreference) {
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), true));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        // Warn if we don't get a valid country code.
        if (MainActivity.PREF_COUNTRY_CODE.equals(preference.getKey()))  {

            newValue = ((String) newValue).toUpperCase();

               if (!VALID_COUNTRY_CODES.contains(newValue))  {
                   String msg = getString(R.string.invalid_country_code, newValue);
                   Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
               }
        }

        preference.setSummary(newValue.toString());

        /*
        * If Preferences change, our database cache is no longer valid.
        */
        getApplicationContext().getContentResolver().delete(
                StreamerContract.BASE_CONTENT_URI,
                null,
                null
        );

        // Notify other activities that that Settings have changed.
        Intent intent = new Intent(ON_SETTINGS_CHANGED_BROADCAST_FILTER);
        LocalBroadcastManager.getInstance(SettingsActivity.this).sendBroadcast(intent);

        return true;
    }
}
