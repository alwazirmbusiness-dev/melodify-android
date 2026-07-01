package com.melodifyverse.nancyajram;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import androidx.core.app.NotificationCompat;

public class ExampleService extends Service {

    public static final String CHANNEL_ID = "com.melodifyverse.trend.MusicPlayerChannel";
    public static final String ACTION_PREVIOUS = "com.melodifyverse.trend.action.PREVIOUS";
    public static final String ACTION_PLAY = "com.melodifyverse.trend.action.PLAY";
    public static final String ACTION_NEXT = "com.melodifyverse.trend.action.NEXT";

    private MediaSessionCompat mediaSession; // field to avoid leak

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Guard against null intent (e.g. service restarted by system)
        String action = (intent != null) ? intent.getAction() : null;
        if (action != null) {
            switch (action) {
                case ACTION_PREVIOUS:
                    // Broadcast back to Play activity to handle prev
                    sendBroadcast(new Intent(ACTION_PREVIOUS));
                    break;
                case ACTION_PLAY:
                    PlayerManager.toggle();
                    break;
                case ACTION_NEXT:
                    // Broadcast back to Play activity to handle next
                    sendBroadcast(new Intent(ACTION_NEXT));
                    break;
            }
        }
        showNotification();
        return START_NOT_STICKY;
    }

    private void showNotification() {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, Play.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

        // Create/reuse media session
        if (mediaSession == null) {
            mediaSession = new MediaSessionCompat(this, "MusicPlayerSession");
        }

        // Previous action — unique requestCode 1
        Intent prevIntent = new Intent(this, ExampleService.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 1, prevIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        // Play/Pause action — unique requestCode 2
        Intent playIntent = new Intent(this, ExampleService.class).setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 2, playIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        // Next action — unique requestCode 3
        Intent nextIntent = new Intent(this, ExampleService.class).setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 3, nextIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        String songTitle = PlayerManager.getCurrentSongTitle();
        if (songTitle == null || songTitle.isEmpty()) {
            songTitle = "No song playing";
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Now Playing")
                .setContentText(songTitle)
                .setSmallIcon(R.drawable.musiclogo)
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.ic_media_previous, "Previous", prevPendingIntent)
                .addAction(new NotificationCompat.Action(PlayerManager.isPlaying() ? R.drawable.pause33 : R.drawable.play__1_, "Play", playPendingIntent))
                .addAction(R.drawable.ic_media_next, "Next", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2));

        startForeground(1, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Music Player";
            // Use IMPORTANCE_LOW so the notification doesn't make intrusive sounds/popups
            NotificationChannel chan = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
        stopForeground(true);
    }
}
