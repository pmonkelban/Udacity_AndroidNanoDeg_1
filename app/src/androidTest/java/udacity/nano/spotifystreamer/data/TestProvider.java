package udacity.nano.spotifystreamer.data;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.test.AndroidTestCase;

public class TestProvider extends AndroidTestCase {


    public static final String TAG = TestProvider.class.getCanonicalName();

    public void deleteAllRecordsFromProvider() {

        mContext.getContentResolver().delete(
                StreamerContract.BASE_CONTENT_URI,
                null,
                null
        );
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                StreamerProvider.class.getName());

        try {
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            assertEquals(
                    "Error: StreamerProvider registered with authority: " +
                            providerInfo.authority +
                            " instead of authority: " + StreamerContract.CONTENT_AUTHORITY,
                    providerInfo.authority, StreamerContract.CONTENT_AUTHORITY);

        } catch (PackageManager.NameNotFoundException e) {
            fail("Error; StreamerProvider not registered at " + mContext.getPackageName());
        }

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        deleteAllRecordsFromProvider();
    }

    public void testGetType() {

        String type = mContext.getContentResolver().getType(StreamerContract.GET_ARTISTS_CONTENT_URI);
        assertEquals(StreamerContract.GET_ARTISTS_CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(StreamerContract.GET_TRACKS_CONTENT_URI);
        assertEquals(StreamerContract.GET_TRACKS_CONTENT_TYPE, type);
    }

    public void testDeleteDatabase() {
        getContext().deleteDatabase("streamer.db");
    }

    public void testArtistQuery() {

        Cursor cursor;

        mContext.getContentResolver().query(
                StreamerContract.GET_ARTISTS_CONTENT_URI.buildUpon().appendEncodedPath("U2").build(),
                null,
                null,
                null,
                null);
//
//        mContext.getContentResolver().query(
//                StreamerContract.GET_ARTISTS_CONTENT_URI.buildUpon().appendEncodedPath("Metallica").build(),
//                null,
//                null,
//                null,
//                null);
//
//        mContext.getContentResolver().query(
//                StreamerContract.GET_ARTISTS_CONTENT_URI.buildUpon().appendEncodedPath("Slayer").build(),
//                null,
//                null,
//                null,
//                null);

//        cursor = mContext.getContentResolver().query(
//                StreamerContract.GET_ARTISTS_CONTENT_URI.buildUpon().appendEncodedPath("Anthrax").build(),
//                null,
//                null,
//                null,
//                null);

//        assertEquals(9, cursor.getCount());

    }

    public void testTrackQuery() {

        final String artist_anthrax = "3JysSUOyfVs1UQ0UaESheP";

        Cursor cursor = mContext.getContentResolver().query(
                StreamerContract.GET_TRACKS_CONTENT_URI.buildUpon().appendEncodedPath(artist_anthrax).build(),
                null,
                null,
                null,
                null);

    }

    public void testManyArtists()  {

        for (char c = 'A'; c <= 'Z'; c++)  {

            mContext.getContentResolver().query(
                    StreamerContract.GET_ARTISTS_CONTENT_URI.buildUpon().appendEncodedPath("" + c).build(),
                    null,
                    null,
                    null,
                    null);

        }
    }


}
