package udacity.nano.spotifystreamer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.activities.NowPlayingActivity;
import udacity.nano.spotifystreamer.activities.SpotifyStreamerActivity;

/*
    * When the image is downloaded (using the Picasso library) and the
    * onBitmapLoaded() method is called, this will create a Notification.
    * If the download fails, a default Bitmap image will be used instead.
    */
class NotificationTarget implements Target {

    // An ID for our notification so we can update or remove them later.
    public static int NOTIFICATION_ID = 27;  // Value doesn't matter
    private static int NOTIFICATION_REQUEST_CODE = 42;  // Value doesn't matter

    private static Bitmap mNoImageAvailableBitmap;

    final Context context;
    final int iconId;
    final String label;
    final String actionStr;
    final String trackName;
    final String trackId;
    final String trackImageUrl;
    final String artistName;
    final String artistId;
    final String albumName;

    NotificationTarget(Context context, int iconId, String label, String action,
                       String trackName, String trackId, String trackImageUrl,
                       String artistName, String artistId, String albumName) {

        this.context = context;
        this.iconId = iconId;
        this.label = label;
        this.actionStr = action;
        this.trackName = trackName;
        this.trackId = trackId;
        this.trackImageUrl = trackImageUrl;
        this.artistName = artistName;
        this.artistId = artistId;
        this.albumName = albumName;

        // Only need to do this once.
        if (mNoImageAvailableBitmap == null)  {
            mNoImageAvailableBitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.image_not_available);
        }
    }

    public void issueNotification()  {

        Picasso.with(context)
                .load(trackImageUrl)
                .placeholder(context.getResources().getDrawable(R.drawable.image_loading, null))
                .error(context.getResources().getDrawable(R.drawable.image_not_available, null))
                .into(this);
    }

    private void createNotification(Bitmap bitmap) {

        Notification.Action action = generateAction(
                iconId, label, actionStr);

        buildNotification(
                action,
                bitmap,
                trackName,
                artistName + " - " + albumName,
                context);

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

    /*
    * Borrowed from:
    * http://www.binpress.com/tutorial/using-android-media-style-notifications-with-media-session-controls/165
    */
    private Notification.Action generateAction(int icon, String title, String intentAction) {

        Intent intent = new Intent(context, NowPlayingActivity.class);
        intent.setAction(intentAction);

        int flags = 0;

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                NOTIFICATION_REQUEST_CODE, intent, flags);

        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    /*
    * Borrowed from:
    * http://www.binpress.com/tutorial/using-android-media-style-notifications-with-media-session-controls/165
    */
    private void buildNotification(Notification.Action action, Bitmap albumImage,
                                   String trackName, String artistAndAlbum, Context context) {

        if (albumImage == null) {
            albumImage = mNoImageAvailableBitmap;
        }

        Notification.MediaStyle style = new Notification.MediaStyle();

        Intent intent = new Intent(context, NowPlayingActivity.class);
        intent.setAction(SpotifyStreamerActivity.ACTION_NO_OP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        /*
        * Should the notification be displayed on the lock screen?
        */
        boolean allowNotificationOnLockScreen =
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(SpotifyStreamerActivity.PREF_ALLOW_ON_LOCK, true);

        final int visibility =
                (allowNotificationOnLockScreen) ? Notification.VISIBILITY_PUBLIC
                        : Notification.VISIBILITY_SECRET;

        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.notes)
                .setLargeIcon(albumImage)
                .setContentTitle(trackName)
                .setContentText(artistAndAlbum)
                .setContentIntent(pendingIntent)
                .setStyle(style)
                .setVisibility(visibility);

        builder.addAction(generateAction(android.R.drawable.ic_media_previous,
                context.getResources().getString(R.string.previous), SpotifyStreamerActivity.ACTION_PREVIOUS));

//        builder.addAction(generateAction(android.R.drawable.ic_media_rew,
//                "Rewind", SpotifyStreamerActivity.ACTION_REWIND));

        builder.addAction(action);

//        builder.addAction(generateAction(android.R.drawable.ic_media_ff,
//                "Fast Forward", SpotifyStreamerActivity.ACTION_FAST_FORWARD));

        builder.addAction(generateAction(android.R.drawable.ic_media_next,
                context.getResources().getString(R.string.next), SpotifyStreamerActivity.ACTION_NEXT));

        style.setShowActionsInCompactView(0, 1, 2);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

}