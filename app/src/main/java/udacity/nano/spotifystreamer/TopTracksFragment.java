package udacity.nano.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import udacity.nano.spotifystreamer.services.StreamerMediaService;


public class TopTracksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // An identifier used for logging
    private final String TAG = getClass().getCanonicalName();

    // A key to get track list out of the Bundle
    public static final String BUNDLE_KEY_ARTIST_ID = "key_artist_id";
    private static final String BUNDLE_KEY_LAST_POSITION = "key_last_position";


    // Bound to a UI ListView element to provide the results of
    // the top tracks query.
    private TopTracksAdapter mTrackAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    private Uri mTrackListUri;

    // Desired height and width for icons.
    private int iconWidth;
    private int iconHeight;

    private static final int TRACK_LOADER_ID = 1;

    StreamerMediaService mStreamerService;
    boolean isStreamerServiceBound = false;

    private ServiceConnection mStreamerServiceConnection = new ServiceConnection()  {

        public void onServiceConnected(ComponentName className, IBinder service)  {
            StreamerMediaService.StreamerMediaServiceBinder binder =
                    (StreamerMediaService.StreamerMediaServiceBinder) service;
            mStreamerService = binder.getService();
            isStreamerServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isStreamerServiceBound = false;
        }
    };

    /*
     * No arg Constructor.
     */
    public TopTracksFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        iconWidth = (int) getActivity().getResources().getDimension(R.dimen.icon_width);
        iconHeight = (int) getActivity().getResources().getDimension(R.dimen.icon_height);

        Intent intent = new Intent(getActivity(), StreamerMediaService.class);
        isStreamerServiceBound = getActivity().bindService(intent, mStreamerServiceConnection, Context.BIND_AUTO_CREATE);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_KEY_ARTIST_ID, mTrackListUri);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null)  {
            mTrackListUri = arguments.getParcelable(BUNDLE_KEY_ARTIST_ID);

        }

        /*
        * The TopTracksAdapter will take data from a source and
        * use it to populate the ListView that it's attached to.
        */
        mTrackAdapter = new TopTracksAdapter(getActivity(), null, 0, iconWidth, iconHeight);

        // Inflate the view which contains the ListView which displays the results.
        View rootView = inflater.inflate(R.layout.track_list, container, false);

        // Create a new Adapter and bind it to the ListView
        mListView = (ListView) rootView.findViewById(R.id.listview_tracks);
        mListView.setAdapter(mTrackAdapter);

         /*
         * Detect when a list view item (an artist) is clicked, and launch
         * and intent passing the artist's id as an extra.
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);

                /*
                * Must make sure that isStreamerServiceBound is true.  It's possible that
                * ServiceConnection.onServiceConnected() callback hasn't happened yet, in
                * which case mStreamerService will be null.
                */
                if ((cursor != null) && (isStreamerServiceBound)) {
                    String previewURL = cursor.getString(TopTracksAdapter.IDX_PREVIEW_URL);
                    mStreamerService.play(Uri.parse(previewURL));
                }

                mPosition = position;
            }
        });

        // Read last search and last position from the saved state
        if (savedInstanceState != null)  {
            if (savedInstanceState.containsKey(BUNDLE_KEY_LAST_POSITION))  {
                mPosition = savedInstanceState.getInt(BUNDLE_KEY_LAST_POSITION);
            }
        }

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)  {
        getLoaderManager().initLoader(TRACK_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTrackAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION)  {
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)  {

        if (mTrackListUri == null)  return null;

        return new CursorLoader(getActivity(),
                mTrackListUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)  {
        mTrackAdapter.swapCursor(null);
    }


}
