package udacity.nano.spotifystreamer.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import udacity.nano.spotifystreamer.NowPlayingFragment;
import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.data.StreamerContract;
import udacity.nano.spotifystreamer.data.StreamerProvider;
import udacity.nano.spotifystreamer.services.StreamerMediaService;

public class NowPlayingActivity extends Activity
        implements NowPlayingFragment.NowPlayingListener {

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

    /*
    * Note durations are for the real track.  All samples are 30 seconds.  This value is OK
    * to display for info, but should not be used to set the SeekBar.
    */
    private int[] mDurations;

    private boolean[] mTrackExplicit;

    private int mCurrentTrack;
    private int mNumberOfTracks;

    private NowPlayingFragment mNowPlayingFragment;

    private boolean mFirstTime = false;
    private boolean mIsPlaying = false;


    StreamerMediaService mStreamerService;
    boolean isStreamerServiceBound = false;

    // Handles updates to the progress bar.
    private Handler mHandler = new Handler();


    private ServiceConnection mStreamerServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            StreamerMediaService.StreamerMediaServiceBinder binder =
                    (StreamerMediaService.StreamerMediaServiceBinder) service;
            mStreamerService = binder.getService();
            isStreamerServiceBound = true;

            // Begin playing the first track
            if (mFirstTime) {
                queueNextSong();
                onPlayClicked();
            } else {
                setIsPlaying(mStreamerService.isPlaying());
            }

            /*
            * Create a process to update the seek bar location every second.
            * Also keeps the duration and play/pause status up to date.  The play/pause
            * status can be off if the user clicks it too quickly.  This will straighten
            * it out every second.
            */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mStreamerService != null) {
                        mNowPlayingFragment.setTrackDuration(mStreamerService.getDuration());
                        mNowPlayingFragment.setSeekBarLocation(mStreamerService.getLocation());
                        mNowPlayingFragment.setIsPlaying(mStreamerService.isPlaying());
                    }

                    if (isStreamerServiceBound) mHandler.postDelayed(this, 1000);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isStreamerServiceBound = false;
        }
    };

    /*
    * When we receive notice that the track has finished playing, move on
    * to the next track.
    */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onNextClicked();
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
            throw new IllegalArgumentException("Must provide both the spotify track ID and " +
                    "spotify artist ID for the track you wish to play.");
        }

        // Register to receive track completed broadcast notifications
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver,
                new IntentFilter(StreamerMediaService.ON_COMPLETE_BROADCAST_FILTER));

        loadTrackData();


        // Start the StreamerMedia service.
        Intent startMediaServiceIntent = new Intent(this, StreamerMediaService.class);
        startService(startMediaServiceIntent);
        bindService(startMediaServiceIntent, mStreamerServiceConnection,
                Context.BIND_AUTO_CREATE);

    }

    private void loadTrackData() {

        /*
        * Load list of tracks for the given artist.
        * Data is pulled from the Content Provider, and stored in String[]s.

        */
        Cursor trackListCursor = null;

        try {
            trackListCursor = getApplicationContext().getContentResolver().query(
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
            mDurations = new int[mNumberOfTracks];
            mTrackExplicit = new boolean[mNumberOfTracks];


            int i = 0;
            while (!trackListCursor.isAfterLast()) {
                mArtistName = trackListCursor.getString(StreamerProvider.IDX_ARTIST_NAME);
                mTrackUrls[i] = trackListCursor.getString(StreamerProvider.IDX_PREVIEW_URL);
                mTrackNames[i] = trackListCursor.getString(StreamerProvider.IDX_TRACK_NAME);
                mAlbumNames[i] = trackListCursor.getString(StreamerProvider.IDX_ALBUM_NAME);
                mTrackImages[i] = trackListCursor.getString(StreamerProvider.IDX_TRACK_IMAGE);
                mDurations[i] = trackListCursor.getInt(StreamerProvider.IDX_DURATION);
                mTrackExplicit[i] = (trackListCursor.getInt(StreamerProvider.IDX_EXPLICIT) == 1);

                if (mFirstTime && mTrackSpotifyId.equals(
                        trackListCursor.getString(StreamerProvider.IDX_TRACK_SPOTIFY_ID))) {
                    mCurrentTrack = i;
                }

                trackListCursor.moveToNext();
                i++;
            }
        } finally {

            if (trackListCursor != null) {
                trackListCursor.close();
            }
        }
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
        mNowPlayingFragment.setTrackName(mTrackNames[mCurrentTrack]);
        mNowPlayingFragment.setAlbumName(mAlbumNames[mCurrentTrack]);
        mNowPlayingFragment.setTrackImage(mTrackImages[mCurrentTrack]);
        mNowPlayingFragment.setIsPlaying(getPlayPauseState());
    }

    private boolean getPlayPauseState() {

        if (isStreamerServiceBound) {
            setIsPlaying(mStreamerService.isPlaying());
        }

        return mIsPlaying;
    }

    private void setIsPlaying(boolean isPlaying) {
        mIsPlaying = isPlaying;
        mNowPlayingFragment.setIsPlaying(isPlaying);
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

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        super.onDestroy();
    }

    private void queueNextSong() {
        if (isStreamerServiceBound) {
            if (!mStreamerService.reset(Uri.parse(mTrackUrls[mCurrentTrack])))  {
                Toast.makeText(this, R.string.media_error_playing, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPlayClicked() {
        if (!mStreamerService.play())  {
            Toast.makeText(this, R.string.media_error_general, Toast.LENGTH_SHORT).show();
        }
        setIsPlaying(true);
    }

    @Override
    public void onPauseClicked() {
        if (isStreamerServiceBound) {
            if (!mStreamerService.pause())  {
                Toast.makeText(this, R.string.media_error_general, Toast.LENGTH_SHORT).show();
            }
            setIsPlaying(false);
        }
    }

    @Override
    public void onNextClicked() {
        mCurrentTrack = (mCurrentTrack + 1) % mNumberOfTracks;
        refreshContent();
        queueNextSong();
        onPlayClicked();
    }

    @Override
    public void onPrevClicked() {
        mCurrentTrack -= 1;
        if (mCurrentTrack < 0) mCurrentTrack = mNumberOfTracks - 1;
        refreshContent();
        queueNextSong();
        onPlayClicked();
    }

    @Override
    public void seekTo(int miliSeconds) {

        if (isStreamerServiceBound) {
            if (!mStreamerService.seekTo(miliSeconds))  {
                Toast.makeText(this, R.string.media_error_general, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
