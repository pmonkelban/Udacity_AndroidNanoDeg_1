package udacity.nano.spotifystreamer;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import udacity.nano.spotifystreamer.activities.NowPlayingActivity;
import udacity.nano.spotifystreamer.activities.SpotifyStreamerActivity;
import udacity.nano.spotifystreamer.adapters.TrackListAdapter;
import udacity.nano.spotifystreamer.data.StreamerProvider;
import udacity.nano.spotifystreamer.services.StreamerMediaService;


public class TrackListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // An identifier used for logging
    private final String TAG = getClass().getCanonicalName();

    // A key to get track list out of the Bundle
    public static final String BUNDLE_KEY_ARTIST_ID = "key_artist_id";
    private static final String BUNDLE_KEY_LAST_POSITION = "key_last_position";

    /*
    * Bound to a UI ListView element to provide the results of
    * the top tracks query.
    */
    private TrackListAdapter mTrackAdapter;

    private ListView mTrackListView;
    private int mPosition = ListView.INVALID_POSITION;

    private Uri mTrackListUri;

    // Desired height and width for icons.
    private int iconWidth;
    private int iconHeight;

    private static final int TRACK_LOADER_ID = 1;

    /*
    * When we receive notice that the track has started playing, highlight the
    * corresponding entry (if we have it).
    */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() called");

            switch (intent.getAction()) {
                case StreamerMediaService.TRACK_START_BROADCAST_FILTER: {

                    // When a track is finished, update the currently highlighted track.
                    PlayListItem item = intent.getParcelableExtra(SpotifyStreamerActivity.KEY_CURRENT_TRACK);

                    for (int p = 0; p < mTrackListView.getAdapter().getCount(); p++) {
                        Cursor cursor = (Cursor) mTrackListView.getItemAtPosition(p);

                        if (!cursor.isClosed()) {
                            String listItemSpotifyId = cursor.getString(StreamerProvider.TRACKS_BY_ARTIST_IDX_TRACK_SPOTIFY_ID);

                            if (item.getTrackId().equals(listItemSpotifyId)) {
                                mPosition = p;
                                mTrackListView.setItemChecked(mPosition, true);
                                mTrackListView.smoothScrollToPosition(mPosition);
                                break;  // break for loop
                            }
                        }
                    }

                    break;
                }
                case StreamerMediaService.TRACK_STOP_BROADCAST_FILTER: {

                    // Clear the currently highlighted track.
                    mTrackListView.setItemChecked(mPosition, false);
                    break;
                }

                default:
                    throw new IllegalArgumentException("Unexpected broadcast message received: " +
                            intent.getAction());
            }
        }
    };

    /*
     * No arg Constructor.
     */
    public TrackListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        iconWidth = (int) getActivity().getResources().getDimension(R.dimen.icon_width);
        iconHeight = (int) getActivity().getResources().getDimension(R.dimen.icon_height);

        // Register to receive track start broadcast notifications
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
                mBroadcastReceiver,
                new IntentFilter(StreamerMediaService.TRACK_START_BROADCAST_FILTER));

        // Register to receive track stop broadcast notifications
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
                mBroadcastReceiver,
                new IntentFilter(StreamerMediaService.TRACK_STOP_BROADCAST_FILTER));

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        // Store the URI to get the current list of tracks.
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_KEY_ARTIST_ID, mTrackListUri);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mTrackListUri = arguments.getParcelable(BUNDLE_KEY_ARTIST_ID);

        }

        /*
        * The TrackListAdapter will take data from a source and
        * use it to populate the ListView that it's attached to.
        */
        mTrackAdapter = new TrackListAdapter(getActivity(), null, 0, iconWidth, iconHeight);

        // Inflate the view which contains the ListView which displays the results.
        View rootView = inflater.inflate(R.layout.track_list, container, false);

        // Create a new Adapter and bind it to the ListView
        mTrackListView = (ListView) rootView.findViewById(R.id.listview_tracks);
        mTrackListView.setEmptyView(rootView.findViewById(R.id.no_track_data));
        mTrackListView.setAdapter(mTrackAdapter);

        /*
        * Detect when a list view item (a track) is clicked, and launch
        * and intent passing the artist's id and track id as extras.
        */
        mTrackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), NowPlayingActivity.class);

                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);

                String trackSpotifyId = cursor.getString(
                        StreamerProvider.TRACKS_BY_ARTIST_IDX_TRACK_SPOTIFY_ID);

                String artistSpotifyId = cursor.getString(
                        StreamerProvider.TRACKS_BY_ARTIST_IDX_ARTIST_SPOTIFY_ID);

                intent.putExtra(SpotifyStreamerActivity.KEY_TRACK_SPOTIFY_ID, trackSpotifyId);
                intent.putExtra(SpotifyStreamerActivity.KEY_ARTIST_SPOTIFY_ID, artistSpotifyId);

                startActivity(intent);

                mPosition = position;

                view.setActivated(true);

            }
        });

        // Read last position from the saved state
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(BUNDLE_KEY_LAST_POSITION)) {
                mPosition = savedInstanceState.getInt(BUNDLE_KEY_LAST_POSITION);
            }
        }

        return rootView;

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRACK_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data == null) {
            Toast.makeText(getActivity(), R.string.track_list_error, Toast.LENGTH_SHORT).show();

        } else if (data.getCount() == 0) {
            Toast.makeText(getActivity(), R.string.track_list_empty, Toast.LENGTH_SHORT).show();
        }

        mTrackAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mTrackListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        if (mTrackListUri == null) return null;

        return new CursorLoader(getActivity(),
                mTrackListUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTrackAdapter.swapCursor(null);
    }


}
