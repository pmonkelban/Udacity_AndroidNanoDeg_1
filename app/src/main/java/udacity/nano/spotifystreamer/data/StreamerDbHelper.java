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
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "streamer.db";

    public StreamerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /*
         * Creates a table to hold Artist information.
         */
        final String SQL_CREATE_ARTIST_TABLE = "CREATE TABLE " + ArtistEntry.TABLE_NAME + " (" +
                ArtistEntry._ID + " INTEGER PRIMARY KEY," +
                ArtistEntry.COLUMN_ARTIST_ID + " TEXT UNIQUE NOT NULL, " +
                ArtistEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ArtistEntry.COLUMN_ICON + " TEXT NOT NULL, " +
                ArtistEntry.COLUMN_CREATE_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);";

        final String SQL_CREATE_TRACK_TABLE = "CREATE TABLE " + TrackEntry.TABLE_NAME + " (" +
                TrackEntry._ID + " INTEGER PRIMARY KEY, " +
                TrackEntry.COLUMN_TRACK_ID + " TEXT UNIQUE NOT NULL, " +
                TrackEntry.COLUMN_ARTIST_ID + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_ALBUM + " TEXT NOT NULL, " +

                TrackEntry.COLUMN_DURATION + " REAL NOT NULL, " +
                TrackEntry.COLUMN_EXPLICIT + " INTEGER NOT NULL, " +

                TrackEntry.COLUMN_PLAYABLE + " INTEGER NOT NULL, " +
                TrackEntry.COLUMN_POPULARITY + " INTEGER NOT NULL, " +
                TrackEntry.COLUMN_PREVIEW + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_ICON + " TEXT, " +
                TrackEntry.COLUMN_CREATE_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, " +

                // Set up the artist ID column as a foreign key to Artist table.
                " FOREIGN KEY (" + TrackEntry.COLUMN_ARTIST_ID + ") REFERENCES " +
                ArtistEntry.TABLE_NAME + " (" + ArtistEntry.COLUMN_ARTIST_ID + ");";

        final String SQL_CREATE_QUERY_TABLE = "CREATE TABLE " + QueryEntry.TABLE_NAME + " (" +
                QueryEntry._ID + " INTEGER PRIMARY KEY," +
                QueryEntry.COLUMN_QUERY + " TEXT UNIQUE NOT NULL, " +
                QueryEntry.COLUMN_CREATE_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL);";


        final String SQL_CREATE_ARTIST_QUERY_TABLE = "CREATE TABLE " + ArtistQuery.TABLE_NAME + " (" +
                ArtistQuery._ID + " INTEGER PRIMARY KEY," +
                ArtistQuery.COLUMN_QUERY + " TEXT NOT NULL, " +
                ArtistQuery.COLUMN_ARTIST_ID + " TEXT NOT NULL, " +
                ArtistQuery.COLUMN_CREATE_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)" +

                // Set up the query column as a foreign key to query table.
                " FOREIGN KEY (" + ArtistQuery.COLUMN_QUERY + ") REFERENCES " +
                QueryEntry.TABLE_NAME + " (" + QueryEntry.COLUMN_QUERY + "), " +

                // Set up the artistId column as a foreign key to artist table.
                " FOREIGN KEY (" + ArtistQuery.COLUMN_ARTIST_ID + ") REFERENCES " +
                ArtistEntry.TABLE_NAME + " (" + ArtistEntry.COLUMN_ARTIST_ID + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_ARTIST_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRACK_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_QUERY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ARTIST_QUERY_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        /*
         * If the database version changes, we'll simply drop all tables and re-create them.
         */
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArtistEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + QueryEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArtistQuery.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
