package udacity.nano.spotifystreamer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import udacity.nano.spotifystreamer.data.StreamerContract;

/*
 * The app's main activity.
 * Kicks off ArtistListFragment
 */
public class MainActivity extends ActionBarActivity
        implements ArtistListFragment.Callback, TopTracksFragment.Callback  {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private boolean mIsTwoPanel = false;

    private static final String TOP_TRACKS_FRAG_TAG = "TOP_TRACKS_FRAG";
    private static final String ARTIST_LIST_FRAG_TAG = "ARTIST_LIST_FRAG";


    // The text field where the user enters their search.
    private SearchView mArtistSearchText;

    /*
    * Prevents multiple searches within a short period of time.
    * This normally happens if the user has a real keyboard.
    * Pressing enter will result in both a Key Down and a Key Up
    * event firing, resulting in two queries being sent to the
    * Spotify API.
    */
    private static final Long SEARCH_REQUEST_WINDOW = 500L;  // one-half second
    private long mLastSearchTime;


    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if (findViewById(R.id.track_list_container) != null)  {
            mIsTwoPanel = true;
            Log.d(TAG, "Using Two Panel Mode");

            if (savedInstanceState == null)  {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.artist_list_container, new TopTracksFragment(), TOP_TRACKS_FRAG_TAG)
                        .commit();
            }

        } else  {
            mIsTwoPanel = false;
            Log.d(TAG, "Using Single Panel Mode");
            getSupportActionBar().setElevation(0f);
        }

        if (savedInstanceState != null)  {
            if (savedInstanceState.containsKey(ArtistListFragment.BUNDLE_KEY_LAST_SEARCH))  {

                Bundle bundle = new Bundle();
                bundle.putString(
                        ArtistListFragment.BUNDLE_KEY_LAST_SEARCH,
                        savedInstanceState.getString(ArtistListFragment.BUNDLE_KEY_LAST_SEARCH));

                ArtistListFragment fragment = new ArtistListFragment();
                fragment.setArguments(bundle);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.artist_list_container, fragment, ARTIST_LIST_FRAG_TAG)
                        .commit();
            }
        }

        // The field where the user enters the artist they're looking for.
        mArtistSearchText = (SearchView) findViewById(R.id.artist_search_searchView);

        /*
         * Sets a listener on the SearchView that kicks off fetchArtists() when the
         * user is done entering their search query.
         */
        mArtistSearchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {

                /*
                 * Throw out search requests if they come too quickly. This addresses
                 * a bug where hitting enter with a real keyboard results in two
                 * events firing.
                 */

                long time = System.currentTimeMillis();

                if ((mLastSearchTime + SEARCH_REQUEST_WINDOW) < time) {
                    mLastSearchTime = time;

                    Uri artistListUri = StreamerContract
                            .GET_ARTISTS_CONTENT_URI
                            .buildUpon()
                            .appendEncodedPath(s)
                            .build();

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(ArtistListFragment.BUNDLE_KEY_LAST_SEARCH, artistListUri);

                    ArtistListFragment fragment = new ArtistListFragment();
                    fragment.setArguments(bundle);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.artist_list_container, fragment, ARTIST_LIST_FRAG_TAG)
                            .commit();

                    return true;
                } else {
                    Log.d(TAG, "Duplicate search request detected - Ignoring");
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

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

    @Override
    public void onArtistSelected(Uri trackListUri) {
        Log.d(TAG, "Artist selected. artistId:" + trackListUri);

        if (mIsTwoPanel) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(TopTracksFragment.BUNDLE_KEY_ARTIST_ID, trackListUri);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.track_list_container, fragment, TOP_TRACKS_FRAG_TAG)
                    .commit();
        } else  {
            Intent intent = new Intent(this, TopTracksActivity.class);
            intent.setData(trackListUri);
            startActivity(intent);
        }
    }

    @Override
    public void onTrackSelected(Uri trackUri) {
        Log.d(TAG, "Track Selected.  trackId:" + trackUri);
    }
}
