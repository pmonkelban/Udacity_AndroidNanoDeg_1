package udacity.nano.spotifystreamer.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/*
* Holds many of the constants that will be needed when interaction with the data provider.
*/
public class StreamerContract {

    /*
    * Base for the URI clients will use to fetch data using the StremaerProvider Content Provider.
    */
    public static final String CONTENT_AUTHORITY = "udacity.nano.spotifystreamer";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /*
    * Tack these onto the BASE_CONTENT_URI to get either artist or track data.
    */
    public static final String PATH_GET_ARTISTS = "getArtists";
    public static final String PATH_GET_TRACKS = "getTracks";

    public static final Uri GET_ARTISTS_CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_GET_ARTISTS).build();

    public static final Uri GET_TRACKS_CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_GET_TRACKS).build();

    public static final String GET_ARTISTS_CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GET_ARTISTS;

    public static final String GET_TRACKS_CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GET_TRACKS;

    /*
    * Defines data related to Artists in the 'artist' table.
    */
    public static final class ArtistEntry implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "artist";

        public static final String COLUMN_SPOTIFY_ID = "artist_spotifyId";
        public static final String COLUMN_NAME = "artist_name";
        public static final String COLUMN_ICON = "artist_icon";
        public static final String COLUMN_TRACKS_LAST_UPDATED = "tracksLastUpdated";
        public static final String COLUMN_LAST_UPDATE_COUNTRY = "lastUpdateCountry";

    }

    /*
    * Defines data related to Tracks.
    */
    public static final class TrackEntry implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "track";

        public static final String COLUMN_SPOTIFY_ID = "track_spotifyId";

        // Foreign key relating this track to the artist.
        public static final String COLUMN_ARTIST_ID = "artist_id";

        public static final String COLUMN_TITLE = "track_name";
        public static final String COLUMN_ALBUM = "album";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_EXPLICIT = "explicit";
        public static final String COLUMN_PLAYABLE = "playable";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_PREVIEW = "previewURL";
        public static final String COLUMN_ICON = "track_icon";
        public static final String COLUMN_IMAGE = "track_image";
    }

    /*
    * Defines data related to Queries.
    */
    public static final class QueryEntry implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "query";

        public static final String COLUMN_QUERY = "queryString";
        public static final String COLUMN_CREATE_TIME = "createTime";
    }

    /*
    * Links Queries to the resulting Artists.
    */
    public static final class ArtistQuery implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "query_artist";

        public static final String COLUMN_QUERY_ID = "query_id";
        public static final String COLUMN_ARTIST_ID = "artist_id";
    }
}
