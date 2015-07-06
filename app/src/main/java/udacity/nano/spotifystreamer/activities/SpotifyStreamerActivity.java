package udacity.nano.spotifystreamer.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import udacity.nano.spotifystreamer.R;

public abstract class SpotifyStreamerActivity extends ActionBarActivity {

    static final String TRACK_LIST_FRAGMENT = "TRACK_LIST_FRAG";
    static final String ARTIST_LIST_FRAGMENT = "ARTIST_LIST_FRAG";
    protected final String TAG = getClass().getCanonicalName();

    public static final String PREF_CURRENT_TRACK_NAME = "prefs_current_track_name";
    public static final String PREF_CURRENT_TRACK_SPOTIFY_ID = "prefs_current_track_spotify_id";
    public static final String PREF_CURRENT_TRACK_URL = "prefs_current_track_url";
    public static final String PREF_CURRENT_ARTIST_NAME = "prefs_current_artist_name";
    public static final String PREF_CURRENT_ARTIST_SPOTIFY_ID = "prefs_current_artist_spotify_id";
    public static final String PREF_CURRENT_ALBUM = "prefs_current_album";
    public static final String PREF_COUNTRY_CODE = "prefs_country_code";
    public static final String PREF_ALLOW_EXPLICIT = "prefs_allow_explicit";
    public static final String PREF_ALLOW_ON_LOCK = "prefs_show_on_lock";

    // Used to format the currently playing track when shared.
    protected final String NEWLINE = System.getProperty("line.separator");

    protected Intent handleShareAction() {
        // The Uri of the most recently played track is stored in preferences.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String currentTrackUrl = settings.getString(PREF_CURRENT_TRACK_URL, null);


        if ((currentTrackUrl == null) || (currentTrackUrl.length() == 0)) {
            Toast.makeText(this, getString(R.string.share_no_tracks_played), Toast.LENGTH_SHORT).show();
            return null;

        } else  {

            String currentArtist = settings.getString(PREF_CURRENT_ARTIST_SPOTIFY_ID, "");
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

            return sendIntent;

        }
    }

    protected void handleNowPlayingAction()  {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String currentArtist = settings.getString(PREF_CURRENT_ARTIST_SPOTIFY_ID, "");
        String currentTrack = settings.getString(PREF_CURRENT_TRACK_SPOTIFY_ID, "");

        Intent intent = new Intent(this, NowPlayingActivity.class);
        intent.putExtra(NowPlayingActivity.EXTRA_KEY_ARTIST_SPOTIFY_ID, currentArtist);
        intent.putExtra(NowPlayingActivity.EXTRA_KEY_TRACK_SPOTIFY_ID, currentTrack);
        intent.putExtra(NowPlayingActivity.EXTRA_KEY_RESET_ON_STARTUP, false);

        startActivity(intent);
    }

}
