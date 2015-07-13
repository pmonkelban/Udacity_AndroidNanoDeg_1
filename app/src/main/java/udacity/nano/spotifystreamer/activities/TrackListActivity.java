package udacity.nano.spotifystreamer.activities;

import android.os.Bundle;
import android.view.Menu;

import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.TrackListFragment;

/*
 * This activity is called when a user selects an artist.
 * It users the TrackListFragment to display a list of
 * tracks associated with the selected artist.
 */
public class TrackListActivity extends SpotifyStreamerActivity {

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
                    .replace(R.id.track_list_container, fragment, TRACK_LIST_FRAGMENT_ID)
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        /*
        * This menu differs from that of the Main Activity.  TrackListActivity is for single
        * panel devices.  With the user on the Track list, if they were to enter settings,
        * that could affect the tracks listed (by changing country, or allow explicit).  It's
        * messy to update the list at that point, so I've removed Settings from here.  They
        * can go back to the Artist search page and access Settings from there.
        */
        getMenuInflater().inflate(R.menu.track_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

}