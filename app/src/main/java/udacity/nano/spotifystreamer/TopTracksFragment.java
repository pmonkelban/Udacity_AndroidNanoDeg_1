package udacity.nano.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import udacity.nano.spotifystreamer.utils.ImageUtils;


public class TopTracksFragment extends Fragment {

    // An identifier used for logging
    private final String TAG = getClass().getCanonicalName();

    /*
    * Bound to a UI ListView element to provide the results of
    * the top tracks query.
    */
    private TrackAdapter mTrackAdapter;

    // Contains the track data for mTrackAdapter.
    private static List<Track> mTrackList = new ArrayList<>();

    // Holds the id of the last artist that was looked up.
    private static String mLastArtistFetched = "";

    // The ID of the artist we're getting tracks for.
    private String mArtistId;

    /*
     * Used to store the user's country.  We get this from their
     * Default Local, and use it when using the Spotify API.
     */
    private Map<String, Object> mLocationMap = new HashMap<>();

    /*
     * No arg Constructor.
     */
    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mTrackAdapter = new TrackAdapter(mTrackList);

        // Get the user's country, and store it in a map.
        mLocationMap.put(SpotifyService.COUNTRY,
                Locale.getDefault().getCountry());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mTrackAdapter = new TrackAdapter(mTrackList);

        /*
         * Inflate the view that contains the top tracks ListView.
         */
        View view = inflater.inflate(R.layout.fragment_track_list, container, false);

        // Grab the ListView and set its adapter
        ListView listView = (ListView) view.findViewById(R.id.listview_tracks);
        listView.setAdapter(mTrackAdapter);

        // Grab the artist's ID from the intent extra data.
        Intent intent = getActivity().getIntent();
        if ((intent != null) && (intent.hasExtra(Intent.EXTRA_TEXT)))  {
            mArtistId = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        if (!mLastArtistFetched.equals(mArtistId))  {
            mLastArtistFetched = mArtistId;
            updateTracks();
        }

        return view;
    }


    private void updateTracks() {

        mTrackAdapter.clear();

        /*
         * Creates an AsyncTask to fetch the track data in the background.
         */
        new AsyncTask<String, Void, List<Track>>() {

            @Override
            protected List<Track> doInBackground(String... params) {

                // Get track data from Spotify.
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                Tracks topTracks = spotify.getArtistTopTrack(mArtistId, mLocationMap);

                return topTracks.tracks;
            }

            /*
             * Called after all the track data is fetched.
             */
            @Override
            protected void onPostExecute(List<Track> topTrackList) {

                if ((topTrackList == null) || (topTrackList.isEmpty())) {
                    Toast.makeText(getActivity(), R.string.track_list_empty, Toast.LENGTH_LONG).show();
                } else {
                    mTrackAdapter.clear(); // This was cleared above, just a precaution.
                    mTrackAdapter.addAll(topTrackList);
                }

            }

        }.execute(); // Kick-off the AsyncTask

    }

    /*
     * An Adapter that know how to populate track data.
     */
    class TrackAdapter extends ArrayAdapter<Track> {

        private class ViewData {
            ImageView icon;
            TextView trackName;
            TextView albumName;
        }


        public TrackAdapter(List<Track> objects) {
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

                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_track, null);

                viewData = new ViewData();
                viewData.icon = (ImageView) view.findViewById(R.id.image_track_icon);
                viewData.trackName = (TextView) view.findViewById(R.id.text_track_name);
                viewData.albumName = (TextView) view.findViewById(R.id.text_album_name);

                view.setTag(viewData);  // Store data so we'll have it next time.

            } else {
                viewData = (ViewData) view.getTag();
            }


            Track track = getItem(position);

            int idealWidth =(int) getContext().getResources().getDimension(R.dimen.icon_width);
            int idealHeight = (int) getContext().getResources().getDimension(R.dimen.icon_height);

            if (track != null) {
                viewData.trackName.setText(track.name);
                viewData.albumName.setText(track.album.name);

                Image image = ImageUtils.getClosestImageSize(track.album.images,
                        idealWidth, idealHeight);

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
