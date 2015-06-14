package udacity.nano.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/*
 * The app's main activity.
 * Kicks off ArtistSearchFragment
 */
public class MainActivity extends ActionBarActivity {

    private final String TAG = getClass().getCanonicalName();

    private boolean mIsTwoPanel;

    private static final String TOP_TRACKS_FRAG_TAG = "TTFRAG";


    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if (findViewById(R.id.artist_search) != null)  {
            Log.d(TAG, "Found artist_search");
        }

        if (findViewById(R.id.track_list) != null)  {
            mIsTwoPanel = true;
            Log.d(TAG, "Using Two Panel Mode");

            if (savedInstanceState == null)  {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.track_list, new TopTracksFragment(), TOP_TRACKS_FRAG_TAG)
                        .commit();
            }

        } else  {
            mIsTwoPanel = false;
            Log.d(TAG, "Using Single Panel Mode");
            getSupportActionBar().setElevation(0f);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


}
