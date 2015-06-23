package udacity.nano.spotifystreamer.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.TrackListFragment;

/*
 * This activity is called when a user selects an artist.
 * It users the TrackListFragment to display a list of
 * tracks associated with the selected artist.
 */
public class TrackListActivity extends SpotifyStreamerActivity {

    private final String TAG = this.getClass().getCanonicalName();

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

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.track_list_container, fragment, TRACK_LIST_FRAGMENT)
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}