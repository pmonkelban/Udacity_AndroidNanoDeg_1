package udacity.nano.spotifystreamer.activities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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

    private static final String ACTION_NO_OP = "action_no_op";
    private static final String ACTION_PLAY = "action_play";
    private static final String ACTION_PAUSE = "action_pause";
    private static final String ACTION_PREV = "action_prev";
    private static final String ACTION_NEXT = "action_next";

    public static final String TRACK_CHANGE_BROADCAST_FILTER = "track_change_broadcast_filter";
    public static final String TRACK_CHANGE_CURRENT_TRACK_NUM = "track_change_current_track";

    public static final String NOW_PLAYING_FRAGMENT = "Now_Playing_Fragment";

    NowPlayingFragment.NowPlayingListener mListener;

    private String mTrackSpotifyId;
    private String mArtistSpotifyId;

    private String mArtistName;
    private String[] mTrackUrls;
    private String[] mTrackNames;
    private String[] mAlbumNames;
    private String[] mTrackImages;
    private String[] mTrackIds;

    /*
    * Note durations are for the real track.  All samples are 30 seconds.  This value is OK
    * to display for info, but should not be used to set the SeekBar.
    */
    private int[] mDurations;

    private boolean[] mTrackExplicit;

    private int mCurrentTrack;
    private int mNumberOfTracks;

    private boolean mPlayOnServiceConnect = false;
    private boolean mPauseOnServiceConnect = false;

    private NowPlayingFragment mNowPlayingFragment;

    private boolean mFirstTime = false;
    private boolean mIsPlaying = false;

    StreamerMediaService mStreamerService;
    boolean isStreamerServiceBound = false;

    private Bitmap mNoImageAvailableBitmap;

    // Handles updates to the progress bar.
    private Handler mHandler = new Handler();

    // An ID for our notification so we can update or remove them later.
    private int NOTIFICATION_ID = 27;  // Value doesn't matter
    private int NOTIFICATION_REQUEST_CODE = 42;  // Value doesn't matter

    private ServiceConnection mStreamerServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {

            if (mNumberOfTracks == 0) return;

            StreamerMediaService.StreamerMediaServiceBinder binder =
                    (StreamerMediaService.StreamerMediaServiceBinder) service;
            mStreamerService = binder.getService();
            isStreamerServiceBound = true;

            // Begin playing the first track
            if (mFirstTime) {
                queueNextSong();
                onPlayClicked();

            } else {

                if (mPlayOnServiceConnect) {
                    mPlayOnServiceConnect = false;
                    onPlayClicked();
                }

                if (mPauseOnServiceConnect) {
                    mPauseOnServiceConnect = false;
                    onPauseClicked();
                }

                setIsPlaying(mStreamerService.isPlaying());

                issueNotification();

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
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);

        switch (i.getAction()) {
            case ACTION_PREV:
                onPrevClicked();
                break;
            case ACTION_NEXT:
                onNextClicked();
                break;
            case ACTION_PAUSE:
                onPauseClicked();
                break;
            case ACTION_PLAY:
                onPlayClicked();
                break;
            default: // Do Nothing
        }
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

        String operation = callingIntent.getStringExtra("operation");

        if ("prev".equals(operation)) {
            onPrevClicked();
            return;
        }

        if ("pause".equals(operation)) {
            onPauseClicked();
            return;
        }

        if ("next".equals(operation)) {
            onNextClicked();
            return;
        }

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

        // Load our default "No Image Available" icon into a Bitmap
        mNoImageAvailableBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_not_available);

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
            mTrackIds = new String[mNumberOfTracks];
            mDurations = new int[mNumberOfTracks];
            mTrackExplicit = new boolean[mNumberOfTracks];


            int i = 0;
            while (!trackListCursor.isAfterLast()) {
                mArtistName = trackListCursor.getString(StreamerProvider.IDX_ARTIST_NAME);
                mTrackUrls[i] = trackListCursor.getString(StreamerProvider.IDX_PREVIEW_URL);
                mTrackIds[i] = trackListCursor.getString(StreamerProvider.IDX_TRACK_SPOTIFY_ID);
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
        } catch (Exception e) {

            /* We can get an exception if, for example, the database cursor comes back null.
            * In that case, we'll show a Toast and call finish(), since there's nothing that
            * can be done here.
            * Set mNumberOfTracks to 0 to indicate an error.
            * Insert empty values into arrays to prevent further errors.
            */

            mNumberOfTracks = 0;
            mCurrentTrack = 0;

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
        if (mNumberOfTracks == 0) return;

        if (mNowPlayingFragment == null) {
            mNowPlayingFragment = (NowPlayingFragment) getFragmentManager()
                    .findFragmentByTag(NOW_PLAYING_FRAGMENT);
        }

        mNowPlayingFragment.setArtistName(mArtistName);
        mNowPlayingFragment.setTrackName(mTrackNames[mCurrentTrack]);
        mNowPlayingFragment.setAlbumName(mAlbumNames[mCurrentTrack]);
        mNowPlayingFragment.setTrackImage(mTrackImages[mCurrentTrack]);
        mNowPlayingFragment.setIsPlaying(isPlaying());
    }

    private boolean isPlaying() {

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
        Log.d(TAG, "onDestroy() called");


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(NOTIFICATION_ID);
//        mStreamerService.stop();

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

    @Override
    public void onStop() {
        Log.d(TAG, "onStop() called");


        super.onStop();
    }

    private void queueNextSong() {
        if (isStreamerServiceBound && (mNumberOfTracks > 0)) {
            if (!mStreamerService.reset(Uri.parse(mTrackUrls[mCurrentTrack]), mTrackIds[mCurrentTrack])) {
                Toast.makeText(this, R.string.media_error_playing, Toast.LENGTH_LONG).show();
            }
        }
    }

    /*
    * Borrowed from:
    * http://www.binpress.com/tutorial/using-android-media-style-notifications-with-media-session-controls/165
    */
    private Notification.Action generateAction(int icon, String title, String intentAction,
                                               String trackId, String artistId) {
        Intent intent = new Intent(getApplicationContext(), NowPlayingActivity.class);
        intent.setAction(intentAction);

        /*
        * We'll add the current track and artist id into the intent just in case the
        * NowPlayingActivity has been destroyed before the Notification comes back.  That will
        * allow us to reconstruct the activity properly.
        */
        intent.putExtra(EXTRA_KEY_TRACK_SPOTIFY_ID, trackId);
        intent.putExtra(EXTRA_KEY_ARTIST_SPOTIFY_ID, artistId);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    /*
    * Borrowed from:
    * http://www.binpress.com/tutorial/using-android-media-style-notifications-with-media-session-controls/165
    */
    private void buildNotification(Notification.Action action, String trackName,
                                   String artistAndAlbum, String trackId, String artistId,
                                   Bitmap albumImage) {

        if (albumImage == null) {
            albumImage = mNoImageAvailableBitmap;
        }

        Notification.MediaStyle style = new Notification.MediaStyle();

        Intent intent = new Intent(getApplicationContext(), NowPlayingActivity.class);
        intent.setAction(ACTION_NO_OP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notes)
                .setLargeIcon(albumImage)
                .setContentTitle(trackName)
                .setContentText(artistAndAlbum)
                .setDeleteIntent(pendingIntent)
                .setStyle(style);

        builder.addAction(generateAction(android.R.drawable.ic_media_previous,
                getResources().getString(R.string.previous), ACTION_PREV, trackId, artistId));

        builder.addAction(action);

        builder.addAction(generateAction(android.R.drawable.ic_media_next,
                getResources().getString(R.string.next), ACTION_NEXT, trackId, artistId));

        style.setShowActionsInCompactView(0, 1, 2);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /*
    * When the image is downloaded (using the Picasso library) and the
    * onBitmapLoaded() method is called, this will create a Notification.
    * If the download fails, a default Bitmap image will be used instead.
    */
    private class NotificationTarget implements Target {

        final int iconId;
        final String label;
        final String actionStr;

        NotificationTarget(int iconId, String label, String action) {
            this.iconId = iconId;
            this.label = label;
            this.actionStr = action;
        }

        void createNotification(Bitmap bitmap) {

            Notification.Action action = generateAction(
                    iconId, label, actionStr, mTrackIds[mCurrentTrack], mArtistSpotifyId);

            buildNotification(
                    action,
                    mTrackNames[mCurrentTrack],
                    mArtistName + " - " + mAlbumNames[mCurrentTrack],
                    mTrackIds[mCurrentTrack], mArtistSpotifyId, bitmap);

        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            createNotification(bitmap);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            // Do Nothing
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            createNotification(mNoImageAvailableBitmap);
        }
    }

    @Override
    public void onPlayClicked() {

        if (mNumberOfTracks == 0) return;

        if (isStreamerServiceBound) {
            if (!mStreamerService.play()) {
                Toast.makeText(this, R.string.media_error_general, Toast.LENGTH_SHORT).show();
            }

            issueNotification();

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

        if (mNumberOfTracks == 0) return;

        if (isStreamerServiceBound) {

            if (!mStreamerService.pause()) {
                Toast.makeText(this, R.string.media_error_general, Toast.LENGTH_SHORT).show();
            }

            issueNotification();

        } else {

            mPauseOnServiceConnect = true;

            // Start the StreamerMedia service.
            Intent startMediaServiceIntent = new Intent(this, StreamerMediaService.class);
            startService(startMediaServiceIntent);
            bindService(startMediaServiceIntent, mStreamerServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }


    private void issueNotification() {

        /*
        * Use our helper class, defined above, to load an image and then
        * create a Notification.
        */

        int iconId;
        String label;
        String action;

        if (isPlaying())  {
            iconId = android.R.drawable.ic_media_pause;
            label = getResources().getString(R.string.pause);
            action = ACTION_PAUSE;
        } else  {
            iconId = android.R.drawable.ic_media_play;
            label = getResources().getString(R.string.play);
            action = ACTION_PLAY;
        }

        Target bitmapTarget = new NotificationTarget(iconId, label, action);

        Picasso.with(this)
                .load(mTrackImages[mCurrentTrack])
                .placeholder(getResources().getDrawable(R.drawable.image_loading, null))
                .error(getResources().getDrawable(R.drawable.image_not_available, null))
                .into(bitmapTarget);

        setIsPlaying(false);

    }

    @Override
    public void onNextClicked() {

        if (mNumberOfTracks == 0) return;

        moveCurrentTrack(1);

        refreshContent();
        queueNextSong();
        onPlayClicked();
    }

    @Override
    public void onPrevClicked() {

        if (mNumberOfTracks == 0) return;

        moveCurrentTrack(-1);

        refreshContent();
        queueNextSong();
        onPlayClicked();
    }

    @Override
    public void seekTo(int miliSeconds) {

        if (mNumberOfTracks == 0) return;

        if (isStreamerServiceBound) {
            if (!mStreamerService.seekTo(miliSeconds)) {
                Toast.makeText(this, R.string.media_error_general, Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void moveCurrentTrack(int delta) {

        mCurrentTrack += delta;

        if (mCurrentTrack >= mNumberOfTracks) mCurrentTrack = 0;
        if (mCurrentTrack < 0) mCurrentTrack = mNumberOfTracks - 1;

        // Notify TrackListFragment of our new current track.
        Intent intent = new Intent(TRACK_CHANGE_BROADCAST_FILTER);
        intent.putExtra(TRACK_CHANGE_CURRENT_TRACK_NUM, mCurrentTrack);

        LocalBroadcastManager.getInstance(NowPlayingActivity.this).sendBroadcast(intent);

        // Save the current tracks URL in shared preferences
        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(MainActivity.PREF_CURRENT_TRACK, mTrackUrls[mCurrentTrack]);
        editor.apply();

    }
}
