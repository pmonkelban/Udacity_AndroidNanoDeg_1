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
import android.util.Log;

import udacity.nano.spotifystreamer.R;

public class StreamerMediaService extends Service implements MediaPlayer.OnCompletionListener {

    private final String TAG = this.getClass().getCanonicalName();

    private int NOTIFICATION = R.string.spotify_streamer_running;

    private NotificationManager mNotificationManager;

    MediaPlayer mMediaPlayer;

    private final IBinder mBinder = new StreamerMediaServiceBinder();

    public class StreamerMediaServiceBinder extends Binder {
        public StreamerMediaService getService()  {
            return StreamerMediaService.this;
        }
    }

    @Override
    public void onCreate()  {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        showNotification();
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.release();
    }

    public void play(Uri trackUri)  {
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

    private void showNotification()  {
//        CharSequence c = getText(R.string.spotify_streamer_running);
//
//        Notification notification = new Notification(R.drawable.ic_logo, c, System.currentTimeMillis());
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, LocalServiceActivities.Controller.class), 0);
//        notification.setLatestEventInfo(this, getText(R.string.app_name), c, pendingIntent);
//        mNotificationManager.notify(NOTIFICATION, notification);


    }

}
