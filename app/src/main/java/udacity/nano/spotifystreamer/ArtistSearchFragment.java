package udacity.nano.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import udacity.nano.spotifystreamer.model.ArtistData;
import udacity.nano.spotifystreamer.utils.ImageUtils;


public class ArtistSearchFragment extends Fragment {

    // An identifier used for logging
    private final String TAG = getClass().getCanonicalName();

    // A key to get artist list out of the Bundle
    private static final String BUNDLE_KEY_ARTIST_LIST = "artists_list";

    // Contains the artist data for mArtistAdapter.
    private ArrayList<ArtistData> mArtistList;

    // mArtistAdapter is bound to a UI ListView element to provide the
    //results of the artist search.
    private ArtistAdapter mArtistAdapter;


    // The text field where the user enters their search.
    private SearchView mArtistSearchText;

    // Desired height and width for icons.
    private int idealIconWidth;
    private int idealIconHeight;

    /*
     * Prevents multiple searches within a short period of time.
     * This normally happens if the user has a real keyboard.
     * Pressing enter will result in both a Key Down and a Key Up
     * event firing, resulting in two queries being sent to the
     * Spotify API.
     */
    private static final Long SEARCH_REQUEST_WINDOW = 500L;  // one-half second
    private long lastSearchRequestTime;

    private final SpotifyService mSpotifyService;

    /*
     * No arg Constructor.
     */
    public ArtistSearchFragment() {
        mSpotifyService = new SpotifyApi().getService();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        idealIconWidth = (int) getActivity().getResources().getDimension(R.dimen.icon_width);
        idealIconHeight = (int) getActivity().getResources().getDimension(R.dimen.icon_height);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        if (mArtistList != null) {
            outState.putParcelableArrayList(BUNDLE_KEY_ARTIST_LIST, mArtistList);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*
         * Inflate the view which contains the text field for entering the artist
         * query, and the ListView which displays the results.
         */
        View view = inflater.inflate(R.layout.artist_search, container, false);

        // Create a new Adapter and bind it to the ListView
        ListView artistListView = (ListView) view.findViewById(R.id.artist_search_listView);

        if ((savedInstanceState != null) &&
                (savedInstanceState.containsKey(BUNDLE_KEY_ARTIST_LIST))) {

            mArtistList = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_ARTIST_LIST);

        } else  {
            mArtistList = new ArrayList<ArtistData>();
        }

        mArtistAdapter = new ArtistAdapter(mArtistList);
        artistListView.setAdapter(mArtistAdapter);

        /*
         * Detect when a list view item (an artist) is clicked, and launch
         * and intent passing the artist's id as an extra.
         */
        artistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArtistData artist = mArtistAdapter.getItem(position);

                Log.d(TAG, "Artist Clicked.  Name=" + artist.getName());

                Intent intent = new Intent(getActivity(), TopTracksActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artist.getId());
                startActivity(intent);

            }
        });

        // The field where the user enters the artist they're looking for.
        mArtistSearchText = (SearchView) view.findViewById(R.id.artist_search_searchView);

        /*
         * Sets a listener on the SearchView that kicks off fetchArtists() when the
         * user is done entering their search query.
         */
        mArtistSearchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {

                /*
                 * Throw out search requests if they come too quickly. This addresses
                 * a bug where hitting enter with a real keyboard results in two
                 * events firing.
                 */

                long time = System.currentTimeMillis();

                if ((lastSearchRequestTime + SEARCH_REQUEST_WINDOW) < time) {
                    lastSearchRequestTime = time;
                    fetchArtists();
                    return true;
                } else {
                    Log.d(TAG, "Duplicate search request detected - Ignoring");
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return view;
    }

    /*
     * Fetches Artist data using Spotify web service.
     * The artists name is contained in the mArtistSearchText field.
     */

    private void fetchArtists() {

        // The SearchView should not allow an empty query - but just in case...
        if (mArtistSearchText.getQuery() == null) {
            return;
        }

        final String artistName = mArtistSearchText.getQuery().toString().trim();

        if (artistName.length() == 0) {
            return;
        }

        /*
         * Clear the artist list before starting the query to prevent the data
         * from intermingling.  This shows up as the wrong icons next to a name
         * until the new images have time to load.
         */
        mArtistAdapter.clear();

        mSpotifyService.searchArtists(artistName, new Callback<ArtistsPager>() {

            @Override
            public void success(final ArtistsPager artistsPager, Response response) {

                /*
                 * Convert the Artist objects into ArtistData objects which
                 * can be stored in a Bundle.
                 */
                mArtistList = convertToArtistData(artistsPager.artists);

                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mArtistAdapter.clear();

                        if (mArtistList.isEmpty()) {
                            Toast.makeText(
                                    getActivity(),
                                    getString(R.string.artist_list_empty),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            mArtistAdapter.addAll(mArtistList);
                        }

                    }
                });
            }

            @Override
            public void failure(final RetrofitError error) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Error Getting Artists: " + error.getMessage());
                        Toast.makeText(
                                getActivity(),
                                getString(R.string.error_fetching_artists),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }

        });
    }

    /*
     * Converts a list of artists to a list of ArtistData.  For each Artist, the
     * ArtistData object will receive the one URL for the icon image that we'll use.
     */
    private ArrayList<ArtistData> convertToArtistData(Pager<Artist> artists) {

        ArrayList<ArtistData> artistDataList = new ArrayList<>();

        if (artists != null) {

            for (Artist artist : artists.items) {

                Image i = ImageUtils.getClosestImageSize(artist.images, idealIconWidth,
                        idealIconHeight);

                String iconUrl = (i == null) ? null : i.url;

                artistDataList.add(new ArtistData(artist, iconUrl));
            }
        }

        return artistDataList;

    }

    /*
     * An ArrayAdapter customized to populate the layout with artist data.
     */
    class ArtistAdapter extends ArrayAdapter<ArtistData> {

        private class ViewData {
            ImageView icon;
            TextView name;
        }

        public ArtistAdapter(List<ArtistData> objects) {
            super(getActivity(), 0, objects);
        }

        /*
         * An ArrayAdapter requires that we write the results to an EditText field.
         * By overriding the getView() method, we can insert the artist data into
         * the fields that we define.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            ViewData viewData;

            /*
             * If the view does not exist, we'll need to create it.  If it's already
             * there, then we can get the data out of the ViewData object stored in
             * the view's tag.
             */
            if (view == null) {

                view = getActivity().getLayoutInflater().inflate(
                        R.layout.list_item_artist, null);

                viewData = new ViewData();
                viewData.icon = (ImageView) view.findViewById(R.id.image_artist_icon);
                viewData.name = (TextView) view.findViewById(R.id.text_artists_name);

                view.setTag(viewData);  // Store data so we'll have it next time.

            } else {
                viewData = (ViewData) view.getTag();
            }


            ArtistData artist = getItem(position);

            if (artist != null) {
                viewData.name.setText(artist.getName());

                // Fetch the image and store in ViewData object's icon.
                if (artist.getUrl() != null) {
                    Picasso.with(getContext())
                            .load(artist.getUrl())
                            .resize(idealIconWidth, idealIconHeight)
                            .into(viewData.icon);
                }
            }

            return view;

        }
    }
}
