# Udacity Spotify Streamer

Open Questions:

? When should I remove the Notification and stop the track.  I don't want to do it when onDestroy()
is called, because it might just be the device rotating.  Removing the Notification when
NowPlayingActivity is Destroyed and letting the current song play to the end seems like a
reasonable compromise.


Known Issues:

! I've seen some odd behavior when rotating the device, and then returning to the activity via
a Notification.  Once, the image and track information was for the previous track.  When the
current track completed, it straightened itself out.

! Ran out of memory once when using the AVD Nexus 5 emulator.  Keep an eye out for this.

! NetworkOnMainThreadException by doing the following:
Start a track
Hit back button
Go into Preferences and change anything
Select a new track

The preference change will flush the cache.  At this point, the list of tracks is invalid, and
should be cleared.  Also clear the currently selected Artist.  Once the user selects an Artist
again, the problem goes away.

To Do:

X Custom Launcher icon.

X Better Notification bar icon.

O Make the UI elements look better.  Pick better colors.  Adjust padding, etc.

O Add becomingNoisy broadcast listener.  Pause when headphones are unplugged.

