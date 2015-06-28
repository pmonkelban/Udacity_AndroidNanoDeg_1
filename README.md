# Udacity Spotify Streamer

Open Questions:

? When should I remove the Notification and stop the track.  I don't want to do it when onDestroy()
is called, because it might just be the device rotating.  Removing the Notification when
NowPlayingActivity is Destroyed and letting the current song play to the end seems like a
reasonable compromise.

