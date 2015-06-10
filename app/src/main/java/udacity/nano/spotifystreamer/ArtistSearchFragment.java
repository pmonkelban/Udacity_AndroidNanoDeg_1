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
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import udacity.nano.spotifystreamer.activities.TopTracksActivity;
import udacity.nano.spotifystreamer.utils.ImageUtils;


public class ArtistSearchFragment extends Fragment {

    // An identifier used for logging
    private final String TAG = getClass().getCanonicalName();


    // Contains the artist data for mArtistAdapter.
    private static List<Artist> mArtistList = new ArrayList<>();

    /*
    * mArtistAdapter is bound to a UI ListView element to provide the
    * results of the artist search.
    */
    private ArtistAdapter mArtistAdapter;


    // The text field where the user enters their search.
    private SearchView mArtistSearchText;

    /*
     * No arg Constructor.
     */
    public ArtistSearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mArtistAdapter = new ArtistAdapter(mArtistList);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*
         * Inflate the view which contains the text field for entering the artist
         * query, and the ListView which displays the results.
         */
        View view = inflater.inflate(R.layout.fragment_artist_search, container, false);

        // Grab the ListView and set its adapter.
        ListView listView = (ListView) view.findViewById(R.id.listview_artists);
        listView.setAdapter(mArtistAdapter);

        /*
         * Detect when a list view item (an artist) is clicked, and launch
         * and intent passing the artist's id as an extra.
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = mArtistAdapter.getItem(position);

                Log.d(TAG, "Artist Clicked.  Name=" + artist.name);

                Intent intent = new Intent(getActivity(), TopTracksActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artist.id);
                startActivity(intent);
            }
        });

        // The field where the user enters the artist they're looking for.
        mArtistSearchText = (SearchView) view.findViewById(R.id.text_artist_search);

        // Detect when the user hits Enter or Done.
//        mArtistSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//
//                /*
//                 * Hitting <Enter> results in an IME_ACTION_UNSPECIFIED.  Clicking "Done" results
//                 * in an IME_ACTION_DONE.  If we get either of these events, then check the length
//                 * of the search string, and as long as it's not empty, call fetchArtists() to
//                 * perform the search.
//                 */
//                if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED) ||
//                        (actionId == EditorInfo.IME_ACTION_DONE)) {
//
//                    if ((v.getText() == null) || (v.getText().length() < 1)) {
//                        Toast.makeText(getActivity(), R.string.no_search_string, Toast.LENGTH_LONG)
//                                .show();
//                        return false;
//
//                    } else {
//                        fetchArtists();
//                        return true;
//                    }
//                }
//
//                return false;
//            }
//        });

        mArtistSearchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                fetchArtists();
                return true;
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

        if (mArtistSearchText.getQuery() == null)  {
            return;
        }

        final String artistName = mArtistSearchText.getQuery().toString().trim();

        if (artistName.length() == 0) {
            return;
        }

        mArtistAdapter.clear();

        SpotifyApi spotifyApi = new SpotifyApi();
        SpotifyService spotifyService = spotifyApi.getService();

        spotifyService.searchArtists(artistName, new Callback<ArtistsPager>() {

            @Override
            public void success(final ArtistsPager artistsPager, Response response) {

                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mArtistAdapter.clear();

                        if (artistsPager.artists.total < 1)  {
                            Toast.makeText(
                                    getActivity(),
                                    getString(R.string.artist_list_empty),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            mArtistAdapter.addAll(artistsPager.artists.items);
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
     * An ArrayAdapter customized to populate the layout with artist data.
     */
    class ArtistAdapter extends ArrayAdapter<Artist> {

        private class ViewData {
            ImageView icon;
            TextView name;
        }


        public ArtistAdapter(List<Artist> objects) {
            super(getActivity(), 0, objects);
        }

        /*
         * An ArrayAdapter requires that we write the results to an EditText field.
         * By overriding the getView method, we can insert the artist data into
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

                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_artist, null);

                viewData = new ViewData();
                viewData.icon = (ImageView) view.findViewById(R.id.image_artist_icon);
                viewData.name = (TextView) view.findViewById(R.id.text_artists_name);

                view.setTag(viewData);  // Store data so we'll have it next time.

            } else {
                viewData = (ViewData) view.getTag();
            }


            Artist artist = getItem(position);

            int idealWidth = (int) getContext().getResources().getDimension(R.dimen.icon_width);
            int idealHeight = (int) getContext().getResources().getDimension(R.dimen.icon_height);

            if (artist != null) {
                viewData.name.setText(artist.name);

                /*
                 * Determines which of the images in the array is closest in size to
                 * what we're looking for.
                 */
                Image image = ImageUtils.getClosestImageSize(
                        artist.images, idealWidth, idealHeight);

                // Fetch the image and store in ViewData object's icon.
                if (image != null) {
                    Picasso.with(getContext())
                            .load(image.url)
                            .resize(idealWidth, idealHeight)
                            .into(viewData.icon);
                }
            }

            return view;

        }
    }
}
