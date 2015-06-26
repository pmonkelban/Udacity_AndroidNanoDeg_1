package udacity.nano.spotifystreamer.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import udacity.nano.spotifystreamer.R;
import udacity.nano.spotifystreamer.utils.ImageUtils;

public class StreamerProvider extends ContentProvider {

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
    private static final String TAG = StreamerProvider.class.getCanonicalName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private StreamerDbHelper mDbHelper;

    /*
    * Constants used by the matcher and query method to determine
    * what type of query to execute.
    */
    static final int GET_ARTISTS = 100;
    static final int GET_TRACKS = 200;

    private static final SQLiteQueryBuilder sArtistsByQuery;
    private static final SQLiteQueryBuilder sTracksByArtist;

    private SpotifyService mSpotifyService;

    // Desired height and width for icons and images.
    private int idealIconWidth;
    private int idealIconHeight;
    private int idealImageWidth;
    private int idealImageHeight;

    private static final long MAX_CACHE_TIME = 1000 * 60 * 60 * 2; // 2 hours ( * 0 -> fetch everything)
//    private static final long MAX_CACHE_TIME = 1000 * 60; // 1 minute (for testing)


    // Used for reading and writing dates to and from the database.  Note all times are in UTC.
    private static final SimpleDateFormat dateFormatter =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final int COLUMN_QUERY_ID_IDX = 0;

    /*
    * Used to store the user's country.  We get this from their
    * Default Local, and use it when using the Spotify API.
     */
    private Map<String, Object> mLocationMap = new HashMap<>();

    /*
    * When we perform a query and get a list of matching artists, we'll go ahead and pre-fetch
    * the top tracks for the first few artists returned.  The list of artists returned are sorted
    * by relevance, so it's likely that they'll match what the user was searching for.  By
    * pre-fetching those tracks, we can provide better performance.
    */
    private static final int NUM_RESULTS_TO_PRE_FETCH_TRACKS = 2;

