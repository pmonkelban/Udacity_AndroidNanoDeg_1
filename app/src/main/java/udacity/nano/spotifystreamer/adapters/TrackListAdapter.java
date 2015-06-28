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
import udacity.nano.spotifystreamer.data.StreamerProvider;

public class TrackListAdapter extends CursorAdapter {

    private final int iconWidth;
    private final int iconHeight;

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

        viewHolder.trackName.setText(cursor.getString(StreamerProvider.IDX_TRACK_NAME));
        viewHolder.albumName.setText(cursor.getString(StreamerProvider.IDX_ALBUM_NAME));

        Picasso.with(context)
                .load(cursor.getString(StreamerProvider.IDX_TRACK_ICON))
                .placeholder(context.getResources().getDrawable(R.drawable.image_loading, null))
                .error(context.getResources().getDrawable(R.drawable.image_not_available, null))
                .resize(iconWidth, iconHeight)
                .into(viewHolder.icon);

    }
}



