package udacity.nano.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/*
 * The app's main activity.
 * Kicks off ArtistSearchFragment
 */
public class ArtistSearchActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);
    }

}
