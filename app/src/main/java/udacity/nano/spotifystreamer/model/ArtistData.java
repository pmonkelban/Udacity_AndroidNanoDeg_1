package udacity.nano.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Artist;

/*
* Stores relevant data from the Spotify API's Artist class
* into a Parcelable object.  A Parcelable object can
* be store in a Bundle, thus allow this data to be
* retreived when the Activity is restarted.
*/
public class ArtistData implements Parcelable {

    String id;
    String name;
    String url;


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    /*
    * Constructor takes an Artist object and the url of the
    * most useful image to be used as an icon.
    */
    public ArtistData(Artist artist, String url) {
        this.name = artist.name;
        this.id = artist.id;
        this.url = url;
    }

    private ArtistData(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.url = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return id + ": " + name + ": " + url;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(url);
    }

    public static final Parcelable.Creator<ArtistData> CREATOR =
            new Parcelable.Creator<ArtistData>() {

                @Override
                public ArtistData createFromParcel(Parcel source) {
                    return new ArtistData(source);
                }

                @Override
                public ArtistData[] newArray(int size) {
                    return new ArtistData[size];
                }
            };
}
