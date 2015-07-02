package udacity.nano.spotifystreamer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import udacity.nano.spotifystreamer.data.StreamerContract.ArtistEntry;
import udacity.nano.spotifystreamer.data.StreamerContract.TrackEntry;
import udacity.nano.spotifystreamer.data.StreamerContract.QueryEntry;
import udacity.nano.spotifystreamer.data.StreamerContract.ArtistQuery;

public class StreamerDbHelper extends SQLiteOpenHelper {

    // Increment this value whenever the database schema is modified.
    private static final int DATABASE_VERSION = 4;

    static final String DATABASE_NAME = "streamer.db";

    public StreamerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /*
         * SQL Statement to create a table to hold Artist information.  In
         * addition to the Artist's name, icon, etc. we also store the time
         * at which the tracks associated with each artist were last updated,
         * and the country code that was used when that track data was fetched.
         * This will allow us to determine if we can use the cached data, or
         * if we must refresh from Spotify.
         */
        final String SQL_CREATE_ARTIST_TABLE =
                "CREATE TABLE " + ArtistEntry.TABLE_NAME + " (" +
                        ArtistEntry._ID + " INTEGER PRIMARY KEY," +
                        ArtistEntry.COLUMN_SPOTIFY_ID + " TEXT NOT NULL, " +
                        ArtistEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        ArtistEntry.COLUMN_ICON + " TEXT, " +
                        ArtistEntry.COLUMN_TRACKS_LAST_UPDATED + " TIMESTAMP," +
                        ArtistEntry.COLUMN_LAST_UPDATE_COUNTRY + " TEXT, " +

                        /*
                        * If an Artist with the same Spotify ID is inserted, replace this data
                        * instead of creating a new record.
                        */
                        " UNIQUE (" + ArtistEntry.COLUMN_SPOTIFY_ID + ") ON CONFLICT REPLACE);";

        /*
        * SQL Statement to create a table to hold Track information.
        */
        final String SQL_CREATE_TRACK_TABLE =
                "CREATE TABLE " + TrackEntry.TABLE_NAME + " (" +
                        TrackEntry._ID + " INTEGER PRIMARY KEY, " +
                        TrackEntry.COLUMN_ARTIST_ID + " INTEGER NOT NULL, " +
                        TrackEntry.COLUMN_SPOTIFY_ID + " TEXT NOT NULL, " +
                        TrackEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        TrackEntry.COLUMN_ALBUM + " TEXT NOT NULL, " +
                        TrackEntry.COLUMN_DURATION + " REAL NOT NULL, " +
                        TrackEntry.COLUMN_EXPLICIT + " INTEGER NOT NULL, " +
                        TrackEntry.COLUMN_PLAYABLE + " INTEGER NOT NULL, " +
                        TrackEntry.COLUMN_POPULARITY + " INTEGER NOT NULL, " +
                        TrackEntry.COLUMN_PREVIEW + " TEXT NOT NULL, " +
                        TrackEntry.COLUMN_ICON + " TEXT, " +
                        TrackEntry.COLUMN_IMAGE + " TEXT, " +

                        // Replace current data on duplicate insert.
                        " UNIQUE (" + TrackEntry.COLUMN_SPOTIFY_ID + ") ON CONFLICT REPLACE);" +

                        // Set up the artist ID column as a foreign key to Artist table.
                        " FOREIGN KEY (" + TrackEntry.COLUMN_ARTIST_ID + ") REFERENCES " +
                        ArtistEntry.TABLE_NAME + " (" + ArtistEntry._ID + "));";

        /*
        * SQL Statement to create a table to hold recent queries.
        */
        final String SQL_CREATE_QUERY_TABLE =
                "CREATE TABLE " + QueryEntry.TABLE_NAME + " (" +
                        QueryEntry._ID + " INTEGER PRIMARY KEY," +
                        QueryEntry.COLUMN_QUERY + " TEXT UNIQUE NOT NULL, " +
                        QueryEntry.COLUMN_CREATE_TIME + " TIMESTAMP NOT NULL);";


        /*
        * SQL Statement to create a table which matches Queries to Artists.
        * Provides a many-to-many join between Queries and Artists.
        */
        final String SQL_CREATE_ARTIST_QUERY_TABLE =
                "CREATE TABLE " + ArtistQuery.TABLE_NAME + " (" +
                        ArtistQuery.COLUMN_QUERY_ID + " INTEGER, " +
                        ArtistQuery.COLUMN_ARTIST_ID + " INTEGER, " +

                        " PRIMARY KEY (" + ArtistQuery.COLUMN_QUERY_ID + ", " +
                        ArtistQuery.COLUMN_ARTIST_ID + ") ON CONFLICT REPLACE, " +

                        // Set up the query column as a foreign key to query table.
                        " FOREIGN KEY (" + ArtistQuery.COLUMN_QUERY_ID + ") REFERENCES " +
                        QueryEntry.TABLE_NAME + " (" + QueryEntry._ID + "), " +

                        // Set up the artistId column as a foreign key to artist table.
                        " FOREIGN KEY (" + ArtistQuery.COLUMN_ARTIST_ID + ") REFERENCES " +
                        ArtistEntry.TABLE_NAME + " (" + ArtistEntry._ID + "));";

        /*
        * Create the tables defined by the above statements.
        */
        sqLiteDatabase.execSQL(SQL_CREATE_ARTIST_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRACK_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_QUERY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ARTIST_QUERY_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        /*
         * If the database version changes, we'll simply drop all tables and re-create them.
         * At this time, we're only storing cache data, so there's nothing that can't be
         * re-fetched from Spotify.
         */
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArtistEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + QueryEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArtistQuery.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
