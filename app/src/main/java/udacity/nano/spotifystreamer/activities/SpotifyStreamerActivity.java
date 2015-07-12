package udacity.nano.spotifystreamer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;

import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.services.StreamerMediaService;

public abstract class SpotifyStreamerActivity extends ActionBarActivity {

    protected final String TAG = getClass().getCanonicalName();

    public static final String ACTION = "action";
    public static final String ACTION_NO_OP = "action_no_op";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_forward";
    public static final String ACTION_PREVIOUS = "action_prev";
    public static final String ACTION_NEXT = "action_next";

    public static final String KEY_TRACK_SPOTIFY_ID = "key_track_id";
    public static final String KEY_ARTIST_SPOTIFY_ID = "key_artist_id";
    public static final String KEY_RESET_ON_STARTUP = "key_reset_on_start";
    public static final String KEY_CURRENT_TRACK = "key_current_track";
    public static final String KEY_IS_PLAYING = "key_is_playing";

    public static final String PREF_CURRENT_TRACK_NAME = "prefs_current_track_name";
    public static final String PREF_CURRENT_TRACK_SPOTIFY_ID = "prefs_current_track_spotify_id";
    public static final String PREF_CURRENT_TRACK_URL = "prefs_current_track_url";
    public static final String PREF_CURRENT_ARTIST_NAME = "prefs_current_artist_name";
    public static final String PREF_CURRENT_ARTIST_SPOTIFY_ID = "prefs_current_artist_spotify_id";
    public static final String PREF_CURRENT_ALBUM = "prefs_current_album";
    public static final String PREF_COUNTRY_CODE = "prefs_country_code";
    public static final String PREF_ALLOW_EXPLICIT = "prefs_allow_explicit";
    public static final String PREF_ALLOW_ON_LOCK = "prefs_show_on_lock";
    public static final String PREF_IS_PLAYING = "prefs_is_playing";


    private ShareActionProvider mShareActionProvider;


    /*
    * Request codes returned from Activities started with startActivityForResult()
    */
    protected static final int TRACK_LIST_REQUEST_CODE = 1;
    protected static final int SETTINGS_ACTIVITY_REQUEST_CODE = 2;

    private MenuItem mNowPlayingMenuItem;
    private MenuItem mShareTrackMenuItem;

//    protected PlayListItem mCurrentlyPlaying;

    // Used to format the currently playing track when shared.
    protected final String NEWLINE = System.getProperty("line.separator");

    /*
    * When we receive notice that the track has finished playing, move on
    * to the next track.
    */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()) {
                case StreamerMediaService.TRACK_STOP_BROADCAST_FILTER: {
                    onTrackStop();
                    break;
                }
                case StreamerMediaService.TRACK_START_BROADCAST_FILTER: {
                    onTrackStart();
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unexpected broadcast message received: " +
                            intent.getAction());
            }
        }
    };

    protected void onTrackStart() {
        mNowPlayingMenuItem.setVisible(true);

        mShareActionProvider.setShareIntent(createShareTrackIntent());
        mShareTrackMenuItem.setVisible(true);
    }

    protected void onTrackStop() {
        mNowPlayingMenuItem.setVisible(false);
        mShareTrackMenuItem.setVisible(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register to receive track start broadcast notifications
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver,
                new IntentFilter(StreamerMediaService.TRACK_START_BROADCAST_FILTER));

        // Register to receive track completed broadcast notifications
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver,
                new IntentFilter(StreamerMediaService.TRACK_STOP_BROADCAST_FILTER));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        mNowPlayingMenuItem = (MenuItem) menu.findItem(R.id.item_now_playing);
        mShareTrackMenuItem = (MenuItem) menu.findItem(R.id.item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareTrackMenuItem);


        // Get the current playing/not playing state
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (prefs.getBoolean(PREF_IS_PLAYING, false))  {
            onTrackStart();
        } else  {
            onTrackStop();
        }

        return super.onCreateOptionsMenu(menu);
    }

    protected Intent createShareTrackIntent() {

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!prefs.getBoolean(PREF_IS_PLAYING, false)) return null;

        /*
        * Use a shareIntent to expose the external Spotify URL for the current track.
        */
        String shareMsg =
                prefs.getString(PREF_CURRENT_TRACK_URL, "") + NEWLINE + NEWLINE +
                        "Artist: " + prefs.getString(PREF_CURRENT_ARTIST_NAME, "") + NEWLINE +
                        "Track: " + prefs.getString(PREF_CURRENT_TRACK_NAME, "") + NEWLINE +
                        "Album: " + prefs.getString(PREF_CURRENT_ALBUM, "");

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
        shareIntent.setType("text/plain");

        return shareIntent;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.item_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class),
                        SETTINGS_ACTIVITY_REQUEST_CODE);
                break;

//            case R.id.item_share:
//                handleShareAction();
//                break;

            case R.id.item_now_playing:
                handleNowPlayingAction();
                break;

            default:
                // Do nothing.

        }

        return super.onOptionsItemSelected(item);
    }

    protected void handleNowPlayingAction() {

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!prefs.getBoolean(PREF_IS_PLAYING, false)) return;

        Intent intent = new Intent(this, NowPlayingActivity.class);
        intent.putExtra(KEY_ARTIST_SPOTIFY_ID, prefs.getString(PREF_CURRENT_ARTIST_SPOTIFY_ID, ""));
        intent.putExtra(KEY_TRACK_SPOTIFY_ID, prefs.getString(PREF_CURRENT_TRACK_SPOTIFY_ID, ""));
        intent.putExtra(KEY_RESET_ON_STARTUP, false);

        startActivity(intent);
    }

}
