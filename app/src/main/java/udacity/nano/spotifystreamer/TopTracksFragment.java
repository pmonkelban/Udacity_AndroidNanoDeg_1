package udacity.nano.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import udacity.nano.spotifystreamer.model.TrackData;
import udacity.nano.spotifystreamer.utils.ImageUtils;


public class TopTracksFragment extends Fragment {

    // An identifier used for logging
    private final String TAG = getClass().getCanonicalName();

    // A key to get track list out of the Bundle
    private static final String BUNDLE_KEY_TRACK_LIST = "track_list";

    // Contains the track data for mTrackAdapter.
    private ArrayList<TrackData> mTrackList;

    // Bound to a UI ListView element to provide the results of
    // the top tracks query.
    private TrackAdapter mTrackAdapter;

    // The ID of the artist we're getting tracks for.
    private String mArtistId;

    /*
     * Used to store the user's country.  We get this from their
     * Default Local, and use it when using the Spotify API.
     */
    private Map<String, Object> mLocationMap = new HashMap<>();

    // Desired height and width for icons.
    private int idealIconWidth;
    private int idealIconHeight;

    private final SpotifyService mSpotifyService;


    /*
     * No arg Constructor.
     */
    public TopTracksFragment() {

        mSpotifyService = new SpotifyApi().getService();

        // Get the user's country, and store it in a map.
        mLocationMap.put(SpotifyService.COUNTRY,
                Locale.getDefault().getCountry());

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

        if (mTrackList != null) {
            outState.putParcelableArrayList(BUNDLE_KEY_TRACK_LIST, mTrackList);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*
         * Inflate the view that contains the top tracks ListView.
         */
        View view = inflater.inflate(R.layout.track_list, container, false);

        // Create a new Adapter and bind it to the ListView
        ListView trackListView = (ListView) view.findViewById(R.id.listview_tracks);
        mTrackAdapter = new TrackAdapter(new ArrayList<TrackData>());
        trackListView.setAdapter(mTrackAdapter);

        // Grab the artist's ID from the intent extra data.
        Intent intent = getActivity().getIntent();
        if ((intent != null) && (intent.hasExtra(Intent.EXTRA_TEXT))) {
            mArtistId = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        /*
        * If we've already have the track info from the savedInstanceState Bundle, then
        * load that into mTrackList.  Otherwise, use fetchTracks() to load it using the
        * Spotify API.
        */
        if ((savedInstanceState != null) &&
                (savedInstanceState.containsKey(BUNDLE_KEY_TRACK_LIST))) {

            mTrackList = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_TRACK_LIST);
            mTrackAdapter.clear();
            mTrackAdapter.addAll(mTrackList);

        } else {
            fetchTracks();
        }

        return view;
    }


    /*
     * Fetches Track data using the Spotify web service.
     * Get's the top hits associated with the given artist, and puts them in mTrackAdapter.
     */
    private void fetchTracks() {

        mTrackAdapter.clear();

        mSpotifyService.getArtistTopTrack(mArtistId, mLocationMap, new Callback<Tracks>() {

            @Override
            public void success(final Tracks tracks, Response response) {

                /*
                 * Convert the Track objects into TrackData objects which
                 * can be stored in a Bundle.
                 */
                mTrackList = convertToTrackData(tracks);

                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mTrackAdapter.clear();

                        if (mTrackList.isEmpty()){
                            Toast.makeText(
                                    getActivity(),
                                    getString(R.string.track_list_empty),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }else{
                            mTrackAdapter.addAll(mTrackList);
                        }
                    }
                });
            }

            @Override
            public void failure(final RetrofitError error) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Error Getting Tracks: " + error.getMessage());
                        Toast.makeText(
                                getActivity(),
                                getString(R.string.error_fetching_tracks),
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
    private ArrayList<TrackData> convertToTrackData(Tracks tracks) {

        ArrayList<TrackData> trackDataList = new ArrayList<>();

        if (tracks != null) {

            for (Track track : tracks.tracks) {

                Image i = ImageUtils.getClosestImageSize(track.album.images, idealIconWidth,
                        idealIconHeight);

                String iconUrl = (i == null) ? null : i.url;

                trackDataList.add(new TrackData(track.name, track.album.name, iconUrl));
            }

        }

        return trackDataList;

    }

    /*
     * An Adapter that know how to populate track data.
     */
    class TrackAdapter extends ArrayAdapter<TrackData> {

        private class ViewData {
            ImageView icon;
            TextView trackName;
            TextView albumName;
        }


        public TrackAdapter(List<TrackData> objects) {
            super(getActivity(), 0, objects);
        }

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
                        R.layout.list_item_track, null);

                viewData = new ViewData();
                viewData.icon = (ImageView) view.findViewById(R.id.image_track_icon);
                viewData.trackName = (TextView) view.findViewById(R.id.text_track_name);
                viewData.albumName = (TextView) view.findViewById(R.id.text_album_name);

                view.setTag(viewData);  // Store data so we'll have it next time.

            } else {
                viewData = (ViewData) view.getTag();
            }


            TrackData track = getItem(position);

            if (track != null) {
                viewData.trackName.setText(track.getName());
                viewData.albumName.setText(track.getAlbum());

                // Fetch the image and store in ViewData object's icon.
                if (track.getUrl() != null) {
                    Picasso.with(getContext())
                            .load(track.getUrl())
                            .resize(idealIconWidth, idealIconHeight)
                            .into(viewData.icon);
                }
            }

            return view;

        }
    }
}
