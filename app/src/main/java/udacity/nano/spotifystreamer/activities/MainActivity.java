package udacity.nano.spotifystreamer.activities;

import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;

import udacity.nano.spotifystreamer.ArtistListFragment;
import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.TrackListFragment;
import udacity.nano.spotifystreamer.data.StreamerContract;
import udacity.nano.spotifystreamer.services.NotificationTarget;

/*
 * The app's main activity.
 * Kicks off ArtistListFragment
 */
public class MainActivity extends SpotifyStreamerActivity implements ArtistListFragment.Callback {

    private final String TAG = getClass().getCanonicalName();

    /*
    * Handles ot the Artist and Track list fragments.
    */
    static final String TRACK_LIST_FRAGMENT_ID = "TRACK_LIST_FRAG";
    static final String ARTIST_LIST_FRAGMENT_ID = "ARTIST_LIST_FRAG";

    /*
    * We'll store an item during onSavedInstanceSate() so we know if this is
    * the first time the app is starting, or if it's being restarted (i.e.
    * after a configuration change.  If it's the first time through,
    * we'll make sure that any preferences and/or notifications are
    * set to a clean state.
    */
    private static final String FIRST_TIME_THROUGH = "bundle-first-time";

    // Set to true if we're in 2 panel (tablet) mode.
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        /*
        * Determine if we're in single or 2 panel mode.  We can tell by the presence of the
        * track_list_container.
        */
        if (findViewById(R.id.track_list_container) != null) {
            mIsTwoPanel = true;
            Log.d(TAG, "Using Two Panel Mode");

            if (savedInstanceState == null) {
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.track_list_container,
                                new TrackListFragment(), TRACK_LIST_FRAGMENT_ID)
                        .commit();
            }

        } else {
            mIsTwoPanel = false;
            Log.d(TAG, "Using Single Panel Mode");
            getSupportActionBar().setElevation(0f);
        }

        if (savedInstanceState == null) {

            /*
            * First time starting up. It's possible preferences contains invalid data, so
            * clear it out.
            */
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .edit()
                    .putString(MainActivity.PREF_CURRENT_ALBUM, null)
                    .putString(MainActivity.PREF_CURRENT_ARTIST_NAME, null)
                    .putString(MainActivity.PREF_CURRENT_ARTIST_SPOTIFY_ID, null)
                    .putString(MainActivity.PREF_CURRENT_TRACK_NAME, null)
                    .putString(MainActivity.PREF_CURRENT_TRACK_SPOTIFY_ID, null)
                    .putString(MainActivity.PREF_CURRENT_TRACK_URL, null)
                    .putBoolean(MainActivity.PREF_IS_PLAYING, false)
                    .commit();

            // Also may have an orphaned notification
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            mNotificationManager.cancel(NotificationTarget.NOTIFICATION_ID);


        } else {

            /*
            * Not the first time loading, so retrieve the most recent search, and use
            * it to re-populate the artist list.
            */
            if (savedInstanceState.containsKey(ArtistListFragment.BUNDLE_KEY_LAST_SEARCH)) {

                Bundle bundle = new Bundle();
                bundle.putString(
                        ArtistListFragment.BUNDLE_KEY_LAST_SEARCH,
                        savedInstanceState.getString(ArtistListFragment.BUNDLE_KEY_LAST_SEARCH));

                ArtistListFragment fragment = new ArtistListFragment();
                fragment.setArguments(bundle);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.artist_list_container, fragment, ARTIST_LIST_FRAGMENT_ID)
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
                            .replace(R.id.artist_list_container, fragment, ARTIST_LIST_FRAGMENT_ID)
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        /*
        * Set this to ensure we can detect subsequent start ups.
        */
        outState.putBoolean(FIRST_TIME_THROUGH, false);
        super.onSaveInstanceState(outState);

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
                    .replace(R.id.track_list_container, fragment, TRACK_LIST_FRAGMENT_ID)
                    .commitAllowingStateLoss();

        } else {
            Intent intent = new Intent(this, TrackListActivity.class);
            intent.setData(trackListUri);

            /*
            * We don't really need the result, but calling startActivity() allows the
            * database cursor to close leading to an error when TrackListFragment attempts
            * to update the selected track in its BroadcastReceiver.onReceive() method.
            */
            startActivityForResult(intent, TRACK_LIST_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {

            /*
            * If we've just returned from the Settings Activity, then refresh
            * the list of tracks.  The allow explicit content setting or country
            * code may have changed, so we want to update the track list to
            * reflect that.
            */
            if (mIsTwoPanel) {
                onArtistSelected(mLastTrackListUri);
            }
        }
    }
}
