package udacity.nano.spotifystreamer.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import udacity.nano.spotifystreamer.NowPlayingFragment;
import udacity.nano.spotifystreamer.PlayList;
import udacity.nano.spotifystreamer.PlayListItem;
import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.data.StreamerContract;
import udacity.nano.spotifystreamer.data.StreamerProvider;
import udacity.nano.spotifystreamer.services.StreamerMediaService;

public class NowPlayingActivity extends Activity
        implements NowPlayingFragment.NowPlayingListener {

    public final String TAG = getClass().getCanonicalName();

    public static final String NOW_PLAYING_FRAGMENT = "Now_Playing_Fragment";
    public static final String CURRENT_PLAYLIST_POSITION = "current_playlist_position";


    NowPlayingFragment.NowPlayingListener mListener;

    private String mTrackSpotifyId;
    private String mArtistSpotifyId;

    PlayList mPlayList;

    private boolean mPlayOnServiceConnect = false;
    private boolean mPauseOnServiceConnect = false;

    private NowPlayingFragment mNowPlayingFragment;

    StreamerMediaService mStreamerService;
    boolean isStreamerServiceBound = false;

    private Bitmap mNoImageAvailableBitmap;

    // Handles updates to the progress bar.
    private Handler mHandler = new Handler();

    // An ID for our notification so we can update or remove them later.
    private int NOTIFICATION_ID = 27;  // Value doesn't matter
    private int NOTIFICATION_REQUEST_CODE = 42;  // Value doesn't matter

    private boolean mResetOnStartup;


    private ServiceConnection mStreamerServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {

            if (mPlayList.size() == 0) return;

            StreamerMediaService.StreamerMediaServiceBinder binder =
                    (StreamerMediaService.StreamerMediaServiceBinder) service;
            mStreamerService = binder.getService();
            isStreamerServiceBound = true;

            /*
            * Let the service know that after a track completes, another track
            * will begin playing.
            */
            mStreamerService.setContinueOnCompletion(true);

            // Begin playing the first track
            if (mResetOnStartup) {
                queueNextSong();
                onPlayClicked();

            } else {

                /*
                * In case Play or Pause was clicked before the service was established.
                */
                if (mPlayOnServiceConnect) {
                    mPlayOnServiceConnect = false;
                    onPlayClicked();

                } else if (mPauseOnServiceConnect) {
                    mPauseOnServiceConnect = false;
                    onPauseClicked();
                }

                setIsPlaying(mStreamerService.isPlaying());

            }


            /*
            * Create a process to update the seek bar location every second.
            * Also keeps the play/pause status up to date.  The play/pause
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
            switch (intent.getAction())  {
                case StreamerMediaService.TRACK_STOP_BROADCAST_FILTER:  {
                    onNextClicked();
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unexpected broadcast message received: " +
                            intent.getAction());
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_PLAYLIST_POSITION, mPlayList.getPosition());
        outState.putBoolean(SpotifyStreamerActivity.KEY_IS_PLAYING, mStreamerService.isPlaying());
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);

        switch (i.getAction()) {
            case SpotifyStreamerActivity.ACTION_PREVIOUS:
                onPrevClicked();
                break;
            case SpotifyStreamerActivity.ACTION_NEXT:
                onNextClicked();
                break;
            case SpotifyStreamerActivity.ACTION_PAUSE:
                onPauseClicked();
                break;
            case SpotifyStreamerActivity.ACTION_PLAY:
                onPlayClicked();
                break;
            default: // Do Nothing
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.now_playing);

        int savedPlayListPosition = -1;

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.now_playing_container, new NowPlayingFragment(), NOW_PLAYING_FRAGMENT)
                    .commit();
            mResetOnStartup = true;

        } else {

            /*
            * Get the playing / paused state from the bundle until the StreamerService
            * is connected.  At that point, we can use the service to determine if we're
            * currently playing.
            */
            mResetOnStartup = false;
            savedPlayListPosition =
                    savedInstanceState.getInt(CURRENT_PLAYLIST_POSITION);
        }

        Intent callingIntent = getIntent();

        String action = callingIntent.getStringExtra(SpotifyStreamerActivity.ACTION);

        if (SpotifyStreamerActivity.ACTION_PREVIOUS.equals(action)) {
            onPrevClicked();
            return;
        }

        if (SpotifyStreamerActivity.ACTION_PAUSE.equals(action)) {
            onPauseClicked();
            return;
        }

        if (SpotifyStreamerActivity.ACTION_NEXT.equals(action)) {
            onNextClicked();
            return;
        }

        mTrackSpotifyId = callingIntent.getStringExtra(SpotifyStreamerActivity.KEY_TRACK_SPOTIFY_ID);
        mArtistSpotifyId = callingIntent.getStringExtra(SpotifyStreamerActivity.KEY_ARTIST_SPOTIFY_ID);

        if ((mTrackSpotifyId == null) || (mArtistSpotifyId == null)) {
            throw new IllegalArgumentException("Must provide both the spotify track ID and " +
                    "spotify artist ID for the track you wish to play.");
        }


        // Are we returning from a notification?  Notification should set to false;
        mResetOnStartup &=
                callingIntent.getBooleanExtra(SpotifyStreamerActivity.KEY_RESET_ON_STARTUP, true);


        // Register to receive track completed broadcast notifications
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver,
                new IntentFilter(StreamerMediaService.TRACK_STOP_BROADCAST_FILTER));

        loadTrackData();

        if (savedPlayListPosition >= 0)  {
            mPlayList.setPosition(savedPlayListPosition);
        }

        // Start the StreamerMedia service.
        Intent startMediaServiceIntent = new Intent(this, StreamerMediaService.class);
        startService(startMediaServiceIntent);
        bindService(startMediaServiceIntent, mStreamerServiceConnection,
                Context.BIND_AUTO_CREATE);

        // Load our default "No Image Available" icon into a Bitmap
        mNoImageAvailableBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.image_not_available);

    }

    private boolean isPlaying() {
        return (mStreamerService == null) ? false : mStreamerService.isPlaying();
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

            mPlayList = new PlayList(trackListCursor.getCount());

            int i = 0;
            while (!trackListCursor.isAfterLast()) {

                PlayListItem item = new PlayListItem();

                item.setArtistName(trackListCursor.getString(StreamerProvider.IDX_ARTIST_NAME));
                item.setArtistId(trackListCursor.getString(StreamerProvider.IDX_ARTIST_SPOTIFY_ID));
                item.setTrackUri(trackListCursor.getString(StreamerProvider.IDX_PREVIEW_URL));
                item.setTrackId(trackListCursor.getString(StreamerProvider.IDX_TRACK_SPOTIFY_ID));
                item.setTrackName(trackListCursor.getString(StreamerProvider.IDX_TRACK_NAME));
                item.setAlbumName(trackListCursor.getString(StreamerProvider.IDX_ALBUM_NAME));
                item.setTrackImage(trackListCursor.getString(StreamerProvider.IDX_TRACK_IMAGE));
                item.setDuration(trackListCursor.getInt(StreamerProvider.IDX_DURATION));
                item.setExplicit(trackListCursor.getInt(StreamerProvider.IDX_EXPLICIT) == 1);

                if (mTrackSpotifyId.equals(item.getTrackId())) {
                    mPlayList.setPosition(i);
                }

                mPlayList.setItemAt(i++, item);
                trackListCursor.moveToNext();
            }

        } catch (Exception e) {

            /* We can get an exception if, for example, the database cursor comes back null.
            * In that case, we'll show a Toast and call finish(), since there's nothing that
            * can be done here.
            * Set mNumberOfTracks to 0 to indicate an error.
            * Insert empty values into arrays to prevent further errors.
            */

            mPlayList = new PlayList(0);

            Toast.makeText(getApplicationContext(), R.string.error_restoring_state, Toast.LENGTH_SHORT).show();
            finish();

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

        /*
        * If there was a error loading track data, don't try to push anything down
        * to mNowPlayingFragment.
        */
        if (mPlayList.size() == 0) return;

        if (mNowPlayingFragment == null) {
            mNowPlayingFragment = (NowPlayingFragment) getFragmentManager()
                    .findFragmentByTag(NOW_PLAYING_FRAGMENT);
        }

        PlayListItem item = mPlayList.getCurrentItem();

        mNowPlayingFragment.setArtistName(item.getArtistName());
        mNowPlayingFragment.setTrackName(item.getTrackName());
        mNowPlayingFragment.setAlbumName(item.getAlbumName());
        mNowPlayingFragment.setTrackImage(item.getTrackImage());
        mNowPlayingFragment.setIsPlaying(isPlaying());
    }

    private void setIsPlaying(boolean isPlaying) {
        mNowPlayingFragment.setIsPlaying(isPlaying);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called");

        try {
            if (isStreamerServiceBound) {

                /*
                * Notify the service that there are no more tracks coming.  This allows
                * it to cancel notifications (rather than leaving them around to be
                * replaced).
                */
                mStreamerService.setContinueOnCompletion(false);
                unbindService(mStreamerServiceConnection);
            }
        } catch (Exception e) {
            // Ignore exception.
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        super.onDestroy();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop() called");
        super.onStop();
    }

    private void queueNextSong() {
        if (isStreamerServiceBound && (mPlayList.size() > 0)) {
            if (!mStreamerService.reset(mPlayList.getCurrentItem())) {
                Toast.makeText(this, R.string.media_error_playing, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onPlayClicked() {

        if (mPlayList.size() == 0) return;

        if (isStreamerServiceBound) {
            if (mStreamerService.play()) {



            } else {
                Toast.makeText(this, R.string.media_error_general, Toast.LENGTH_SHORT).show();
            }

        } else {

            mPlayOnServiceConnect = true;

            // Start the StreamerMedia service.
            Intent startMediaServiceIntent = new Intent(this, StreamerMediaService.class);
            startService(startMediaServiceIntent);
            bindService(startMediaServiceIntent, mStreamerServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPauseClicked() {

        if (mPlayList.size() == 0) return;

        if (isStreamerServiceBound) {

            if (!mStreamerService.pause()) {
                Toast.makeText(this, R.string.media_error_general, Toast.LENGTH_SHORT).show();
            }

        } else {

            mPauseOnServiceConnect = true;

            // Start the StreamerMedia service.
            Intent startMediaServiceIntent = new Intent(this, StreamerMediaService.class);
            startService(startMediaServiceIntent);
            bindService(startMediaServiceIntent, mStreamerServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onNextClicked() {

        if (mPlayList.size() == 0) return;

        mPlayList.nextTrack();
        refreshContent();
        queueNextSong();
        onPlayClicked();
    }

    @Override
    public void onPrevClicked() {

        if (mPlayList.size() == 0) return;

        mPlayList.previousTrack();
        refreshContent();
        queueNextSong();
        onPlayClicked();
    }

    @Override
    public void seekTo(int miliSeconds) {

        if (mPlayList.size() == 0) return;

        if (isStreamerServiceBound) {
            if (!mStreamerService.seekTo(miliSeconds)) {
                Toast.makeText(this, R.string.media_error_general, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
