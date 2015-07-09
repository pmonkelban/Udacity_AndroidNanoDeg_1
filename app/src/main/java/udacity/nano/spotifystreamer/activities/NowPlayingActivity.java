package udacity.nano.spotifystreamer.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.MediaController;

import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.services.StreamerMediaService;

public class NowPlayingActivity extends Activity implements MediaController.MediaPlayerControl {

    public final String TAG = getClass().getCanonicalName();

    private MediaPlayer mMediaPlayer;
    private MediaSession mMediaSession;

    //    public static final String EXTRA_KEY_TRACK_SPOTIFY_ID = "key_track_id";
//    public static final String EXTRA_KEY_ARTIST_SPOTIFY_ID = "key_artist_id";
//    public static final String EXTRA_KEY_RESET_ON_STARTUP = "key_reset_on_start";
//
//    public static final String BUNDLE_KEY_CURRENT_TRACK = "key_current_track";
//    public static final String BUNDLE_KEY_IS_PLAYING = "key_is_playing";
//
//    public static final String TRACK_CHANGE_BROADCAST_FILTER = "track_change_broadcast_filter";
//    public static final String TRACK_CHANGE_CURRENT_TRACK_NUM = "track_change_current_track";
//
//    public static final String NOW_PLAYING_FRAGMENT = "Now_Playing_Fragment";
//
//    NowPlayingFragment.NowPlayingListener mListener;
//
//    private String mTrackSpotifyId;
//    private String mArtistSpotifyId;
//
//    private String mArtistName;
//    private String[] mTrackUrls;
//    private String[] mTrackNames;
//    private String[] mAlbumNames;
//    private String[] mTrackImages;
//    private String[] mTrackIds;
//
//    private int[] mDurations;
//
//    private boolean[] mTrackExplicit;
//
//    private int mCurrentTrack;
//    private int mNumberOfTracks;
//
//    private boolean mPlayOnServiceConnect = false;
//    private boolean mPauseOnServiceConnect = false;
//
//    private NowPlayingFragment mNowPlayingFragment;
//
//    private boolean mFirstTime = false;
//    private boolean mIsPlaying = false;
//
    StreamerMediaService mStreamerService;
    private MediaController mController;

//    boolean isStreamerServiceBound = false;
//
//    // Handles updates to the progress bar.
    private Handler mHandler = new Handler();
//
//    private boolean mResetOnStartup;
//
//
//

    private ServiceConnection mStreamerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StreamerMediaService.StreamerMediaServiceBinder binder =
                    (StreamerMediaService.StreamerMediaServiceBinder) service;

            mStreamerService = binder.getService();

            mController = new MediaController(NowPlayingActivity.this, false);

            mController.setMediaPlayer(mStreamerService.getMediaPlayer());

            mController.setAnchorView(findViewById(R.id.now_playing_layout));

