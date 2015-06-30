package udacity.nano.spotifystreamer.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.TrackListFragment;

/*
 * This activity is called when a user selects an artist.
 * It users the TrackListFragment to display a list of
 * tracks associated with the selected artist.
 */
public class TrackListActivity extends ActionBarActivity {

    private final String TAG = this.getClass().getCanonicalName();

    static final String TRACK_LIST_FRAGMENT = "TRACK_LIST_FRAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_list_activity);

        if (savedInstanceState == null) {
            // Create the track list fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putParcelable(TrackListFragment.BUNDLE_KEY_ARTIST_ID, getIntent().getData());

            TrackListFragment fragment = new TrackListFragment();
            fragment.setArguments(arguments);

            getFragmentManager().beginTransaction()
                    .replace(R.id.track_list_container, fragment, TRACK_LIST_FRAGMENT)
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.track_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_share) {

            // The Uri of the most recently played track is stored in preferences.
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String currentTrack = settings.getString(MainActivity.PREF_CURRENT_TRACK_URL, null);

            if ((currentTrack == null) || (currentTrack.length() == 0)) {
                Toast.makeText(this, getString(R.string.share_no_tracks_played), Toast.LENGTH_SHORT).show();

            } else  {
                /*
                * Use a shareIntent to expose the external Spotify URL for the current track.
                */
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, currentTrack);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

            }

        }

        return super.onOptionsItemSelected(item);
    }
}