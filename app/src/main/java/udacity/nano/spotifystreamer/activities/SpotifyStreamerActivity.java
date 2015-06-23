package udacity.nano.spotifystreamer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.TrackListFragment;
import udacity.nano.spotifystreamer.services.StreamerMediaService;

abstract class SpotifyStreamerActivity extends ActionBarActivity implements TrackListFragment.Callback  {

    final String TAG = this.getClass().getCanonicalName();

    static final String TRACK_LIST_FRAGMENT = "TRACK_LIST_FRAG";
    static final String ARTIST_LIST_FRAGMENT = "ARTIST_LIST_FRAG";


    StreamerMediaService mStreamerService;
    boolean isStreamerServiceBound = false;

    private ServiceConnection mStreamerServiceConnection = new ServiceConnection()  {

        public void onServiceConnected(ComponentName className, IBinder service)  {
            StreamerMediaService.StreamerMediaServiceBinder binder =
                    (StreamerMediaService.StreamerMediaServiceBinder) service;
            mStreamerService = binder.getService();
            isStreamerServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isStreamerServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Start the StreamerMedia service.
        Intent intent = new Intent(this, StreamerMediaService.class);
        startService(intent);
        isStreamerServiceBound = bindService(intent, mStreamerServiceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onTrackClicked(Uri trackUri) {

        if (isStreamerServiceBound)  {
            mStreamerService.play(trackUri);
        } else  {
            Log.e(TAG, "Track selected trackUri:" + trackUri + " but Streamer Service not connected");
            Toast.makeText(this, R.string.spotify_streamer_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy()  {
        try  {
            if (isStreamerServiceBound)  {
                unbindService(mStreamerServiceConnection);
            }
        } catch (Exception e)  {
            // Ignore exception.
        }

        super.onDestroy();
    }
}
