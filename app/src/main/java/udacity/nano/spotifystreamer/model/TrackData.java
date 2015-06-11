package udacity.nano.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

/*
* Stores relevant data about an artist's tracks
* into a Parcelable object.  A Parcelable object can
* be store in a Bundle, thus allow this data to be
* retreived when the Activity is restarted.
*/
public class TrackData implements Parcelable {

    String name;
    String album;
    String url;


    public String getName() {
        return name;
    }

    public String getAlbum() {
        return album;
    }

    public String getUrl() {
        return url;
    }

    public TrackData(String name, String album, String url)  {
        this.name = name;
        this.album = album;
        this.url = url;
    }

    private TrackData(Parcel in)  {
        this.name = in.readString();
        this.album = in.readString();
        this.url = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString()  {
        return name + ": " + album + ": " + url;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(album);
        dest.writeString(url);
    }

    public static final Creator<TrackData> CREATOR =
            new Creator<TrackData>()  {

        @Override
        public TrackData createFromParcel(Parcel source) {
            return new TrackData(source);
        }

        @Override
        public TrackData[] newArray(int size) {
            return new TrackData[size];
        }
    };
}
