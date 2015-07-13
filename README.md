# Udacity Spotify Streamer

Open Questions:

? Not sure if the preference for showing/hiding notifications on the lock screen works.  All of
my devices have a Settings option that overrides the Notification.setVisibility() setting.  At the
OS level, I have the choices to "Show all notification content" or "Don't show notifications at all"
Setting the notification to PRIVATE, PUBLIC, or SECRET seems to have no effect.

? When should I remove the Notification and stop the track.  The current implementation is not
ideal.  See Known Issues below.

? How to get MediaController widget to look right.  I tried using this, but ran into a number of
problems.  It put the audio controls in separate window in the bottom right corner of the screen.
It required two back button presses to get back to the Top Tracks page.  I couldn't get the
album images to be the correct size after rotations and such, etc.  After trying it
for a while, it worked out better not using it.

? Investigate ways to do unit testing in Android.  Would like to be able to provide a
MockSpotifyService class to StreamerProvider so that queries can be handled locally.

Known Issues:

! Notifications may become "orphaned", that is the track has stopped playing, the app
has closed, but the notification still shows.  If the user attempts to access the app through the
notification, the MainActivity will be started with the initial blank page, and the notification
will be canceled.  In general, notifications are removed when the NowPlayingActivity is closed
and the currently playing track completes, but there is at least one case where this does not occur.

Some background:  The StreamerMediaService issues notifications when a new track starts playing.  It
attempts to re-use/update the same notification when then next track begins.  When the
NowPlayingActivity exits, it sets a flag in the StreamerMediaService letting it know that there
will be no more tracks coming.  The StreamerMediaService is then free to cancel the notification
when the current track completes.

However, if the current track is paused when the NowPlayingActivity exits, the StreamerMediaService
never completes the current song, and thus never removes the notification.  The notification
eventually gets cancelled when the service's onDestroy() method is called, but that can be much
later.

Note that the NowPlayingActivity should not stop the service when it exists, because it might
just be going through a configuration change.  Also, onPause() is the last life-cycle stage
that NowPlayingActivity is guaranteed to go through, but I don't want to remove notifications
here, because this will be called even if the activity is just off screen.

! Ran out of memory once when using the AVD Nexus 5 emulator.  Keep an eye out for this.

! When loading images using the Picasso library it never switches from the placeholder
to the error image.  I expect to see the placeholder image ("Loading...") for a few
seconds, then it should switch to the error ("No Image Available") image.
According to
http://code.tutsplus.com/tutorials/android-sdk-working-with-picasso--cms-22149
"Picasso will try to download the remote image three times and display the error placeholder
image if it was unable to fetch the remote asset."  This doesn't happen.


To Do:

O Add becomingNoisy broadcast listener.  Pause when headphones are unplugged.  Done (in theory) but
needs testing on a real device.  Not sure if AVD can handle this case.

Extra Features:

Below are additional features (beyond the Required and Optional Components described in the rubric).
In many cases these features make the project more complicated than it needed to be.  These are
areas that I wanted to learn how to do, even if it made the project more complex.

* The app uses a SQLite database to cache query results.  The results of any query performed in
the last 2 hours is stored and will not require a network call if requested again.  Data
stored in the cache can be used while offline.  Details of how the cache is implemented
are given the docs CacheArtistFowChar.pdf and CacheTopTracksFlowChart.pdf in the extras directory.
Stale data is removed whenever a new record is inserted.  This prevents its size from growing too
large.

* Pre-Fetch artist and track data.  When a user queries for an artist, it's likely that they'll
select one of the top results.  Therefore, I pre-fetch the track data for the top 3 artists
in order to increase performance.

* Added a setting to filter tracks marked as Explicit.

* Created custom launcher and notification icons.

* When a track ends, the app automatically begins playing the next track by that artist.
Notifications and the highlighted track in the track list are updated.

* Included unit tests for the data package.
