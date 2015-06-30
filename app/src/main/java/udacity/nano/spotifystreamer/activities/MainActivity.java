package udacity.nano.spotifystreamer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import udacity.nano.spotifystreamer.ArtistListFragment;
import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.TrackListFragment;
import udacity.nano.spotifystreamer.data.StreamerContract;

/*
 * The app's main activity.
 * Kicks off ArtistListFragment
 */
public class MainActivity extends ActionBarActivity implements ArtistListFragment.Callback {

    private final String TAG = getClass().getCanonicalName();

    static final String TRACK_LIST_FRAGMENT = "TRACK_LIST_FRAG";
    static final String ARTIST_LIST_FRAGMENT = "ARTIST_LIST_FRAG";

    public static final String PREF_CURRENT_TRACK = "prefs_current_track";
    public static final String PREF_COUNTRY_CODE = "prefs_country_code";
    public static final String PREF_ALLOW_EXPLICIT = "prefs_allow_explicit";

    private boolean mIsTwoPanel = false;


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

    private Uri mLastTrackListUri = null;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() called");

            switch (intent.getAction()) {
                case SettingsActivity.ON_SETTINGS_CHANGED_BROADCAST_FILTER: {

                    // When settings change, clear the track list.
                    if (mIsTwoPanel) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(TrackListFragment.BUNDLE_KEY_ARTIST_ID, mLastTrackListUri);

                        TrackListFragment fragment = new TrackListFragment();
                        fragment.setArguments(bundle);

                        getFragmentManager()
                                .beginTransaction()
                                .replace(R.id.track_list_container, fragment, TRACK_LIST_FRAGMENT)
                                .commit();
                    }

                    break;
                }

                default:
                    throw new IllegalArgumentException("Unexpected broadcast message received: " +
                            intent.getAction());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if (findViewById(R.id.track_list_container) != null) {
            mIsTwoPanel = true;
            Log.d(TAG, "Using Two Panel Mode");

            if (savedInstanceState == null) {
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.track_list_container, new TrackListFragment(), TRACK_LIST_FRAGMENT)
                        .commit();
            }

        } else {
            mIsTwoPanel = false;
            Log.d(TAG, "Using Single Panel Mode");
            getSupportActionBar().setElevation(0f);
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ArtistListFragment.BUNDLE_KEY_LAST_SEARCH)) {

                Bundle bundle = new Bundle();
                bundle.putString(
                        ArtistListFragment.BUNDLE_KEY_LAST_SEARCH,
                        savedInstanceState.getString(ArtistListFragment.BUNDLE_KEY_LAST_SEARCH));

                ArtistListFragment fragment = new ArtistListFragment();
                fragment.setArguments(bundle);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.artist_list_container, fragment, ARTIST_LIST_FRAGMENT)
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

                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.artist_list_container, fragment, ARTIST_LIST_FRAGMENT)
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

        // Register to receive Settings Changed broadcast notifications
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBroadcastReceiver,
                        new IntentFilter(SettingsActivity.ON_SETTINGS_CHANGED_BROADCAST_FILTER));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_share) {

            // The Uri of the most recently played track is stored in preferences.
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String currentTrack = settings.getString(PREF_CURRENT_TRACK, "");

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

    @Override
    public void onArtistSelected(Uri trackListUri) {

        mLastTrackListUri = trackListUri;

        Log.d(TAG, "Artist selected. artistId:" + trackListUri);

        if (mIsTwoPanel) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(TrackListFragment.BUNDLE_KEY_ARTIST_ID, trackListUri);

            TrackListFragment fragment = new TrackListFragment();
            fragment.setArguments(bundle);

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.track_list_container, fragment, TRACK_LIST_FRAGMENT)
                    .commit();
        } else {
            Intent intent = new Intent(this, TrackListActivity.class);
            intent.setData(trackListUri);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)  {
        // do not call super.onSaveInstanceState
    }

}
