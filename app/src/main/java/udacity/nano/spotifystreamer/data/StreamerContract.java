package udacity.nano.spotifystreamer.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class StreamerContract {

    public static final String CONTENT_AUTHORITY = "udacity.nano.spotifystreamer";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ARTIST_SEARCH_RESULTS = "artistSearch";
    public static final String PATH_ARTIST = "artist";
    public static final String PATH_TRACK_SEARCH_RESULTS = "trackSearch";
    public static final String PATH_TRACK = "track";

    public static final class ArtistEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTIST).build();

        // Table name
        public static final String TABLE_NAME = "artist";

        public static final String COLUMN_ARTIST_ID = "spotifyId";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_CREATE_TIME = "createTime";

    }

    public static final class TrackEntry implements BaseColumns  {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRACK).build();

        // Table name
        public static final String TABLE_NAME = "track";

        public static final String COLUMN_TRACK_ID = "spotifyId";

        // Foreign key relating this track to the artist.
        public static final String COLUMN_ARTIST_ID = "artistId";

        public static final String COLUMN_TITLE = "name";
        public static final String COLUMN_ALBUM = "album";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_EXPLICIT = "explicit";
        public static final String COLUMN_PLAYABLE = "playable";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_PREVIEW = "previewURL";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_CREATE_TIME = "createTime";

    }

    public static final class QueryEntry implements BaseColumns  {


        // Table name
        public static final String TABLE_NAME = "query";

        public static final String COLUMN_QUERY = "queryString";
        public static final String COLUMN_CREATE_TIME = "createTime";

    }

    public static final class ArtistQuery implements BaseColumns  {

        // Table name
        public static final String TABLE_NAME = "query_artist";

        public static final String COLUMN_QUERY = "query";
        public static final String COLUMN_ARTIST_ID = "artistId";
        public static final String COLUMN_CREATE_TIME = "createTime";

    }
}
