package udacity.nano.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

public class PlayListItem implements Parcelable {

    private String trackUri;
    private String trackId;
    private String trackName;
    private String trackImage;
    private String albumName;
    private String artistName;
    private String artistId;
    private int duration;
    private boolean explicit;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(trackUri);
        dest.writeString(trackId);
        dest.writeString(trackName);
        dest.writeString(trackImage);
        dest.writeString(albumName);
        dest.writeString(artistName);
        dest.writeString(artistId);
        dest.writeInt(duration);
        dest.writeByte((byte) (explicit ? 1 : 0));
    }

    public static final Creator<PlayListItem> CREATOR =
            new Creator<PlayListItem>()  {

        @Override
        public PlayListItem createFromParcel(Parcel source) {
            return new PlayListItem(source);
        }

        @Override
        public PlayListItem[] newArray(int size) {
            return new PlayListItem[size];
        }
    };

    public PlayListItem()  {

    }

    public PlayListItem(Parcel in)  {
        trackUri = in.readString();
        trackId = in.readString();
        trackName = in.readString();
        trackImage = in.readString();
        albumName = in.readString();
        artistName = in.readString();
        artistId = in.readString();
        duration = in.readInt();
        explicit = in.readByte() != 0;
    }

    public String getTrackUri() {
        return trackUri;
    }

    public void setTrackUri(String trackUri) {
        this.trackUri = trackUri;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getTrackImage() {
        return trackImage;
    }

    public void setTrackImage(String trackImage) {
        this.trackImage = trackImage;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}