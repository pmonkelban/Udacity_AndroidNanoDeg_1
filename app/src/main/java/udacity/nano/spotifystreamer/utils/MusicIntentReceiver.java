package udacity.nano.spotifystreamer.utils;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import udacity.nano.spotifystreamer.services.StreamerMediaService;

public class MusicIntentReceiver extends android.content.BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY))  {

            Intent i = new Intent(context, StreamerMediaService.class);


        }
    }
}
