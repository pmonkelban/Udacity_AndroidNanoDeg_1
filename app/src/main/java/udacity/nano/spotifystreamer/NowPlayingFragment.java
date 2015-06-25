package udacity.nano.spotifystreamer;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class NowPlayingFragment extends DialogFragment {

    public interface NowPlayingListener {

        void requestContentRefresh();

        void onPlayClicked();

        void onPauseClicked();

        void onNextClicked();

        void onPrevClicked();

        void seekTo(int miliSeconds);
    }

    private NowPlayingListener mListener;

    private TextView mTextViewArtist;
    private TextView mTextViewTrackName;
    private TextView mTextViewAlbumName;
    private ImageView mTextViewTrackImage;

    private int mImageWidth;
    private int mImageHeight;

    Button mPrevButton;
    Button mPlayButton;
    Button mPauseButton;
    Button mNextButton;

    private String mArtistName;
    private String mTrackUrl;
    private String mTrackName;
    private String mAlbumName;
    private String mTrackImage;

    public void setArtistName(String artistName) {
        this.mArtistName = artistName;
    }

    public void setTrackUrl(String trackUrl) {
        this.mTrackUrl = trackUrl;
    }

    public void setTrackName(String trackName) {
        this.mTrackName = trackName;
    }

    public void setAlbumName(String albumName) {
        this.mAlbumName = albumName;
    }

    public void setTrackImage(String trackImage) {
        this.mTrackImage = trackImage;
    }

    public void setIsPlaying(boolean isPlaying)  {

        if (isPlaying)  {
            mPlayButton.setVisibility(View.GONE);
            mPauseButton.setVisibility(View.VISIBLE);
        } else  {
            mPlayButton.setVisibility(View.VISIBLE);
            mPauseButton.setVisibility(View.GONE);
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageWidth = (int) getActivity().getResources().getDimension(R.dimen.track_image_display_width);
        mImageHeight = (int) getActivity().getResources().getDimension(R.dimen.track_image_display_height);


        try {
            mListener = (NowPlayingListener) getActivity();

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement" +
                    "NowPlayingFragment.NowPlayingListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.now_playing_dialog, container, false);

        mTextViewArtist = (TextView) rootView.findViewById(R.id.artist);
        mTextViewTrackName = (TextView) rootView.findViewById(R.id.track_name);
        mTextViewAlbumName = (TextView) rootView.findViewById(R.id.album_name);
        mTextViewTrackImage = (ImageView) rootView.findViewById(R.id.track_image);

        mPrevButton = (Button) rootView.findViewById(R.id.button_prev);
        mPlayButton = (Button) rootView.findViewById(R.id.button_play);
        mPauseButton = (Button) rootView.findViewById(R.id.button_pause);
        mNextButton = (Button) rootView.findViewById(R.id.button_next);

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPrevClicked();
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPlayClicked();
            }
        });

        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPauseClicked();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onNextClicked();
            }
        });

        mListener.requestContentRefresh();

        return rootView;

    }


    public void refreshContent() {

        mTextViewArtist.setText(mArtistName);
        mTextViewTrackName.setText(mTrackName);
        mTextViewAlbumName.setText(mAlbumName);

        Picasso.with(getActivity().getApplicationContext())
                .load(mTrackImage)
                .resize(mImageWidth, mImageHeight)
                .into(mTextViewTrackImage);


    }
}