            mHandler.post(new Runnable()  {
                public void run()  {
                    mController.setEnabled(true);
                    mController.show();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mStreamerService = null;

        }
    };

    

    //    private ServiceConnection mStreamerServiceConnection = new ServiceConnection() {
//
//        public void onServiceConnected(ComponentName className, IBinder service) {
//
//            if (mNumberOfTracks == 0) return;
//
//            StreamerMediaService.StreamerMediaServiceBinder binder =
//                    (StreamerMediaService.StreamerMediaServiceBinder) service;
//
//
//            mStreamerService = binder.getService();
//            isStreamerServiceBound = true;
//
//            if (mResetOnStartup) {
//                // Begin playing the first track
//                if (mFirstTime) {
//                    queueNextSong();
//                    onPlayClicked();
//
//                } else {
//
//                /*
//                * In case Play or Pause was clicked before the service was established.
//                */
//                    if (mPlayOnServiceConnect) {
//                        mPlayOnServiceConnect = false;
//                        onPlayClicked();
//
//                    } else if (mPauseOnServiceConnect) {
//                        mPauseOnServiceConnect = false;
//                        onPauseClicked();
//                    }
//
//                    setIsPlaying(mStreamerService.isPlaying());
//
//                }
//
//            }  else {
//                issueNotification();
//            }
//
//            /*
//            * Create a process to update the seek bar location every second.
//            * Also keeps the duration and play/pause status up to date.  The play/pause
//            * status can be off if the user clicks it too quickly.  This will straighten
//            * it out every second.
//            */
//            runOnUiThread(new Runnable() {
//
//                boolean setDuration = true;
//
//                @Override
//                public void run() {
//                    if (mStreamerService != null) {
//                        if (setDuration) {
//                            mNowPlayingFragment.setTrackDuration(mStreamerService.getDuration());
//                            setDuration = false;
//                        }
//                        mNowPlayingFragment.setSeekBarLocation(mStreamerService.getLocation());
//                        mNowPlayingFragment.setIsPlaying(mStreamerService.isPlaying());
//                    }
//
//                    if (isStreamerServiceBound) mHandler.postDelayed(this, 1000);
//                }
//            });
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            isStreamerServiceBound = false;
//        }
//    };
//
//
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putInt(BUNDLE_KEY_CURRENT_TRACK, mCurrentTrack);
//        outState.putBoolean(BUNDLE_KEY_IS_PLAYING, mIsPlaying);
//    }
//
//    @Override
//    public void onNewIntent(Intent i) {
//        super.onNewIntent(i);
//
//        switch (i.getAction()) {
//            case ACTION_PREV:
//                onPrevClicked();
//                break;
//            case ACTION_NEXT:
//                onNextClicked();
//                break;
//            case ACTION_PAUSE:
//                onPauseClicked();
//                break;
//            case ACTION_PLAY:
//                onPlayClicked();
//                break;
//            default: // Do Nothing
//        }
//    }
//
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.now_playing_dialog);
//
//        if (savedInstanceState == null) {
//            getFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.now_playing_container, new NowPlayingFragment(), NOW_PLAYING_FRAGMENT)
//                    .commit();
//            mFirstTime = true;
//            mIsPlaying = false;
//
//        } else {
//            mCurrentTrack = savedInstanceState.getInt(BUNDLE_KEY_CURRENT_TRACK);
//
//            /*
//            * Get the playing / paused state from the bundle until the StreamerService
//            * is connected.  At that point, we can use the service to determine if we're
//            * currently playing.
//            */
//            mIsPlaying = savedInstanceState.getBoolean(BUNDLE_KEY_IS_PLAYING);
//            mFirstTime = false;
//        }
//
//        Intent callingIntent = getIntent();
//
//        // TODO: Make these Strings constants.
//        String operation = callingIntent.getStringExtra("operation");
//
//        if ("prev".equals(operation)) {
//            onPrevClicked();
//            return;
//        }
//
//        if ("pause".equals(operation)) {
//            onPauseClicked();
//            return;
//        }
//
//        if ("next".equals(operation)) {
//            onNextClicked();
//            return;
//        }
//
//        mTrackSpotifyId = callingIntent.getStringExtra(EXTRA_KEY_TRACK_SPOTIFY_ID);
//        mArtistSpotifyId = callingIntent.getStringExtra(EXTRA_KEY_ARTIST_SPOTIFY_ID);
//
//        if ((mTrackSpotifyId == null) || (mArtistSpotifyId == null)) {
//            throw new IllegalArgumentException("Must provide both the spotify track ID and " +
//                    "spotify artist ID for the track you wish to play.");
//        }
//
//        mResetOnStartup = callingIntent.getBooleanExtra(EXTRA_KEY_RESET_ON_STARTUP, true);
//
//        // Register to receive track completed broadcast notifications
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//                mBroadcastReceiver,
//                new IntentFilter(StreamerMediaService.ON_COMPLETE_BROADCAST_FILTER));
//
//        loadTrackData();
//
        // Start the StreamerMedia service.
        Intent startMediaServiceIntent = new Intent(this, StreamerMediaService.class);
        startService(startMediaServiceIntent);
        bindService(startMediaServiceIntent, mStreamerServiceConnection,
                Context.BIND_AUTO_CREATE);
//
//
//
//    }
//
//    private void loadTrackData() {
//
//        /*
//        * Load list of tracks for the given artist.
//        * Data is pulled from the Content Provider, and stored in String[]s.
//        */
//        Cursor trackListCursor = null;
//
//        try {
//            trackListCursor = getApplicationContext().getContentResolver().query(
//                    StreamerContract.GET_TRACKS_CONTENT_URI.buildUpon()
//                            .appendEncodedPath(mArtistSpotifyId).build(),
//                    null,
//                    null,
//                    null,
//                    null);
//
//            trackListCursor.moveToFirst();
//
//            mNumberOfTracks = trackListCursor.getCount();
//            mTrackUrls = new String[mNumberOfTracks];
//            mTrackNames = new String[mNumberOfTracks];
//            mAlbumNames = new String[mNumberOfTracks];
//            mTrackImages = new String[mNumberOfTracks];
//            mTrackIds = new String[mNumberOfTracks];
//            mDurations = new int[mNumberOfTracks];
//            mTrackExplicit = new boolean[mNumberOfTracks];
//
//
//            int i = 0;
//            while (!trackListCursor.isAfterLast()) {
//                mArtistName = trackListCursor.getString(StreamerProvider.IDX_ARTIST_NAME);
//                mTrackUrls[i] = trackListCursor.getString(StreamerProvider.IDX_PREVIEW_URL);
//                mTrackIds[i] = trackListCursor.getString(StreamerProvider.IDX_TRACK_SPOTIFY_ID);
//                mTrackNames[i] = trackListCursor.getString(StreamerProvider.IDX_TRACK_NAME);
//                mAlbumNames[i] = trackListCursor.getString(StreamerProvider.IDX_ALBUM_NAME);
//                mTrackImages[i] = trackListCursor.getString(StreamerProvider.IDX_TRACK_IMAGE);
//                mDurations[i] = trackListCursor.getInt(StreamerProvider.IDX_DURATION);
//                mTrackExplicit[i] = (trackListCursor.getInt(StreamerProvider.IDX_EXPLICIT) == 1);
//
//                if (mFirstTime && mTrackSpotifyId.equals(
//                        trackListCursor.getString(StreamerProvider.IDX_TRACK_SPOTIFY_ID))) {
//                    mCurrentTrack = i;
//                }
//
//                trackListCursor.moveToNext();
//                i++;
//            }
//
//        } catch (Exception e) {
//
//            /* We can get an exception if, for example, the database cursor comes back null.
//            * In that case, we'll show a Toast and call finish(), since there's nothing that
//            * can be done here.
//            * Set mNumberOfTracks to 0 to indicate an error.
//            */
//
//            mNumberOfTracks = 0;
//            mCurrentTrack = 0;
//
//            Toast.makeText(getApplicationContext(), R.string.error_restoring_state, Toast.LENGTH_SHORT).show();
//            finish();
//
//        } finally {
//
//            if (trackListCursor != null) {
//                trackListCursor.close();
//            }
//        }
//    }
//
//    public void requestContentRefresh() {
//        refreshContent();
//    }
//
//    private void refreshContent() {
//
//        /*
//        * If there was a error loading track data, don't try to push anything down
//        * to mNowPlayingFragment.
//        */
//        if (mNumberOfTracks == 0) return;
//
//        if (mNowPlayingFragment == null) {
//            mNowPlayingFragment = (NowPlayingFragment) getFragmentManager()
//                    .findFragmentByTag(NOW_PLAYING_FRAGMENT);
//        }
//
//        mNowPlayingFragment.setArtistName(mArtistName);
//        mNowPlayingFragment.setTrackName(mTrackNames[mCurrentTrack]);
//        mNowPlayingFragment.setAlbumName(mAlbumNames[mCurrentTrack]);
//        mNowPlayingFragment.setTrackImage(mTrackImages[mCurrentTrack]);
//        mNowPlayingFragment.setIsPlaying(isPlaying());
//    }
//
//    private boolean isPlaying() {
//
//        if (isStreamerServiceBound) {
//            setIsPlaying(mStreamerService.isPlaying());
//        }
//
//        return mIsPlaying;
//    }
//
//    private void setIsPlaying(boolean isPlaying) {
//        mIsPlaying = isPlaying;
//        mNowPlayingFragment.setIsPlaying(isPlaying);
//    }
//
//
//
//    @Override
//    public void onStop() {
//        Log.d(TAG, "onStop() called");
//        super.onStop();
//    }
//
//    private void queueNextSong() {
//        if (isStreamerServiceBound && (mNumberOfTracks > 0)) {
//            if (!mStreamerService.reset(Uri.parse(mTrackUrls[mCurrentTrack]), mTrackIds[mCurrentTrack])) {
//                Toast.makeText(this, R.string.media_error_playing, Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//
//
//
//
//
//    @Override
//    public void onPlayClicked() {
//
//        if (mNumberOfTracks == 0) return;
//
//        if (isStreamerServiceBound) {
//            if (mStreamerService.play()) {
//
//                // Save the current track's URL in shared preferences
//                SharedPreferences settings =
//                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//
//                SharedPreferences.Editor editor = settings.edit();
//                editor.putString(SpotifyStreamerActivity.PREF_CURRENT_TRACK_NAME, mTrackNames[mCurrentTrack]);
//                editor.putString(SpotifyStreamerActivity.PREF_CURRENT_TRACK_URL, mTrackUrls[mCurrentTrack]);
//                editor.putString(SpotifyStreamerActivity.PREF_CURRENT_ALBUM, mAlbumNames[mCurrentTrack]);
//                editor.putString(SpotifyStreamerActivity.PREF_CURRENT_ARTIST_NAME, mArtistName);
//                editor.putString(SpotifyStreamerActivity.PREF_CURRENT_ARTIST_SPOTIFY_ID, mArtistSpotifyId);
//                editor.putString(SpotifyStreamerActivity.PREF_CURRENT_TRACK_SPOTIFY_ID, mTrackIds[mCurrentTrack]);
//                editor.commit();
//
//                issueNotification();
//
//            } else {
//                Toast.makeText(this, R.string.media_error_general, Toast.LENGTH_SHORT).show();
//            }
//
//        } else {
//
//            mPlayOnServiceConnect = true;
//
//            // Start the StreamerMedia service.
//            Intent startMediaServiceIntent = new Intent(this, StreamerMediaService.class);
//            startService(startMediaServiceIntent);
//            bindService(startMediaServiceIntent, mStreamerServiceConnection,
//                    Context.BIND_AUTO_CREATE);
//        }
//    }
//
//
//    @Override
//    public void onPauseClicked() {
//
//        if (mNumberOfTracks == 0) return;
//
//        if (isStreamerServiceBound) {
//
//            if (!mStreamerService.pause()) {
//                Toast.makeText(this, R.string.media_error_general, Toast.LENGTH_SHORT).show();
//            }
//
//            issueNotification();
//
//        } else {
//
//            mPauseOnServiceConnect = true;
//
//            // Start the StreamerMedia service.
//            Intent startMediaServiceIntent = new Intent(this, StreamerMediaService.class);
//            startService(startMediaServiceIntent);
//            bindService(startMediaServiceIntent, mStreamerServiceConnection,
//                    Context.BIND_AUTO_CREATE);
//        }
//    }
//
//
//
//
//    @Override
//    public void onNextClicked() {
//
//        if (mNumberOfTracks == 0) return;
//
//        moveCurrentTrack(1);
//        refreshContent();
//        queueNextSong();
//        onPlayClicked();
//    }
//
//    @Override
//    public void onPrevClicked() {
//
//        if (mNumberOfTracks == 0) return;
//
//        moveCurrentTrack(-1);
//        refreshContent();
//        queueNextSong();
//        onPlayClicked();
//    }
//
//    @Override
//    public void seekTo(int miliSeconds) {
//
//        if (mNumberOfTracks == 0) return;
//
//        if (isStreamerServiceBound) {
//            if (!mStreamerService.seekTo(miliSeconds)) {
//                Toast.makeText(this, R.string.media_error_general, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//
//    private void moveCurrentTrack(int delta) {
//
//        mCurrentTrack += delta;
//
//        if (mCurrentTrack >= mNumberOfTracks) mCurrentTrack = 0;
//        if (mCurrentTrack < 0) mCurrentTrack = mNumberOfTracks - 1;
//
//        // Notify TrackListFragment of our new current track.
//        Intent intent = new Intent(TRACK_CHANGE_BROADCAST_FILTER);
//        intent.putExtra(TRACK_CHANGE_CURRENT_TRACK_NUM, mCurrentTrack);
//
//        LocalBroadcastManager.getInstance(NowPlayingActivity.this).sendBroadcast(intent);
//
//    }

    }
}