    static {

        /*
        * When reading/writing to the database, all times will be in UTC.
        */
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        sArtistsByQuery = new SQLiteQueryBuilder();

        /*
        * Joins a Query to the set of Artists that match that query using the query_artist
        * join table.
        */
        sArtistsByQuery.setTables(
                StreamerContract.QueryEntry.TABLE_NAME +
                        " INNER JOIN " +
                        StreamerContract.ArtistQuery.TABLE_NAME +
                        " ON " +
                        StreamerContract.QueryEntry.TABLE_NAME + "." + StreamerContract.QueryEntry._ID +
                        " = " +
                        StreamerContract.ArtistQuery.TABLE_NAME + "." + StreamerContract.ArtistQuery.COLUMN_QUERY_ID +
                        " INNER JOIN " +
                        StreamerContract.ArtistEntry.TABLE_NAME +
                        " ON " +
                        StreamerContract.ArtistQuery.TABLE_NAME + "." + StreamerContract.ArtistQuery.COLUMN_ARTIST_ID +
                        " = " +
                        StreamerContract.ArtistEntry.TABLE_NAME + "." + StreamerContract.ArtistEntry._ID
        );

        sTracksByArtist = new SQLiteQueryBuilder();

        /*
        * Get the tracks associated with an artist.
        */
        sTracksByArtist.setTables(
                StreamerContract.ArtistEntry.TABLE_NAME +
                        " INNER JOIN " +
                        StreamerContract.TrackEntry.TABLE_NAME +
                        " ON " +
                        StreamerContract.TrackEntry.TABLE_NAME + "." + StreamerContract.TrackEntry.COLUMN_ARTIST_ID +
                        " = " +
                        StreamerContract.ArtistEntry.TABLE_NAME + "." + StreamerContract.ArtistEntry._ID
        );
    }


    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = StreamerContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, StreamerContract.PATH_GET_ARTISTS + "/*", GET_ARTISTS);
        matcher.addURI(authority, StreamerContract.PATH_GET_TRACKS + "/*", GET_TRACKS);

        return matcher;
    }

    @Override
    public boolean onCreate() {

        // Get the user's country, and store it in a map.
        // TODO: Get Location from preferences, or based on actual location.
        mLocationMap.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());

        mDbHelper = new StreamerDbHelper(getContext());

        if (mSpotifyService == null) {
            mSpotifyService = new SpotifyApi().getService();
        }

        idealIconWidth = (int) getContext().getResources().getDimension(R.dimen.icon_width);
        idealIconHeight = (int) getContext().getResources().getDimension(R.dimen.icon_height);

        idealImageWidth = (int) getContext().getResources().getDimension(R.dimen.track_image_download_width);
        idealImageHeight = (int) getContext().getResources().getDimension(R.dimen.track_image_download_height);

        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Date cacheCutOffTime = new Date();
        cacheCutOffTime.setTime(System.currentTimeMillis() - MAX_CACHE_TIME);

        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {

            case GET_ARTISTS: {
                retCursor = getArtists(uri, db, cacheCutOffTime);
                break;
            }
            case GET_TRACKS: {
                retCursor = getTracks(uri, db, cacheCutOffTime);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (retCursor != null)  retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    /*
    * This method is pretty long, but it handles all the work needed to get the set of artists
    * that match the query given in the URI.  It implements a cache so that artists may be
    * retrieved from the SQLite database instead of via a network call.  The method
    * also cleans up the cache by removing all expired entries whenever there is a new insert.
    * The flow is detailed in docs/CacheArtistFlowChart.pdf and the comments match up with each
    * step of the flow chart.
    */
    // TODO: Refactor this method into smaller pieces.

    private Cursor getArtists(Uri uri, SQLiteDatabase db, Date cacheCutOffTime) {

        Log.d(TAG, "getArtists() called. uri:" + uri.toString());

        final Pager<Artist> pager;

        final String query = uri.getLastPathSegment().toLowerCase().trim();

        /*
        * Does a record exist in the Query table matching this request?
        */
        Cursor checkForCachedQueryCursor = db.query(
                StreamerContract.QueryEntry.TABLE_NAME,
                null,
                StreamerContract.QueryEntry.COLUMN_QUERY + " = ?",
                new String[]{query},
                null,
                null,
                null);

        if (checkForCachedQueryCursor.moveToFirst()) {

            Log.d(TAG, "Record exists in Query table for query: " + query);

            /*
            * Has the Query record expired?
            */
            Date queryCreateTime;

            try {
                String queryCreateTimeStr =
                        checkForCachedQueryCursor.getString(checkForCachedQueryCursor.getColumnIndex(
                                StreamerContract.QueryEntry.COLUMN_CREATE_TIME));

                queryCreateTime = dateFormatter.parse(queryCreateTimeStr);

            } catch (ParseException e) {

                Log.e(TAG, "Error parsing create date for query: " + query);

                /*
                * If there's an error parsing the time stamp, this will default
                * to refreshing the data.
                */
                queryCreateTime = null;
            }

            // Finished query of Query table.
            checkForCachedQueryCursor.close();

            if ((queryCreateTime != null) && (queryCreateTime.after(cacheCutOffTime))) {
                Log.d(TAG, "Cache hit for query: " + query);

                // Fetch Artists matching the query.
                return sArtistsByQuery.query(
                        db,
                        null,
                        StreamerContract.QueryEntry.COLUMN_QUERY + " = ?",
                        new String[]{query},
                        null,
                        null,
                        null);

            } else {

                Log.d(TAG, "Cache stale for query:" + query);

                try {

                    /*
                    * Query Spotify for Matching Artists.
                    */
                    ArtistsPager artistPager = mSpotifyService.searchArtists(query);
                    pager = artistPager.artists;
                    Log.d(TAG, "Successfully retrieved artists for query: " + query);

                    /*
                    * Delete ArtistQuery Entry.
                    */
                    int queryId = checkForCachedQueryCursor.getInt(COLUMN_QUERY_ID_IDX);

                    db.delete(
                            StreamerContract.ArtistQuery.TABLE_NAME,
                            StreamerContract.ArtistQuery.COLUMN_QUERY_ID + " = ?",
                            new String[]{"" + queryId}
                    );

                    /*
                    * Delete Query Record.
                    */
                    db.delete(
                            StreamerContract.QueryEntry.TABLE_NAME,
                            StreamerContract.QueryEntry._ID + " = ?",
                            new String[]{"" + queryId}
                    );

                } catch (Exception e) {

                    /*
                    * Error, Using Stale Data.
                    */
                    Log.e(TAG, "Failed to fetch data for query: " + query + " Using stale data");
                    Log.e(TAG, "Error: " + e.getMessage());

                    return sArtistsByQuery.query(
                            db,
                            null,
                            StreamerContract.QueryEntry.COLUMN_QUERY + " = ?",
                            new String[]{query},
                            null,
                            null,
                            null);

                }
            }

        } else {

            Log.d(TAG, "Record does not exist in Query table for query: " + query);

            checkForCachedQueryCursor.close();

            try {
                ArtistsPager artistPager = mSpotifyService.searchArtists(query);
                pager = artistPager.artists;
                Log.d(TAG, "Successfully retrieved artists for query: " + query);

            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch data for query: " + query);
                Log.e(TAG, "Error: " + e.getMessage());

                // Fetch failed - show error
                return null;

            }
        }

        /*
        * Create new Query Record.
        */
        ContentValues values = new ContentValues();
        values.put(StreamerContract.QueryEntry.COLUMN_QUERY, query);
        values.put(StreamerContract.QueryEntry.COLUMN_CREATE_TIME, dateFormatter.format(new Date()));
        long queryRecordId = db.insert(StreamerContract.QueryEntry.TABLE_NAME, null, values);

        if (queryRecordId < 0) {
            throw new android.database.SQLException("Error creating query record. query:" + query);
        }

       /*
       * For Each Returned Artist
       */
        int n = 0;

        for (Artist artist : pager.items) {

            /*
            * Create / Update Artist data.
            * Note, we don't try to perform an Update, always insert.  The table definition
            * has a unique constraint on the Artist table such that if a duplicate
            * spotify id is entered, the existing record will be replaced.
            */
            values = new ContentValues();
            values.put(StreamerContract.ArtistEntry.COLUMN_SPOTIFY_ID, artist.id);
            values.put(StreamerContract.ArtistEntry.COLUMN_NAME, artist.name);

            Image i = ImageUtils.getClosestImageSize(artist.images, idealIconWidth,
                    idealIconHeight);

            values.put(StreamerContract.ArtistEntry.COLUMN_ICON, (i == null) ? null : i.url);

            long artistRecordId = db.insert(StreamerContract.ArtistEntry.TABLE_NAME, null, values);

            if (artistRecordId < 0) {
                throw new android.database.SQLException(
                        "Error creating Artist record.  artistId:" + artist.id);
            }

            /*
            * Create ArtistQuery Record.
            */
            values = new ContentValues();
            values.put(StreamerContract.ArtistQuery.COLUMN_QUERY_ID, queryRecordId);
            values.put(StreamerContract.ArtistQuery.COLUMN_ARTIST_ID, artistRecordId);
            if (db.insert(StreamerContract.ArtistQuery.TABLE_NAME, null, values) < 0) {
                throw new android.database.SQLException(
                        "Error creating query_artist record. queryRecordId:" + queryRecordId +
                                " artistRecordId:" + artistRecordId);
            }

            if (n < NUM_RESULTS_TO_PRE_FETCH_TRACKS) {
                getTracks(artistRecordId, artist.id, db, cacheCutOffTime);
                n++;
            }

        }

        flushArtistCache(db, cacheCutOffTime);

        /*
        * Return List of Artists.
        */
        return sArtistsByQuery.query(
                db,
                null,
                StreamerContract.QueryEntry.TABLE_NAME + "." + StreamerContract.QueryEntry.COLUMN_QUERY + " = ?",
                new String[]{query},
                null,
                null,
                null);

    }

    /*
    * Removes all queries that are older than cacheCutOffTime.
    */
    private void flushArtistCache(SQLiteDatabase db, Date cacheCutOffTime) {

        /*
        * Begin cleaning up expired records in the cache.
        */
        String cacheCutOffTimeStr = dateFormatter.format(cacheCutOffTime);

        db.beginTransaction();

        try {

            /*
            * Delete ArtistQuery records where the related Query has expired.
            */
            db.delete(
                    StreamerContract.ArtistQuery.TABLE_NAME,
                    StreamerContract.ArtistQuery.COLUMN_QUERY_ID + " IN (SELECT " +
                            StreamerContract.QueryEntry._ID + " FROM " +
                            StreamerContract.QueryEntry.TABLE_NAME + " WHERE " +
                            StreamerContract.QueryEntry.COLUMN_CREATE_TIME + " < ?)",
                    new String[]{cacheCutOffTimeStr}
            );

            /*
            * Delete Query records that have expired.
            */
            db.delete(
                    StreamerContract.QueryEntry.TABLE_NAME,
                    StreamerContract.QueryEntry.COLUMN_CREATE_TIME + " < ?",
                    new String[]{cacheCutOffTimeStr}
            );

            /*
            * Delete Tracks for all Artists that no longer have entries in ArtistQuery.
            */
            db.delete(
                    StreamerContract.TrackEntry.TABLE_NAME,
                    StreamerContract.TrackEntry.COLUMN_ARTIST_ID + " IN (SELECT " +
                            StreamerContract.ArtistEntry._ID + " FROM " +
                            StreamerContract.ArtistEntry.TABLE_NAME + " WHERE " +
                            StreamerContract.ArtistEntry._ID + " NOT IN (SELECT " +
                            StreamerContract.ArtistQuery.COLUMN_ARTIST_ID + " FROM " +
                            StreamerContract.ArtistQuery.TABLE_NAME + "))",
                    null
            );

            /*
            * Delete Artists that no longer have entries in ArtistQuery.
            */
            db.delete(
                    StreamerContract.ArtistEntry.TABLE_NAME,
                    StreamerContract.ArtistEntry._ID + " NOT IN (SELECT " +
                            StreamerContract.ArtistQuery.COLUMN_ARTIST_ID + " FROM " +
                            StreamerContract.ArtistQuery.TABLE_NAME + ")",
                    null
            );

            db.setTransactionSuccessful();

        } finally {

            db.endTransaction();
        }
    }

    /*
    * Gets tracks for the given artist where the artist's spotify ID is the last
    * segment of the given Uri.
    */
    private Cursor getTracks(Uri uri, SQLiteDatabase db, Date cacheCutOffTime) {

        final String spotifyId = uri.getLastPathSegment();

        Cursor artistLookupCursor = db.query(
                StreamerContract.ArtistEntry.TABLE_NAME,
                new String[]{StreamerContract.ArtistEntry._ID},
                StreamerContract.ArtistEntry.COLUMN_SPOTIFY_ID + " =?",
                new String[]{spotifyId},
                null,
                null,
                null);

        if (!artistLookupCursor.moveToFirst()) {
            Log.e(TAG, "Error: Unable to find Artist record for artist: " + spotifyId);
            return null;
        }

        int artistId = artistLookupCursor.getInt(
                artistLookupCursor.getColumnIndex(StreamerContract.ArtistEntry._ID));

        artistLookupCursor.close();

        return getTracks(artistId, spotifyId, db, cacheCutOffTime);

    }

    /*
    * Gets tracks for the given artistID where artistId is a key into the Artist table
    * in the given database.
    */
    private Cursor getTracks(long artistId, String artistSpotifyId, SQLiteDatabase db,
                             Date cacheCutOffTime) {

        Log.d(TAG, "getTracks() called. artistId:" + artistId);

        /*
        * Get the Artists last tracks updated time.
        */
        Cursor artistLastUpdateTimeCursor = db.query(
                StreamerContract.ArtistEntry.TABLE_NAME,
                new String[]{StreamerContract.ArtistEntry.COLUMN_TRACKS_LAST_UPDATED},
                StreamerContract.ArtistEntry._ID + " = ?",
                new String[]{"" + artistId},
                null,
                null,
                null);

        Date tracksLastUpdated = null;

        if (artistLastUpdateTimeCursor.moveToFirst()) {

            String tracksLastUpdatedStr = artistLastUpdateTimeCursor.getString(
                    artistLastUpdateTimeCursor.getColumnIndex(
                    StreamerContract.ArtistEntry.COLUMN_TRACKS_LAST_UPDATED));

            try {
                if (tracksLastUpdatedStr != null) {
                    tracksLastUpdated = dateFormatter.parse(tracksLastUpdatedStr);
                }
            } catch (ParseException e) {
                tracksLastUpdated = null;
            }
        }

        artistLastUpdateTimeCursor.close();

        List<Track> topTracks = null;
        
        /*
        * Is Artist's tracksUpdate NULL?
        */
        if (tracksLastUpdated == null) {

            try {

                /*
                * Fetch Tracks from Spotify
                */
                Tracks result = mSpotifyService.getArtistTopTrack(artistSpotifyId, mLocationMap);
                if (result != null) topTracks = result.tracks;
                Log.d(TAG, "Successfully retrieved top tracks for artist: " + artistSpotifyId);

            } catch (Exception e) {
                
                /*
                * Error
                */
                Log.e(TAG, "Failed to fetch to tracks for artist: " + artistSpotifyId);
                Log.e(TAG, "Error: " + e.getMessage());
                return null;
            }

        } else {

            /*
            * Artist's last Track update expired ?
            */
            if (tracksLastUpdated.before(cacheCutOffTime)) {

                try {

                /*
                * Fetch Tracks from Spotify
                */
                    Tracks result = mSpotifyService.getArtistTopTrack(artistSpotifyId, mLocationMap);
                    if (result != null) topTracks = result.tracks;

                    Log.d(TAG, "Successfully retrieved top tracks for artist: " + artistSpotifyId);

                    /*
                    * Delete Existing Tracks for Artist.
                    */
                    db.delete(
                            StreamerContract.TrackEntry.TABLE_NAME,
                            StreamerContract.TrackEntry.COLUMN_ARTIST_ID + " = ?",
                            new String[]{"" + artistId}
                    );

                } catch (Exception e) {

                    /*
                    * Error, use Stale Data
                    */
                    Log.e(TAG, "Failed to fetch to tracks for artist: " + artistSpotifyId + ", using Stale Data.");
                    Log.e(TAG, "Error: " + e.getMessage());

                    return sTracksByArtist.query(
                            db,
                            null,
                            StreamerContract.ArtistEntry.TABLE_NAME + "." + StreamerContract.ArtistEntry._ID + " = ?",
                            new String[]{"" + artistId},
                            null,
                            null,
                            null);
                }

            } else {

                /*
                * Return cached list of tracks.
                */
                return sTracksByArtist.query(
                        db,
                        null,
                        StreamerContract.ArtistEntry.TABLE_NAME + "." + StreamerContract.ArtistEntry._ID + " = ?",
                        new String[]{"" + artistId},
                        null,
                        null,
                        null);

            }

        }

        for (Track track : topTracks) {

             /*
            * Create Track data.
            */
            ContentValues values = new ContentValues();
            values.put(StreamerContract.TrackEntry.COLUMN_SPOTIFY_ID, track.id);
            values.put(StreamerContract.TrackEntry.COLUMN_ARTIST_ID, artistId);
            values.put(StreamerContract.TrackEntry.COLUMN_TITLE, track.name);
            values.put(StreamerContract.TrackEntry.COLUMN_ALBUM, track.album.name);
            values.put(StreamerContract.TrackEntry.COLUMN_DURATION, track.duration_ms);
            values.put(StreamerContract.TrackEntry.COLUMN_EXPLICIT, track.explicit);

            // Sometimes is_playable is null.  In this case, I've found that it is playable.
            values.put(StreamerContract.TrackEntry.COLUMN_PLAYABLE,
                    ((track.is_playable == null) || (track.is_playable)));

            values.put(StreamerContract.TrackEntry.COLUMN_POPULARITY, track.popularity);
            values.put(StreamerContract.TrackEntry.COLUMN_PREVIEW, track.preview_url);

            Image i = ImageUtils.getClosestImageSize(track.album.images, idealIconWidth,
                    idealIconHeight);

            values.put(StreamerContract.TrackEntry.COLUMN_ICON, (i == null) ? null : i.url);

            i = ImageUtils.getClosestImageSize(track.album.images, idealImageWidth, idealImageHeight);

            values.put(StreamerContract.TrackEntry.COLUMN_IMAGE, (i == null) ? null : i.url);

            if (db.insert(StreamerContract.TrackEntry.TABLE_NAME, null, values) < 0) {
                throw new android.database.SQLException("Error creating track record.  trackId:" + track.id);
            }
        }

        /*
        * Set Artist's track last update timestamp.
        */
        ContentValues values = new ContentValues();
        values.put(StreamerContract.ArtistEntry.COLUMN_TRACKS_LAST_UPDATED, dateFormatter.format(new Date()));

        db.update(
                StreamerContract.ArtistEntry.TABLE_NAME,
                values,
                StreamerContract.ArtistEntry._ID + " = ?",
                new String[]{"" + artistId}
        );

        /*
        * Begin removing expired tracks from cache.
        */
        flushTrackCache(db, cacheCutOffTime);

        /*
        * Return tracks for Artist.
        */
        return sTracksByArtist.query(
                db,
                null,
                StreamerContract.ArtistEntry.TABLE_NAME + "." + StreamerContract.ArtistEntry._ID + " = ?",
                new String[]{"" + artistId},
                null,
                null,
                null);

    }

    /*
    * Removes Track entries for all Artists where Artist.tracksLastUpdate is older than
    * cacheCutOffTime.
    */
    private void flushTrackCache(SQLiteDatabase db, Date cacheCutOffTime) {

        /*
        * Begin cleaning up expired tracks in the cache.
        */
        String cacheCutOffTimeStr = dateFormatter.format(cacheCutOffTime);

        db.beginTransaction();

        try {

            /*
            * Set the Artist's trackLastUpdated to NULL, if it has expired
            */
            ContentValues values = new ContentValues();
            values.putNull(StreamerContract.ArtistEntry.COLUMN_TRACKS_LAST_UPDATED);

            db.update(
                    StreamerContract.ArtistEntry.TABLE_NAME,
                    values,
                    StreamerContract.ArtistEntry.COLUMN_TRACKS_LAST_UPDATED + " < ?",
                    new String[]{cacheCutOffTimeStr}
            );

            /*
            * Delete all tracks where the related Artist's trackLastUpdate is NULL.
            */
            db.delete(
                    StreamerContract.TrackEntry.TABLE_NAME,
                    StreamerContract.TrackEntry.COLUMN_ARTIST_ID + " IN (SELECT " +
                            StreamerContract.ArtistEntry._ID + " FROM " +
                            StreamerContract.ArtistEntry.TABLE_NAME + " WHERE " +
                            StreamerContract.ArtistEntry.COLUMN_TRACKS_LAST_UPDATED +
                            " IS NULL)",
                    null

            );

            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e(TAG, "Error querying for artists with expired caches: " + e.getMessage());

        } finally {
            db.endTransaction();
        }
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case GET_ARTISTS:
                return StreamerContract.GET_ARTISTS_CONTENT_TYPE;

            case GET_TRACKS:
                return StreamerContract.GET_TRACKS_CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    /*
    * Insert operation is not currently supported.
    */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Direct Inserts Unsupported uri: " + uri);
    }

    /*
    * WARNING: Calling this deletes all cached data!
    * This should only be used to reset the database to it's initial state (i.e. for Testing)
    */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted = 0;

        try {
            db.beginTransaction();

            rowsDeleted += db.delete(StreamerContract.TrackEntry.TABLE_NAME, null, null);
            rowsDeleted += db.delete(StreamerContract.ArtistQuery.TABLE_NAME, null, null);
            rowsDeleted += db.delete(StreamerContract.QueryEntry.TABLE_NAME, null, null);
            rowsDeleted += db.delete(StreamerContract.ArtistEntry.TABLE_NAME, null, null);

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

        return rowsDeleted;
    }

    /*
    * Update operation is not currently supported.
    */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Direct Updates Unsupported uri: " + uri);
    }

}
