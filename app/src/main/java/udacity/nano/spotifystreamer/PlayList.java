package udacity.nano.spotifystreamer;

public class PlayList {

    private String[] trackUris;
    private String[] trackIds;
    private String[] trackNames;
    private String[] trackImages;
    private String[] albumNames;
    private String artistName;
    private String artistId;
    private int currentPosition;

    public String[] getTrackUris() {
        return trackUris;
    }

    public void setTrackUris(String[] trackUris) {
        this.trackUris = trackUris;
    }

    public String[] getTrackIds() {
        return trackIds;
    }

    public void setTrackIds(String[] trackIds) {
        this.trackIds = trackIds;
    }

    public String[] getTrackNames() {
        return trackNames;
    }

    public void setTrackNames(String[] trackNames) {
        this.trackNames = trackNames;
    }

    public String[] getTrackImages() {
        return trackImages;
    }

    public void setTrackImages(String[] trackImages) {
        this.trackImages = trackImages;
    }

    public String[] getAlbumNames() {
        return albumNames;
    }

    public void setAlbumNames(String[] albumNames) {
        this.albumNames = albumNames;
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

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    /*
    * Moves to the next song.  If the end of the PlayList is reached, loops
    * back to the beginning.
    */
    public void nextTrack()  {
        currentPosition++;

        if (currentPosition >= trackIds.length)  currentPosition = 0;
    }

    /*
    * Moves to the previous song.  If the beginning of the PlayList is reached,
    * loops back to the last song.
    */
    public void previousTrack()  {
        currentPosition--;

        if (currentPosition < 0)  currentPosition = trackIds.length - 1;
    }

    /*
    * Returns the URI of the current track.
    */
    public String getCurrentTrackURI()  {
        return trackUris[currentPosition];
    }

    /*
    * Returns the Spotify ID of the current track.
    */
    public String getCurrentTrackId()  {
        return trackIds[currentPosition];
    }

    /*
    * Returns the name of the current track.
    */
    public String getCurrentTrackName()  {
        return trackNames[currentPosition];
    }

    public String getCurrentTrackImage()  {
        return trackImages[currentPosition];
    }

    /*
    * Returns the name of the current album.
    */
    public String getCurrentAlbumName()  {
        return albumNames[currentPosition];
    }

    /*
    * Returns the name of the current Artist.
    */
    public String getCurrentArtistName()  {
        return artistName;
    }

    /*
    * Returns the Spotify ID of the current Artist.
    */
    public String getCurrentArtistId()  {
        return artistId;
    }

    /*
    * Returns the current Artist and Album together as one String.
    */
    public String getCurrentArtistAndAlbum()  {
        return artistName + " / " + albumNames[currentPosition];
    }

    /*
    * Returns a copy of the current PlayList object.
    */
    public PlayList copy()  {

        String[] tmpArray;

        PlayList newPlayList = new PlayList();

        tmpArray = new String[this.trackUris.length];
        System.arraycopy(this.trackUris, 0, tmpArray, 0, this.trackUris.length);
        newPlayList.setTrackUris(tmpArray);

        tmpArray = new String[this.trackIds.length];
        System.arraycopy(this.trackIds, 0, tmpArray, 0, this.trackIds.length);
        newPlayList.setTrackIds(tmpArray);

        tmpArray = new String[this.trackNames.length];
        System.arraycopy(this.trackNames, 0, tmpArray, 0, this.trackNames.length);
        newPlayList.setTrackIds(tmpArray);

        tmpArray = new String[this.trackImages.length];
        System.arraycopy(this.trackImages, 0, tmpArray, 0, this.trackImages.length);
        newPlayList.setTrackImages(tmpArray);

        tmpArray = new String[this.albumNames.length];
        System.arraycopy(albumNames, 0, tmpArray, 0, this.albumNames.length);
        newPlayList.setAlbumNames(tmpArray);

        newPlayList.setArtistName(this.artistName);
        newPlayList.setArtistId(this.artistId);
        newPlayList.setCurrentPosition(this.currentPosition);

        return newPlayList;
    }
}
