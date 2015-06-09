package udacity.nano.spotifystreamer.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.TopTracksFragment;

/*
 * This activity is called when a user selects an artist.
 * It users the TopTracksFragment to display a list of
 * tracks associated with the selected artist.
 */
public class TopTracksActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.layout_top_tracks, new TopTracksFragment())
                    .commit();
        }
    }
}
