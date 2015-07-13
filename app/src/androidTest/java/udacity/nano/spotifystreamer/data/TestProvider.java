package udacity.nano.spotifystreamer.data;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Set;

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

        String type = mContext.getContentResolver().getType(
                StreamerContract.GET_ARTISTS_CONTENT_URI
                        .buildUpon()
                        .appendEncodedPath("a")
                        .build());

        assertEquals(StreamerContract.GET_ARTISTS_CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(
                StreamerContract.GET_TRACKS_CONTENT_URI
                        .buildUpon()
                        .appendEncodedPath("a")
                        .build());

        assertEquals(StreamerContract.GET_TRACKS_CONTENT_TYPE, type);
    }

    public void testDeleteDatabase() {
        getContext().deleteDatabase("streamer.db");
    }

    /*
    * TODO: Create MockSpotifyService.
    * Unfortunately, these unit tests rely on the SpotifyService.  Would like to inject a
    * Mock service into the content provider, so that we can control the results during the test.
    */
    public void testArtistQuery_1() {

        String query = "The Beatles";

        Cursor cursor;

        cursor = mContext.getContentResolver().query(
                StreamerContract.GET_ARTISTS_CONTENT_URI.buildUpon().appendEncodedPath(query).build(),
                null,
                null,
                null,
                null);

        /*
        * Artist IDs that match a query for "The Beatles"
        */
        String[] expectedResults = {
                "6L7ZhDn5GoQmV2c1wkXiVJ", "32G8uSrpZ9limb9m8kdQm4",
                "0hGFawLWfFixdz6BA3tIhM", "0rhGLV687CCwGfeJYXd176",
                "3ZOhDuAjCjvw9z5lEGUmgZ", "0rhA28IdY2o9SPUl6isIgx",
                "3WrFJ7ztbogyGnTHbHJFl2", "2mqAewXBJxGwxj1d1cGzf6",
                "1DKvlSxKEDJvj7JuerSzs3", "3MqavRgbnd0NlRuNBWlo3Z",
                "7EzNTMzMN70jRxBWbxeshd", "2SOEgjJBzwSi0U2bpDGeTR",
                "3cBV24PM5nZsXqopSHvdtS", "0qVGD3AptaPDfBSDntaBzb",
                "5RAkohKAstz0FD9LXWXvsW", "6bJMtaq66ey844D4IvjzHP",
                "3mnrmpvuo4TyzSZXpWPQUY", "2TI403CaRm5C8Ud6hsELcW",
                "5264gfijccY4lvqa45OVEp", "5zgR7V2XsPpRYs1ou6xOPB"};


        String result = compareResults(cursor, expectedResults, StreamerProvider.ARTISTS_BY_QUERY_IDX_SPOTIFY_ID);
        assertNull(result, result);

    }

    public void testTrackQuery_1() {

        String artistName = "The Beatles";
        String artistSpotifyId = "3WrFJ7ztbogyGnTHbHJFl2";


        /*
        * Make sure we have the artist data loaded.
        */
        mContext.getContentResolver().query(
                StreamerContract.GET_ARTISTS_CONTENT_URI.buildUpon().appendEncodedPath(artistName).build(),
                null,
                null,
                null,
                null);

        Cursor cursor = mContext.getContentResolver().query(
                StreamerContract.GET_TRACKS_CONTENT_URI.buildUpon().appendEncodedPath(artistSpotifyId).build(),
                null,
                null,
                null,
                null);

        String[] expectedResults = {
                "2EaUskvDGalY8XvkPjMeBE", "2UKhTBz32B80dmKAbOVyNH",
                "2Ykdg68oiLyTLMEDVby2DL", "3UO0N9CCedr5k3VpN8m9Q4",
                "2e6riK6IKvAEq0cqNWRHeG", "3LBE5PAMsXvctjLpB8wTde",
                "1wI1dtbKoc5EUtMdzhFkPG", "1gsgGMKLJy7AEKtqZpd4JH",
                "4rLIUR60zAvcNxH0LYUtV9", "5YIWWwapZoj5Plr06TqcMJ"};

        String result = compareResults(cursor, expectedResults, StreamerProvider.TRACKS_BY_ARTIST_IDX_TRACK_SPOTIFY_ID);
        assertNull(result, result);
    }

    private String compareResults(Cursor cursor, String[] expectedResults, int index) {

        Set<String> expected = new HashSet<>();
        for (String s : expectedResults)  {
            expected.add(s);
        }

        cursor.moveToFirst();

        Set<String> actual = new HashSet<>();

        while (!cursor.isAfterLast())  {
            actual.add(cursor.getString(index));
            cursor.moveToNext();
        }

        if (expected.equals(actual))  return null;

        expected.removeAll(actual);
        actual.removeAll(expected);

        StringBuilder sb = new StringBuilder();

        sb.append("In Expected but not in Actual: ");
        for (String s : expected)  {
            sb.append(s);
            sb.append(" ");
        }
        sb.append(" In Actual but not in Expected: ");
        for (String s : actual)  {
            sb.append(s);
            sb.append(" ");
        }

        return sb.toString();
    }

}
