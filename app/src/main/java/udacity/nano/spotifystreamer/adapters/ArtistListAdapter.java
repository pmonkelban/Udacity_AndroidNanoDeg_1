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

public class ArtistListAdapter extends CursorAdapter {

    private final int iconWidth;
    private final int iconHeight;

    /*
    * These are tied to the the query sArtistsByQuery in StreamerProvider.
    * If the attributes returned by that query changes, these values must be updated.
    */
    public static final int IDX_QUERY_STRING = 1;
    public static final int IDX_CREATE_TIME = 2;
    public static final int IDX_QUERY_ID = 3;
    public static final int IDX_ARTIST_ID = 4;
    public static final int IDX_ID = 5;
    public static final int IDX_SPOTIFY_ID = 6;
    public static final int IDX_ARTIST_NAME = 7;
    public static final int IDX_ARTIST_ICON = 8;
    public static final int IDX_LAST_UPDATED = 9;

    /*
    * Cache the views for an Artist list item.
    */
    public static class ViewHolder {
        public final ImageView icon;
        public final TextView name;

        public ViewHolder(View view) {
            icon = (ImageView) view.findViewById(R.id.image_artist_icon);
            name = (TextView) view.findViewById(R.id.text_artists_name);
        }
    }

    public ArtistListAdapter(Context context, Cursor c, int flags,
                             int iconWidth, int iconHeight) {

        super(context, c, flags);

        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_artist, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.name.setText(cursor.getString(IDX_ARTIST_NAME));

        Picasso.with(context)
                .load(cursor.getString(IDX_ARTIST_ICON))
                .resize(iconWidth, iconHeight)
                .into(viewHolder.icon);

    }
}



