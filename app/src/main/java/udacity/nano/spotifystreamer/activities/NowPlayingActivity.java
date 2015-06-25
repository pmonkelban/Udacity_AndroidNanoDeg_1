package udacity.nano.spotifystreamer.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import udacity.nano.spotifystreamer.NowPlayingFragment;
import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.data.StreamerContract;
import udacity.nano.spotifystreamer.data.StreamerProvider;
import udacity.nano.spotifystreamer.services.StreamerMediaService;

public class NowPlayingActivity extends Activity
        implements MediaPlayer.OnCompletionListener, NowPlayingFragment.NowPlayingListener {

    public final String TAG = getClass().getCanonicalName();

    public static final String EXTRA_KEY_TRACK_SPOTIFY_ID = "key_track_id";
    public static final String EXTRA_KEY_ARTIST_SPOTIFY_ID = "key_artist_id";
    public static final String BUNDLE_KEY_CURRENT_TRACK = "key_current_track";
    public static final String BUNDLE_KEY_IS_PLAYING = "key_is_playing";


    public static final String NOW_PLAYING_FRAGMENT = "Now_Playing_Fragment";

    NowPlayingFragment.NowPlayingListener mListener;

    private String mTrackSpotifyId;
    private String mArtistSpotifyId;

    private String mArtistName;
    private String[] mTrackUrls;
    private String[] mTrackNames;
    private String[] mAlbumNames;
    private String[] mTrackImages;
    private boolean[] mTrackExplicit;

    private int mCurrentTrack;
    private int mNumberOfTracks;

    private NowPlayingFragment mNowPlayingFragment;

    private boolean mFirstTime = false;
    private boolean mIsPlaying = false;


    StreamerMediaService mStreamerService;
    boolean isStreamerServiceBound = false;

    private ServiceConnection mStreamerServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            StreamerMediaService.StreamerMediaServiceBinder binder =
                    (StreamerMediaService.StreamerMediaServiceBinder) service;
            mStreamerService = binder.getService();
            isStreamerServiceBound = true;

            // Begin playing the first track
            if (mFirstTime)  {
                onPlayClicked();
            } else  {
                mIsPlaying = mStreamerService.isPlaying();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isStreamerServiceBound = false;
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_KEY_CURRENT_TRACK, mCurrentTrack);
        outState.putBoolean(BUNDLE_KEY_IS_PLAYING, mIsPlaying);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.now_playing);

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.now_playing_container, new NowPlayingFragment(), NOW_PLAYING_FRAGMENT)
                    .commit();
            mFirstTime = true;
            mIsPlaying = false;

        } else {
            mCurrentTrack = savedInstanceState.getInt(BUNDLE_KEY_CURRENT_TRACK);

            /*
            * Get the playing / paused state from the bundle until the StreamerService
            * is connected.  At that point, we can use the service to determine if we're
            * currently playing.
            */
            mIsPlaying = savedInstanceState.getBoolean(BUNDLE_KEY_IS_PLAYING);

            mFirstTime = false;
        }

        Intent callingIntent = getIntent();

        mTrackSpotifyId = callingIntent.getStringExtra(EXTRA_KEY_TRACK_SPOTIFY_ID);
        mArtistSpotifyId = callingIntent.getStringExtra(EXTRA_KEY_ARTIST_SPOTIFY_ID);


        if ((mTrackSpotifyId == null) || (mArtistSpotifyId == null)) {
            throw new IllegalArgumentException("Must provide both the spotify track ID and spotify" +
                    "artist ID for the track you wish to play.");
        }

        /*
        * Load list of tracks for the given artist.
        * Data is pulled from the Content Provider, and stored in String[]s.
        */
        Cursor trackListCursor = getApplicationContext().getContentResolver().query(
                StreamerContract.GET_TRACKS_CONTENT_URI.buildUpon()
                        .appendEncodedPath(mArtistSpotifyId).build(),
                null,
                null,
                null,
                null);

        trackListCursor.moveToFirst();

        mNumberOfTracks = trackListCursor.getCount();
        mTrackUrls = new String[mNumberOfTracks];
        mTrackNames = new String[mNumberOfTracks];
        mAlbumNames = new String[mNumberOfTracks];
        mTrackImages = new String[mNumberOfTracks];
        mTrackExplicit = new boolean[mNumberOfTracks];


        int i = 0;
        while (!trackListCursor.isAfterLast()) {
            mArtistName = trackListCursor.getString(StreamerProvider.IDX_ARTIST_NAME);
            mTrackUrls[i] = trackListCursor.getString(StreamerProvider.IDX_PREVIEW_URL);
            mTrackNames[i] = trackListCursor.getString(StreamerProvider.IDX_TRACK_NAME);
            mAlbumNames[i] = trackListCursor.getString(StreamerProvider.IDX_ALBUM_NAME);
            mTrackImages[i] = trackListCursor.getString(StreamerProvider.IDX_TRACK_IMAGE);
            mTrackExplicit[i] = (trackListCursor.getInt(StreamerProvider.IDX_EXPLICIT) == 1);

            if (mFirstTime && mTrackSpotifyId.equals(
                    trackListCursor.getString(StreamerProvider.IDX_TRACK_SPOTIFY_ID))) {
                mCurrentTrack = i;
            }

            trackListCursor.moveToNext();
            i++;
        }

        trackListCursor.close();

        // Start the StreamerMedia service.
        Intent startMediaServiceIntent = new Intent(this, StreamerMediaService.class);
        startService(startMediaServiceIntent);
        bindService(startMediaServiceIntent, mStreamerServiceConnection,
                Context.BIND_AUTO_CREATE);

    }

    public void requestContentRefresh() {
        refreshContent();
    }

    private void refreshContent() {

        if (mNowPlayingFragment == null) {
            mNowPlayingFragment = (NowPlayingFragment) getFragmentManager()
                    .findFragmentByTag(NOW_PLAYING_FRAGMENT);
        }

        mNowPlayingFragment.setArtistName(mArtistName);
        mNowPlayingFragment.setTrackUrl(mTrackUrls[mCurrentTrack]);
        mNowPlayingFragment.setTrackName(mTrackNames[mCurrentTrack]);
        mNowPlayingFragment.setAlbumName(mAlbumNames[mCurrentTrack]);
        mNowPlayingFragment.setTrackImage(mTrackImages[mCurrentTrack]);
        mNowPlayingFragment.setIsPlaying(getPlayPauseState());

        mNowPlayingFragment.refreshContent();

    }

    private boolean getPlayPauseState()  {

        if (isStreamerServiceBound) {
            mIsPlaying = mStreamerService.isPlaying();
        }

        return mIsPlaying;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onDestroy() {
        try {
            if (isStreamerServiceBound) {
                unbindService(mStreamerServiceConnection);
            }
        } catch (Exception e) {
            // Ignore exception.
        }

        super.onDestroy();
    }

    @Override
    public void onPlayClicked() {
        if (isStreamerServiceBound) {
            mStreamerService.play(Uri.parse(mTrackUrls[mCurrentTrack]));
            mIsPlaying = true;
            mNowPlayingFragment.setIsPlaying(mIsPlaying);
        }

    }

    @Override
    public void onPauseClicked() {
        if (isStreamerServiceBound) {
            mStreamerService.pause();
            mIsPlaying = false;
            mNowPlayingFragment.setIsPlaying(mIsPlaying);
        }

    }

    @Override
    public void onNextClicked() {
        mCurrentTrack = (mCurrentTrack + 1) % mNumberOfTracks;
        refreshContent();
        onPlayClicked();
    }

    @Override
    public void onPrevClicked() {
        mCurrentTrack -= 1;
        if (mCurrentTrack < 0) mCurrentTrack = mNumberOfTracks - 1;
        refreshContent();
        onPlayClicked();
    }

    @Override
    public void seekTo(int miliSeconds) {
        if (isStreamerServiceBound) {
            mStreamerService.seekTo(miliSeconds);
        }
    }
}
