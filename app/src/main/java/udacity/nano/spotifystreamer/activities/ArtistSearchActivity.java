package udacity.nano.spotifystreamer.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import udacity.nano.spotifystreamer.ArtistSearchFragment;
import udacity.nano.spotifystreamer.R;


/*
 * The app's main activity.
 */
public class ArtistSearchActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * If this is the first time the application is loaded
         * (i.e. there is no savedInstanceState, then create
         * the ArtistSearchFragment, and store it in the
         * FragmentManager.  This will allow us to retrieve
         * it later if the application is re-started.
         */
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.layout_main, new ArtistSearchFragment())
                    .commit();
        }

    }

}
