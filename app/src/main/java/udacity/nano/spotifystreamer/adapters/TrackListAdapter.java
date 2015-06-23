package udacity.nano.spotifystreamer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import udacity.nano.spotifystreamer.R;

public class TrackListAdapter extends CursorAdapter {

    private final int iconWidth;
    private final int iconHeight;

    /*
    * These are tied to the the query sTracksByArtist in StreamerProvider.
    * If the attributes returned by that query changes, these values must be updated.
    */
    public static final int IDX_ARTIST_SPOTIFY_ID = 1;
    public static final int IDX_ARTIST_NAME = 2;
    public static final int IDX_ARTIST_ICON = 3;
    public static final int IDX_LAST_UPDATED = 4;
    public static final int IDX_ID = 5;
    public static final int IDX_ARTIST_ID = 6;
    public static final int IDX_TRACK_SPOTIFY_ID = 7;
    public static final int IDX_TRACK_NAME = 8;
    public static final int IDX_ALBUM_NAME = 9;
    public static final int IDX_DURATION = 10;
    public static final int IDX_EXPLICIT = 11;
    public static final int IDX_PLAYABLE = 12;
    public static final int IDX_POPULARITY = 13;
    public static final int IDX_PREVIEW_URL = 14;
    public static final int IDX_TRACK_ICON = 15;
    public static final int IDX_TRACK_IMAGE = 16;

    /*
    * Cache the views for an Track list item.
    */
    public static class ViewHolder {
        public final ImageView icon;
        public final TextView trackName;
        public final TextView albumName;

        public ViewHolder(View view) {
            icon = (ImageView) view.findViewById(R.id.image_track_icon);
            trackName = (TextView) view.findViewById(R.id.text_track_name);
            albumName = (TextView) view.findViewById(R.id.text_album_name);
        }
    }

    public TrackListAdapter(Context context, Cursor c, int flags,
                            int iconWidth, int iconHeight) {

        super(context, c, flags);

        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_track, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.trackName.setText(cursor.getString(IDX_TRACK_NAME));
        viewHolder.albumName.setText(cursor.getString(IDX_ALBUM_NAME));

        Picasso.with(context)
                .load(cursor.getString(IDX_TRACK_ICON))
                .resize(iconWidth, iconHeight)
                .into(viewHolder.icon);

    }
}



