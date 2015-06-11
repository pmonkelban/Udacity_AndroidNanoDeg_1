package udacity.nano.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

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

    }
}
