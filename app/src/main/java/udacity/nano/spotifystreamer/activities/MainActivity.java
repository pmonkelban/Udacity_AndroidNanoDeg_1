package udacity.nano.spotifystreamer.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

    // Used to format the currently playing track when shared.
    private final String NEWLINE = System.getProperty("line.separator");

    public static final String PREF_CURRENT_TRACK_NAME = "prefs_current_track_name";
    public static final String PREF_CURRENT_ALBUM = "prefs_current_album";
    public static final String PREF_CURRENT_ARTIST = "prefs_current_artist";
    public static final String PREF_CURRENT_TRACK_URL = "prefs_current_track_url";

    public static final String PREF_COUNTRY_CODE = "prefs_country_code";
    public static final String PREF_ALLOW_EXPLICIT = "prefs_allow_explicit";
    public static final String PREF_ALLOW_ON_LOCK = "prefs_show_on_lock";

    private boolean mIsTwoPanel = false;

    /*
    * Request codes returned from Activities started with startActivityForResult()
    */
    private static final int TRACK_LIST_REQUEST_CODE = 1;
    private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 2;

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
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_ACTIVITY_REQUEST_CODE);
            return true;
        }

        if (id == R.id.action_share) {

            // The Uri of the most recently played track is stored in preferences.
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String currentTrackUrl = settings.getString(PREF_CURRENT_TRACK_URL, null);


            if ((currentTrackUrl == null) || (currentTrackUrl.length() == 0)) {
                Toast.makeText(this, getString(R.string.share_no_tracks_played), Toast.LENGTH_SHORT).show();

            } else  {

                String currentArtist = settings.getString(PREF_CURRENT_ARTIST, "");
                String currentAlbum = settings.getString(PREF_CURRENT_ALBUM, "");
                String currentTrackName = settings.getString(PREF_CURRENT_TRACK_NAME, "");

                /*
                * Use a shareIntent to expose the external Spotify URL for the current track.
                */

                String shareMsg =
                        currentTrackUrl + NEWLINE + NEWLINE +
                        "Artist: " + currentArtist + NEWLINE +
                        "Track: " + currentTrackName + NEWLINE +
                        "Album: " + currentAlbum;

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data)  {

        if (requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {

            /*
            * If we've just returned from the Settings Activity, then refresh
            * the list of tracks.  The allow explicit content setting or country
            * code may have changed, so we want to update the track list to
            * reflect that.
            */
            onArtistSelected(mLastTrackListUri);
        }
    }



}
