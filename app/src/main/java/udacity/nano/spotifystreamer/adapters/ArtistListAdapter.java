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

/*
* Shows Artist Data for each element in an ArrayAdapter.
*/
public class ArtistListAdapter extends CursorAdapter {

    // The ideal width and height for the Artist icons.
    private final int iconWidth;
    private final int iconHeight;

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

        viewHolder.name.setText(cursor.getString(StreamerProvider.ARTISTS_BY_QUERY_IDX_ARTIST_NAME));

        Picasso.with(context)
                .load(cursor.getString(StreamerProvider.ARTISTS_BY_QUERY_IDX_ARTIST_ICON))
                .placeholder(context.getResources().getDrawable(R.drawable.image_loading, null))
                .error(context.getResources().getDrawable(R.drawable.image_not_available, null))
                .resize(iconWidth, iconHeight)
                .into(viewHolder.icon);

    }
}



