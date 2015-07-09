package udacity.nano.spotifystreamer.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.squareup.picasso.Picasso;

import udacity.nano.spotifystreamer.PlayList;
import udacity.nano.spotifystreamer.R;

public class StreamerMediaService extends Service {

    private final String TAG = this.getClass().getCanonicalName();
    private final String MEDIA_SESSION_TAG = "streamer-media-service-tag";


    static final String ACTION_NO_OP = "action_no_op";
    static final String ACTION_PLAY = "action_play";
    static final String ACTION_PAUSE = "action_pause";
    static final String ACTION_REWIND = "action_rewind";
    static final String ACTION_FAST_FORWARD = "action_fast_forward";
    static final String ACTION_PREVIOUS = "action_prev";
    static final String ACTION_NEXT = "action_next";
    static final String ACTION_STOP = "action_stop";

    public static final int NOTIFICATION_ID = 10;



    //    private int NOTIFICATION_TAG = R.string.spotify_streamer_running;
//
//    private NotificationManager mNotificationManager;
//
//    public static final String ON_COMPLETE_BROADCAST_FILTER = "streamer-media-service-on-complete";
//
    private MediaSession mSession;
    private MediaController mController;
    private MediaPlayer mMediaPlayer;

    //
//
//    MediaPlayer.OnCompletionListener mOnCompletionListener = null;
//



    private NotificationTarget mNotificationTarget;

    private final IBinder mBinder = new StreamerMediaServiceBinder();

    public class StreamerMediaServiceBinder extends Binder {
        public StreamerMediaService getService() {
            return StreamerMediaService.this;
        }
    }


    private PlayList playList;

    @Override
    public void onCreate() {
//        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mSession == null) {
            initMediaSessions();
        }

        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent i) {
        return mBinder;
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_FAST_FORWARD)) {
            mController.getTransportControls().fastForward();
        } else if (action.equalsIgnoreCase(ACTION_REWIND)) {
            mController.getTransportControls().rewind();
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mController.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            mController.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            mController.getTransportControls().stop();
        }
    }

    public MediaSession.Token getMediaSessionToken()  {
        if (mSession == null) return null;
        return mSession.getSessionToken();
    }

    private void initMediaSessions() {

        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mController.getTransportControls().skipToNext();
                mController.getTransportControls().play();
            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d(TAG, "onError() called.  what:" + what + " extra:" + extra);
                return false;
            }
        });

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "onPrepared() called");
                mp.start();
            }
        });


        mSession = new MediaSession(getApplicationContext(), MEDIA_SESSION_TAG);

        mController = new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback() {

            @Override
            public void onPlay() {
                Log.d(TAG, "onPlay() called");

                super.onPlay();

                try {
                    mMediaPlayer = MediaPlayer.create(StreamerMediaService.this,
                            Uri.parse(playList.getCurrentTrackURI()));

                } catch (Exception e) {
                    Log.e(TAG, "Error in onPlay(): " + e.getMessage());
                }

                new NotificationTarget(
                        getApplicationContext(),
                        android.R.drawable.ic_media_pause,
                        getResources().getString(R.string.pause),
                        ACTION_PAUSE,
                        playList.getCurrentTrackName(),
                        playList.getCurrentTrackId(),
                        playList.getCurrentTrackImage(),
                        playList.getCurrentArtistName(),
                        playList.getCurrentArtistId(),
                        playList.getCurrentAlbumName()
                ).issueNotification();
            }

            @Override
            public void onPause() {
                Log.d(TAG, "onPause() called");
                super.onPause();

                new NotificationTarget(
                        getApplicationContext(),
                        android.R.drawable.ic_media_play,
                        getResources().getString(R.string.play),
                        ACTION_PLAY,
                        playList.getCurrentTrackName(),
                        playList.getCurrentTrackId(),
                        playList.getCurrentTrackImage(),
                        playList.getCurrentArtistName(),
                        playList.getCurrentArtistId(),
                        playList.getCurrentAlbumName()
                ).issueNotification();
            }

            @Override
            public void onSkipToNext() {
                Log.d(TAG, "onSkipToNext() called");
                super.onSkipToNext();
                playList.nextTrack();

                new NotificationTarget(
                        getApplicationContext(),
                        android.R.drawable.ic_media_play,
                        getResources().getString(R.string.play),
                        ACTION_PLAY,
                        playList.getCurrentTrackName(),
                        playList.getCurrentTrackId(),
                        playList.getCurrentTrackImage(),
                        playList.getCurrentArtistName(),
                        playList.getCurrentArtistId(),
                        playList.getCurrentAlbumName()
                ).issueNotification();
            }

            @Override
            public void onSkipToPrevious() {
                Log.d(TAG, "onSkipToPrevious() called");
                super.onSkipToPrevious();
                playList.previousTrack();

                new NotificationTarget(
                        getApplicationContext(),
                        android.R.drawable.ic_media_play,
                        getResources().getString(R.string.play),
                        ACTION_PLAY,
                        playList.getCurrentTrackName(),
                        playList.getCurrentTrackId(),
                        playList.getCurrentTrackImage(),
                        playList.getCurrentArtistName(),
                        playList.getCurrentArtistId(),
                        playList.getCurrentAlbumName()
                ).issueNotification();
            }

            @Override
            public void onStop() {
                Log.d(TAG, "onStop() called");
                super.onStop();
                ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE))
                        .cancel(NOTIFICATION_ID);

                stopService(new Intent(getApplicationContext(), StreamerMediaService.class));
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);

            }
        });
    }

    public void setPlayList(PlayList playList) {
        this.playList = playList.copy();
    }

    public MediaPlayer getMediaPlayer()  {
        return mMediaPlayer;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called");

        // Cancel ending pending image downloads.
        Picasso.with(this).cancelRequest(mNotificationTarget);

        // Cancel the notification
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);

        // Close the media session
        mSession.release();
        mSession = null;

        super.onDestroy();
    }


