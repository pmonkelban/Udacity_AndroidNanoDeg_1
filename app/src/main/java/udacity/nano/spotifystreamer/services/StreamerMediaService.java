package udacity.nano.spotifystreamer.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import udacity.nano.spotifystreamer.PlayListItem;
import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.activities.MainActivity;
import udacity.nano.spotifystreamer.activities.SpotifyStreamerActivity;

public class StreamerMediaService extends Service {

    private final String TAG = this.getClass().getCanonicalName();

    private NotificationManager mNotificationManager;

    /*
    * We'll send these broadcases when a track starts or stops (not pause) playing.
    */
    public static final String TRACK_STOP_BROADCAST_FILTER = "streamer-media-service-on-complete";
    public static final String TRACK_START_BROADCAST_FILTER = "streamer-media-service-track-started";

    MediaPlayer mMediaPlayer;

    private PlayListItem mCurrentlyPlaying;

    /*
    * This will be true so long as the NowPlayingActivity is active.  When active, we'll keep
    * looping through all the songs for the current artist.  In this case, we'll re-use the
    * same notification, just updating the track info.
    * Once the NowPlayingActivity exits, this will be set to false, and we'll cancel the
    * notification when the current track finishes.
    */
    private boolean continueOnCompletion;

    private final IBinder mBinder = new StreamerMediaServiceBinder();

    public class StreamerMediaServiceBinder extends Binder {
        public StreamerMediaService getService() {
            return StreamerMediaService.this;
        }
    }

    /*
    * Listens for headphone un-plug events.  We'll pause the current track when
    * this event is received.
    */
    private BroadcastReceiver mNoisyAudioStreamReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent)  {

            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))  {
                pause();
            }
        }
    };

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Listen for headphone un-plug events.
        registerReceiver(mNoisyAudioStreamReceiver,
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        // Stop listening for headphone un-plug events.
        unregisterReceiver(mNoisyAudioStreamReceiver);

        // Cancel our Notification
        mNotificationManager.cancel(NotificationTarget.NOTIFICATION_ID);

        // Set preference to indication not playing.
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit()
                .putBoolean(MainActivity.PREF_IS_PLAYING, false)
                .commit();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent i) {
        return mBinder;
    }

    /*
    * Initializes the MediaPlayer and prepares it to play the current song.
    */
    public boolean reset(final PlayListItem playListItem) {

        try {
            stop();

            mCurrentlyPlaying = playListItem;

            mMediaPlayer = MediaPlayer.create(this, Uri.parse(playListItem.getTrackUri()));
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            // Keeps the device running, but allows the screen to turn off.
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {

                    // Media has loaded and we're ready to begin playing.
                    Log.d(TAG, "onPrepared() called");
                    mMediaPlayer.start();

                    // Notify listeners that a new track has started.
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putString(MainActivity.PREF_CURRENT_ALBUM, playListItem.getAlbumName())
                            .putString(MainActivity.PREF_CURRENT_ARTIST_NAME, playListItem.getArtistName())
                            .putString(MainActivity.PREF_CURRENT_ARTIST_SPOTIFY_ID, playListItem.getArtistId())
                            .putString(MainActivity.PREF_CURRENT_TRACK_NAME, playListItem.getTrackName())
                            .putString(MainActivity.PREF_CURRENT_TRACK_SPOTIFY_ID, playListItem.getTrackId())
                            .putString(MainActivity.PREF_CURRENT_TRACK_URL, playListItem.getTrackUri())
                            .putBoolean(MainActivity.PREF_IS_PLAYING, true)
                            .commit();

                    Intent intent = new Intent(TRACK_START_BROADCAST_FILTER);
                    intent.putExtra(SpotifyStreamerActivity.KEY_CURRENT_TRACK, playListItem);
                    LocalBroadcastManager.getInstance(StreamerMediaService.this).sendBroadcast(intent);
                }
            });

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d(TAG, "onError() called.  what:" + what + " extra:" + extra);
                    return false;
                }
            });

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {

                    if (!continueOnCompletion) {

                        mNotificationManager.cancel(NotificationTarget.NOTIFICATION_ID);

                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                .edit()
                                .putBoolean(MainActivity.PREF_IS_PLAYING, false)
                                .commit();
                    }

                    // Notify listeners that the track has completed.
                    Intent intent = new Intent(TRACK_STOP_BROADCAST_FILTER);
                    LocalBroadcastManager.getInstance(StreamerMediaService.this).sendBroadcast(intent);
                }

            });

        } catch (Exception e) {
            Log.e(TAG, "Error in reset(): " + e.getMessage());
            return false;
        }

        return true;
    }

    public boolean pause() {

        try {
            if ((mMediaPlayer != null) && (mMediaPlayer.isPlaying())) {
                mMediaPlayer.pause();
            }

            // Updates the notification to show the play button
            new NotificationTarget(
                    getApplicationContext(),
                    android.R.drawable.ic_media_play,
                    getResources().getString(R.string.play),
                    SpotifyStreamerActivity.ACTION_PLAY,
                    mCurrentlyPlaying.getTrackName(),
                    mCurrentlyPlaying.getTrackId(),
                    mCurrentlyPlaying.getTrackImage(),
                    mCurrentlyPlaying.getArtistName(),
                    mCurrentlyPlaying.getArtistId(),
                    mCurrentlyPlaying.getAlbumName()
            ).issueNotification();

        } catch (Exception e) {
            Log.e(TAG, "Error in pause(): " + e.getMessage());
            return false;
        }

        return true;

    }

    public boolean play() {

        try {
            if ((mMediaPlayer != null) && (!mMediaPlayer.isPlaying())) {
                mMediaPlayer.start();
            }

            // Updates the notification to show the pause button
            new NotificationTarget(
                    getApplicationContext(),
                    android.R.drawable.ic_media_pause,
                    getResources().getString(R.string.pause),
                    SpotifyStreamerActivity.ACTION_PAUSE,
                    mCurrentlyPlaying.getTrackName(),
                    mCurrentlyPlaying.getTrackId(),
                    mCurrentlyPlaying.getTrackImage(),
                    mCurrentlyPlaying.getArtistName(),
                    mCurrentlyPlaying.getArtistId(),
                    mCurrentlyPlaying.getAlbumName()
            ).issueNotification();

        } catch (Exception e) {
            Log.e(TAG, "Error in play(): " + e.getMessage());
            return false;
        }

        return true;
    }

    public boolean stop() {

        try {
            if (mMediaPlayer != null) {

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }

                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in stop(): " + e.getMessage());
            return false;
        }

        return true;
    }

    public boolean seekTo(int miliSeconds) {

        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(miliSeconds);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in seekTo(): " + e.getMessage());
            return false;
        }

        return true;
    }

    public int getDuration() {
        return (mMediaPlayer == null) ? 100 : mMediaPlayer.getDuration();
    }

    public int getLocation() {
        return (mMediaPlayer == null) ? 0 : mMediaPlayer.getCurrentPosition();
    }

    public boolean isPlaying() {

        if (mMediaPlayer == null) return false;
        return mMediaPlayer.isPlaying();
    }

    /*
    * Set to false to indicate there will be no more tracks coming.  Go ahead and cancel
    * notifications.  If true, we'll re-use the notifications.
    */
    public void setContinueOnCompletion(boolean continueOnCompletion) {
        this.continueOnCompletion = continueOnCompletion;
    }


}
