package udacity.nano.spotifystreamer.utils;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

public class ImageUtils {

    /*
     * Returns the Image in the list of Images that is closet to the requested
     * width and height.  This should reduce the amount of work needed when re-sizing.
     */
    public static Image getClosestImageSize(List<Image> images, int idealWidth, int idealHeight) {

        if ((images == null) || (images.size() < 1)) return null;

        Image bestFit = null;
        long bestScore = Long.MAX_VALUE;

        for (Image i : images) {

            // A simple heuristic, but it seems to do the trick.
            long score = Math.abs((i.height - idealHeight) * (i.width - idealWidth));

            if (score < bestScore) {
                bestScore = score;
                bestFit = i;
            }
        }

        return bestFit;

    }

}