//
//    public boolean reset(Uri trackUri, String spotifyId) {
//
//        try {
//            stop();
//
//            mMediaPlayer = MediaPlayer.create(this, trackUri);
//            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
//
//            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    Log.d(TAG, "onPrepared() called");
//                    mMediaPlayer.start();
//                }
//            });
//
//            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//                @Override
//                public boolean onError(MediaPlayer mp, int what, int extra) {
//                    Log.d(TAG, "onError() called.  what:" + what + " extra:" + extra);
//                    return false;
//                }
//            });
//
//            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    Intent intent = new Intent(ON_COMPLETE_BROADCAST_FILTER);
//                    LocalBroadcastManager.getInstance(StreamerMediaService.this).sendBroadcast(intent);
//                }
//            });
//
//        } catch (Exception e) {
//            Log.e(TAG, "Error in reset(): " + e.getMessage());
//            return false;
//        }
//
//        return true;
//    }
//
//    public boolean pause() {
//
//        try {
//            if ((mMediaPlayer != null) && (mMediaPlayer.isPlaying())) {
//                mMediaPlayer.pause();
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "Error in pause(): " + e.getMessage());
//            return false;
//        }
//
//        return true;
//
//    }
//
//    public boolean play() {
//
//        try {
//            if ((mMediaPlayer != null) && (!mMediaPlayer.isPlaying())) {
//                mMediaPlayer.start();
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "Error in play(): " + e.getMessage());
//            return false;
//        }
//
//        return true;
//    }
//
//    public boolean stop() {
//
//        try {
//            if (mMediaPlayer != null) {
//
//                if (mMediaPlayer.isPlaying()) {
//                    mMediaPlayer.stop();
//                }
//
//                mMediaPlayer.reset();
//                mMediaPlayer.release();
//                mMediaPlayer = null;
//            }
//        } catch (Exception e)  {
//            Log.e(TAG, "Error in stop(): " + e.getMessage());
//            return false;
//        }
//
//        return true;
//    }
//
//    public boolean seekTo(int miliSeconds) {
//
//        try {
//            if (mMediaPlayer != null) {
//                mMediaPlayer.seekTo(miliSeconds);
//            }
//
//        } catch (Exception e)  {
//            Log.e(TAG, "Error in seekTo(): " + e.getMessage());
//            return false;
//        }
//
//        return true;
//    }
//
//    public int getDuration() {
//        return (mMediaPlayer == null) ? 100 : mMediaPlayer.getDuration();
//    }
//
//    public int getLocation() {
//        return (mMediaPlayer == null) ? 0 : mMediaPlayer.getCurrentPosition();
//    }
//
//    public boolean isPlaying() {
//
//        if (mMediaPlayer == null) return false;
//        return mMediaPlayer.isPlaying();
//    }

    @Override
    public boolean onUnbind(Intent intent) {
        mSession.release();
        return super.onUnbind(intent);
    }
}
