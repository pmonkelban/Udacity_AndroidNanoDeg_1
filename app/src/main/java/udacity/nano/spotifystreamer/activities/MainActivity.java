package udacity.nano.spotifystreamer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import udacity.nano.spotifystreamer.ArtistListFragment;
import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.TrackListFragment;
import udacity.nano.spotifystreamer.data.StreamerContract;

/*
 * The app's main activity.
 * Kicks off ArtistListFragment
 */
public class MainActivity extends SpotifyStreamerActivity implements ArtistListFragment.Callback {

    private boolean mIsTwoPanel = false;

    /*
    * Request codes returned from Activities started with startActivityForResult()
    */
    private static final int TRACK_LIST_REQUEST_CODE = 1;
    private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 2;

    private ShareActionProvider mShareActionProvider;

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

        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class),
                        SETTINGS_ACTIVITY_REQUEST_CODE);
                break;

//            case R.id.action_share:
//                handleShareAction();
//                break;

            case R.id.action_now_playing:
                handleNowPlayingAction();
                break;

            default:
                throw new IllegalArgumentException("Unknown menu item. id=" + item.getItemId());

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

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
