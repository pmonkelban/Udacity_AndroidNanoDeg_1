package udacity.nano.spotifystreamer;

public class PlayList {

    private final PlayListItem[] playListItems;

    private int currentPosition;

    public PlayList(int size)  {
        playListItems = new PlayListItem[size];
        currentPosition = 0;
    }

    public int size()  {
        return playListItems.length;
    }

    public PlayListItem getCurrentItem()  {
        return playListItems[currentPosition];
    }

    public int getPosition()  {
        return currentPosition;
    }

    public void setPosition(int pos)  {
        if ((pos < 0) || (pos > playListItems.length))  {
            throw new IllegalArgumentException(
                    "Invalid PlayListItem requested.  Position:" + pos);
        }

        currentPosition = pos;
    }

    public PlayListItem getItemAt(int pos)  {

        if ((pos < 0) || (pos > playListItems.length))  {
            throw new IllegalArgumentException(
                    "Invalid PlayListItem requested.  Position:" + pos);
        }

        return playListItems[pos];
    }

    public void setItemAt(int pos, PlayListItem item)  {

        if ((pos < 0) || (pos > playListItems.length))  {
            throw new IllegalArgumentException(
                    "Invalid PlayListItem requested.  Position:" + pos);
        }

        playListItems[pos] = item;
    }


    /*
    * Moves to the next song.  If the end of the PlayList is reached, loops
    * back to the beginning.
    */
    public void nextTrack()  {
        currentPosition++;

        if (currentPosition >= playListItems.length)  currentPosition = 0;
    }

    /*
    * Moves to the previous song.  If the beginning of the PlayList is reached,
    * loops back to the last song.
    */
    public void previousTrack()  {
        currentPosition--;

        if (currentPosition < 0)  currentPosition = playListItems.length - 1;
    }

}
