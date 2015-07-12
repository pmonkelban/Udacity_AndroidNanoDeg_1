package udacity.nano.spotifystreamer;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import udacity.nano.spotifystreamer.adapters.ArtistListAdapter;
import udacity.nano.spotifystreamer.data.StreamerContract;


public class ArtistListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // An identifier used for logging
    private static final String TAG = ArtistListFragment.class.getCanonicalName();

    public static final String BUNDLE_KEY_LAST_SEARCH = "key_last_search";
    private static final String BUNDLE_KEY_LAST_POSITION = "key_last_position";

    /*
    * mArtistListAdapter is bound to a UI ListView element to provide the
    * results of the artist search.
    */
    private ArtistListAdapter mArtistListAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    // Holds the last string that was searched for.
    private Uri mArtistListUri;

    // Dimensions for Artist icon
    private int iconWidth;
    private int iconHeight;

    private static final int ARTIST_LOADER_ID = 0;

    Callback mCallback;

    public interface Callback  {
        void onArtistSelected(Uri trackListUri);
    }

    public ArtistListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iconWidth = (int) getActivity().getResources().getDimension(R.dimen.icon_width);
        iconHeight = (int) getActivity().getResources().getDimension(R.dimen.icon_height);

        try  {
            mCallback = (Callback) getActivity();

        } catch (ClassCastException e)  {
            throw new ClassCastException(getActivity().toString() + " must implement" +
                    "ArtistListFragment.Callback");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_KEY_LAST_SEARCH, mArtistListUri);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null)  {
            mArtistListUri = arguments.getParcelable(BUNDLE_KEY_LAST_SEARCH);

        }
        /*
        * The ArtistListAdapter will take data from a source and
        * use it to populate the ListView that it's attached to.
        */
        mArtistListAdapter = new ArtistListAdapter(getActivity(), null, 0, iconWidth, iconHeight);

        // Inflate the view which contains ListView which displays the results.
        View rootView = inflater.inflate(R.layout.artist_list, container, false);

        // Create a new Adapter and bind it to the ListView
        mListView = (ListView) rootView.findViewById(R.id.artist_search_listView);
        mListView.setAdapter(mArtistListAdapter);

        /*
         * Detect when a list view item (an artist) is clicked, and launch
         * and intent passing the artist's id as an extra.
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);

                if (cursor != null) {
                    String artistSpotifyId = cursor.getString(ArtistListAdapter.IDX_SPOTIFY_ID);

                    Uri trackListUri = StreamerContract
                            .GET_TRACKS_CONTENT_URI
                            .buildUpon()
                            .appendEncodedPath(artistSpotifyId)
                            .build();

                    mCallback.onArtistSelected(trackListUri);
                }
                mPosition = position;

                view.setActivated(true);
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
        getLoaderManager().initLoader(ARTIST_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        /*
        * TODO:  Determine why data is null.  Check network state.
        */
        if (data == null)  {

            // Clear track list data and display a Toast
            mCallback.onArtistSelected(null);
            Toast.makeText(getActivity(), R.string.artist_list_error, Toast.LENGTH_SHORT).show();

        } else if (data.getCount() == 0)  {

            // Clear track list data and display a Toast
            mCallback.onArtistSelected(null);
            Toast.makeText(getActivity(), R.string.artist_list_empty, Toast.LENGTH_SHORT).show();
        }

        mArtistListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION)  {
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)  {

        if (mArtistListUri == null) return null;

        return new CursorLoader(getActivity(),
                mArtistListUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)  {
        mArtistListAdapter.swapCursor(null);
    }

}
