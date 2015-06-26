package udacity.nano.spotifystreamer.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import udacity.nano.spotifystreamer.R;

public class StreamerMediaService extends Service  {

    private final String TAG = this.getClass().getCanonicalName();

    private int NOTIFICATION = R.string.spotify_streamer_running;

    private NotificationManager mNotificationManager;

    public static final String ON_COMPLETE_BROADCAST_FILTER = "streamer-media-service-on-complete";

    MediaPlayer mMediaPlayer;

    MediaPlayer.OnCompletionListener mOnCompletionListener = null;

    private final IBinder mBinder = new StreamerMediaServiceBinder();

    public class StreamerMediaServiceBinder extends Binder {
        public StreamerMediaService getService()  {
            return StreamerMediaService.this;
        }
    }

    @Override
    public void onCreate()  {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)  {
        return START_STICKY;
    }

    @Override
    public void onDestroy()  {
        mNotificationManager.cancel(NOTIFICATION);
    }

    @Override
    public IBinder onBind(Intent i)  {
        return mBinder;
    }

    public void reset(Uri trackUri)  {

        stop();

        mMediaPlayer = MediaPlayer.create(this, trackUri);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "onPrepared() called");
                mMediaPlayer.start();
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
                Intent intent = new Intent(ON_COMPLETE_BROADCAST_FILTER);
                LocalBroadcastManager.getInstance(StreamerMediaService.this).sendBroadcast(intent);
            }
        });

    }

    public void pause()  {

        if ((mMediaPlayer != null) && (mMediaPlayer.isPlaying()))  {
            mMediaPlayer.pause();
        }

    }

    public void play()  {

        if ((mMediaPlayer != null) && (!mMediaPlayer.isPlaying())) {
            mMediaPlayer.start();
        }
    }

    public void stop()  {

        if (mMediaPlayer != null)  {

            if (mMediaPlayer.isPlaying())  {
                mMediaPlayer.stop();
            }

            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void seekTo(int miliSeconds)  {

        if (mMediaPlayer != null)  {
            mMediaPlayer.seekTo(miliSeconds);
        }

    }

    public int getDuration()  {
        return (mMediaPlayer == null) ? 100 : mMediaPlayer.getDuration();
    }

    public int getLocation()  {
        return (mMediaPlayer == null) ? 0 : mMediaPlayer.getCurrentPosition();
    }

    public boolean isPlaying()  {

        if (mMediaPlayer == null) return false;
        return mMediaPlayer.isPlaying();
    }

}
